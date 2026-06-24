package net.dreamlu.mica.admin.project.im.push;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.project.im.session.ImSessionRegistry;
import net.dreamlu.mica.admin.project.im.topic.MqttTopicConstants;
import net.dreamlu.mica.core.utils.JsonUtil;
import org.dromara.mica.mqtt.codec.MqttQoS;
import org.dromara.mica.mqtt.spring.server.MqttServerTemplate;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * IM 模块统一推送服务。
 * <p>
 * 任何模块需要向 IM 通道下发消息时，都通过本服务发送，避免业务代码直接依赖 MqttServerTemplate。
 * 设计要点（详见 docs/im/architecture.md §3.3）：
 * <ul>
 *   <li>序列化统一为 UTF-8 JSON。</li>
 *   <li>QoS = AT_LEAST_ONCE（QOS1），保证投递可达；顺序由 single-threaded publisher 保证。</li>
 *   <li>用户离线时静默丢弃（{@code sys_user_message} 已写入，作为离线兜底）。</li>
 * </ul>
 *
 * @author L.cm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImPushService implements SmartInitializingSingleton {
	private final ApplicationContext applicationContext;
	private final ImSessionRegistry sessionRegistry;
	private MqttServerTemplate mqttServerTemplate;

	/**
	 * 推送系统消息给单个用户。
	 *
	 * @param userId  目标用户
	 * @param payload payload
	 * @return true 投递到 broker 成功；false 用户离线或序列化失败
	 */
	public boolean pushSystemMessage(Long userId, ImPushPayload payload) {
		if (!sessionRegistry.isOnline(userId)) {
			log.debug("[IM] pushSystemMessage skipped, user offline, userId={}", userId);
			return false;
		}
		return publish(MqttTopicConstants.sysTopic(userId), payload);
	}

	/**
	 * 推送系统消息给多个用户（fan-out）。
	 *
	 * @param userIds 目标用户列表
	 * @param payload payload（同一份内容）
	 * @return 成功送达的人数（在线且 publish 返回 true）
	 */
	public int broadcastSystemMessage(Iterable<Long> userIds, ImPushPayload payload) {
		int success = 0;
		for (Long userId : userIds) {
			if (pushSystemMessage(userId, payload)) {
				success++;
			}
		}
		return success;
	}

	/**
	 * 单聊下行：推送给会话双方。
	 * <p>
	 * 注意：本方法适用于服务端主动推送（例如新消息入库后的实时下行）；
	 * 客户端 P2P 上行消息由 {@code IMqttMessageListener} 监听后调用本服务。
	 *
	 * @param conversationId 会话 id
	 * @param userA          参与方 A
	 * @param userB          参与方 B
	 * @param payload        payload
	 * @return 在线送达端数
	 */
	public int pushP2pMessage(String conversationId, Long userA, Long userB, ImPushPayload payload) {
		int success = 0;
		String topic = MqttTopicConstants.p2pOutTopic(userA, userB);
		if (sessionRegistry.isOnline(userA) && publish(topic, payload)) {
			success++;
		}
		if (sessionRegistry.isOnline(userB) && publish(topic, payload)) {
			success++;
		}
		return success;
	}

	/**
	 * 群聊下行：群成员各自订阅自己的下行 topic。
	 * <p>
	 * 群成员列表需由调用方提供（Phase 1.1）。
	 *
	 * @param groupId   群 id
	 * @param memberIds 群成员列表
	 * @param payload   payload
	 * @return 在线送达人数
	 */
	public int pushGroupMessage(Long groupId, Iterable<Long> memberIds, ImPushPayload payload) {
		int success = 0;
		String topic = MqttTopicConstants.groupOutTopic(groupId);
		for (Long memberId : memberIds) {
			if (sessionRegistry.isOnline(memberId) && publish(topic, payload)) {
				success++;
			}
		}
		return success;
	}

	/**
	 * 推送"已读回传"事件：B 标记某会话已读后，通知 A（A 的会话未读数也要减少）。
	 *
	 * @param conversationId 会话 id
	 * @param peerUserId     对端用户 id（需要被通知的一方）
	 * @param readerUserId   执行已读的用户 id
	 * @param readToMsgId    已读到的最新消息 id
	 * @return true 成功下发
	 */
	public boolean pushReadReceipt(String conversationId, Long peerUserId, Long readerUserId, Long readToMsgId) {
		if (!sessionRegistry.isOnline(peerUserId)) {
			return false;
		}
		ImPushPayload payload = ImPushPayload.builder()
			.msgId(java.util.UUID.randomUUID().toString())
			.msgType("p2p")
			.conversationId(conversationId)
			.senderId(readerUserId)
			.eventType("read")
			.eventMsgId(readToMsgId)
			.serverTime(LocalDateTime.now())
			.build();
		return publish(MqttTopicConstants.p2pOutTopicByConv(conversationId), payload);
	}

	/**
	 * 推送"消息撤回"事件到会话双方。
	 */
	public int pushRecallEvent(String conversationId, Iterable<Long> memberIds, Long recalledMsgId, Long operatorId) {
		int success = 0;
		String topic = MqttTopicConstants.p2pOutTopicByConv(conversationId);
		ImPushPayload payload = ImPushPayload.builder()
			.msgId(java.util.UUID.randomUUID().toString())
			.msgType("p2p")
			.conversationId(conversationId)
			.senderId(operatorId)
			.eventType("recall")
			.eventMsgId(recalledMsgId)
			.serverTime(LocalDateTime.now())
			.build();
		for (Long memberId : memberIds) {
			if (sessionRegistry.isOnline(memberId) && publish(topic, payload)) {
				success++;
			}
		}
		return success;
	}

	/**
	 * 通用发送方法：序列化 → 发布。
	 */
	private boolean publish(String topic, ImPushPayload payload) {
		if (payload.getServerTime() == null) {
			payload.setServerTime(LocalDateTime.now());
		}
		byte[] body = JsonUtil.toJsonAsBytes(payload);
		try {
			return mqttServerTemplate.publishAll(topic, body, MqttQoS.QOS1);
		} catch (Exception e) {
			log.error("[IM] publish failed, broker error, topic={}", topic, e);
			return false;
		}
	}

	@Override
	public void afterSingletonsInstantiated() {
		mqttServerTemplate = applicationContext.getBean(MqttServerTemplate.class);
	}
}
