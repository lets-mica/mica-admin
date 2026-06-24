package net.dreamlu.mica.admin.project.im.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.project.im.auth.MqttAuthInterceptor;
import net.dreamlu.mica.admin.project.im.entity.ImMessage;
import net.dreamlu.mica.admin.project.im.push.ImPushPayload;
import net.dreamlu.mica.admin.project.im.push.ImPushService;
import net.dreamlu.mica.admin.project.im.service.IImMessageService;
import net.dreamlu.mica.admin.project.im.topic.MqttTopicConstants;
import net.dreamlu.mica.net.core.ChannelContext;
import org.dromara.mica.mqtt.codec.MqttQoS;
import org.dromara.mica.mqtt.codec.message.MqttPublishMessage;
import org.dromara.mica.mqtt.core.server.event.IMqttMessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 单聊上行消息处理器。
 * <p>
 * 客户端通过 {@code im/p2p/{sender}/{receiver}} 上行消息，本处理器负责：
 * <ol>
 *   <li>解析 payload JSON，提取内容 / 类型。</li>
 *   <li>入库（自动创建/更新会话、累加未读）。</li>
 *   <li>通过 {@link ImPushService} 推送到对端订阅的下行 topic。</li>
 * </ol>
 *
 * @author L.cm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImP2pMessageHandler implements IMqttMessageListener {
	private final IImMessageService messageService;
	private final ImPushService imPushService;
	private final ObjectMapper objectMapper;

	@Override
	public void onMessage(ChannelContext context, String clientId, String topic, MqttQoS qoS, MqttPublishMessage message) {
		// 仅处理 p2p 上行
		if (topic == null || !topic.startsWith(MqttTopicConstants.P2P_TOPIC_PREFIX)) {
			return;
		}
		// 已通过 MqttTopicFilter 的 hasPermission 校验，这里拿 userId 不会为 null
		Long userId = MqttAuthInterceptor.getUserId(context);
		if (userId == null) {
			log.warn("[IM] P2P receive skipped: missing userId, topic={}", topic);
			return;
		}
		// 1. 解析 topic: im/p2p/{sender}/{receiver}
		String[] parts = topic.substring(MqttTopicConstants.P2P_TOPIC_PREFIX.length()).split("/");
		if (parts.length != 2) {
			log.warn("[IM] P2P receive skipped: invalid topic, topic={}", topic);
			return;
		}
		long senderId;
		long receiverId;
		try {
			senderId = Long.parseLong(parts[0]);
			receiverId = Long.parseLong(parts[1]);
		} catch (NumberFormatException e) {
			log.warn("[IM] P2P receive skipped: non-numeric topic, topic={}", topic);
			return;
		}
		if (senderId != userId) {
			log.warn("[IM] P2P receive rejected: senderId mismatch, userId={}, topic={}", userId, topic);
			return;
		}
		// 2. 解析 payload
		ImMessage incoming;
		try {
			incoming = objectMapper.readValue(message.getPayload(), ImMessage.class);
		} catch (Exception e) {
			log.error("[IM] P2P receive failed: invalid payload, topic={}", topic, e);
			return;
		}
		// 3. 入库
		ImMessage entity = new ImMessage();
		entity.setSenderId(senderId);
		entity.setReceiverId(receiverId);
		entity.setMsgType(incoming.getMsgType());
		entity.setContent(incoming.getContent());
		entity.setExtra(incoming.getExtra());
		try {
			entity = messageService.saveP2pMessage(entity);
		} catch (Exception e) {
			log.error("[IM] P2P receive failed: save error, sender={}, receiver={}", senderId, receiverId, e);
			return;
		}
		log.info("[IM] P2P message saved: id={}, sender={}, receiver={}", entity.getId(), senderId, receiverId);
		// 4. 推送下行
		ImPushPayload payload = ImPushPayload.builder()
			.msgId(UUID.randomUUID().toString())
			.msgType("p2p")
			.conversationId(entity.getConversationId())
			.senderId(senderId)
			.receiverId(receiverId)
			.content(entity.getContent())
			.extra(entity.getExtra())
			.serverTime(entity.getServerReceivedAt())
			.build();
		imPushService.pushP2pMessage(entity.getConversationId(), senderId, receiverId, payload);
	}
}
