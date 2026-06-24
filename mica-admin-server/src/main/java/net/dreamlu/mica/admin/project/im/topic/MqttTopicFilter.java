package net.dreamlu.mica.admin.project.im.topic;

import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.project.im.auth.MqttAuthInterceptor;
import net.dreamlu.mica.net.core.ChannelContext;
import org.dromara.mica.mqtt.codec.MqttQoS;
import org.dromara.mica.mqtt.core.server.auth.IMqttServerPublishPermission;
import org.dromara.mica.mqtt.core.server.auth.IMqttServerSubscribeValidator;
import org.springframework.stereotype.Component;

/**
 * IM 模块订阅/发布权限过滤器。
 * <p>
 * 规则（详见 docs/im/architecture.md §2.3）：
 * <ul>
 *   <li>只能订阅以 {@link MqttTopicConstants#TOPIC_PREFIX}{@code } 开头的 topic。</li>
 *   <li>系统消息 topic ({@code im/sys/{userId}})：只能订阅属于自己的 userId。</li>
 *   <li>单聊 topic ({@code im/p2p/{a}/{b}})：订阅的 {a}/{b} 中必须包含自己；发布的 {sender} 必须是自己。</li>
 *   <li>群聊 topic ({@code im/group/{gid}/out})：订阅/发布权限由 Phase 1.1 的群成员服务校验，
 *       本期只允许前缀匹配 + 字数长度校验，留 TODO。</li>
 * </ul>
 *
 * @author L.cm
 */
@Slf4j
@Component
public class MqttTopicFilter implements IMqttServerSubscribeValidator, IMqttServerPublishPermission {

	@Override
	public boolean isValid(ChannelContext context, String clientId, String topic, MqttQoS qoS) {
		Long userId = MqttAuthInterceptor.getUserId(context);
		if (userId == null) {
			log.warn("[IM] SUBSCRIBE rejected: missing userId, clientId={}", clientId);
			return false;
		}
		if (topic == null || !topic.startsWith(MqttTopicConstants.TOPIC_PREFIX)) {
			log.warn("[IM] SUBSCRIBE rejected: illegal topic prefix, userId={}, topic={}", userId, topic);
			return false;
		}
		// 系统消息订阅：仅允许订阅自己的 userId
		if (topic.startsWith(MqttTopicConstants.SYS_TOPIC_PREFIX)) {
			String suffix = topic.substring(MqttTopicConstants.SYS_TOPIC_PREFIX.length());
			return suffix.equals(String.valueOf(userId));
		}
		// 单聊下行订阅：topic = im/p2p/{min}_{max}/out
		if (topic.startsWith(MqttTopicConstants.P2P_OUT_TOPIC_PREFIX) && topic.endsWith("/out")) {
			String middle = topic.substring(MqttTopicConstants.P2P_OUT_TOPIC_PREFIX.length(), topic.length() - 4);
			String[] parts = middle.split("_");
			if (parts.length != 2) {
				return false;
			}
			try {
				long a = Long.parseLong(parts[0]);
				long b = Long.parseLong(parts[1]);
				return userId == a || userId == b;
			} catch (NumberFormatException e) {
				return false;
			}
		}
		// 群聊下行订阅：topic = im/group/{gid}/out，权限留给 Phase 1.1
		if (topic.startsWith(MqttTopicConstants.GROUP_OUT_TOPIC_PREFIX) && topic.endsWith("/out")) {
			return true;
		}
		log.warn("[IM] SUBSCRIBE rejected: unknown topic pattern, userId={}, topic={}", userId, topic);
		return false;
	}

	@Override
	public boolean hasPermission(ChannelContext context, String clientId, String topic, MqttQoS qoS, boolean isRetain) {
		Long userId = MqttAuthInterceptor.getUserId(context);
		if (userId == null) {
			log.warn("[IM] PUBLISH rejected: missing userId, clientId={}", clientId);
			return false;
		}
		if (topic == null || !topic.startsWith(MqttTopicConstants.TOPIC_PREFIX)) {
			log.warn("[IM] PUBLISH rejected: illegal topic prefix, userId={}, topic={}", userId, topic);
			return false;
		}
		// 客户端禁止直接发布到系统消息 topic（系统消息由服务端 publishAll）
		if (topic.startsWith(MqttTopicConstants.SYS_TOPIC_PREFIX)) {
			log.warn("[IM] PUBLISH rejected: client cannot publish to sys topic, userId={}, topic={}", userId, topic);
			return false;
		}
		// 客户端禁止直接发布到下行 topic（PR-Phase 1 由服务端 MqttServerTemplate 转发）
		if (topic.startsWith(MqttTopicConstants.P2P_OUT_TOPIC_PREFIX) && topic.endsWith("/out")) {
			log.warn("[IM] PUBLISH rejected: client cannot publish to p2p out topic, userId={}, topic={}", userId, topic);
			return false;
		}
		if (topic.startsWith(MqttTopicConstants.GROUP_OUT_TOPIC_PREFIX) && topic.endsWith("/out")) {
			log.warn("[IM] PUBLISH rejected: client cannot publish to group out topic, userId={}, topic={}", userId, topic);
			return false;
		}
		// 单聊上行 topic：im/p2p/{sender}/{receiver}，sender 必须是自己
		if (topic.startsWith(MqttTopicConstants.P2P_TOPIC_PREFIX)) {
			String middle = topic.substring(MqttTopicConstants.P2P_TOPIC_PREFIX.length());
			String[] parts = middle.split("/");
			if (parts.length != 2) {
				return false;
			}
			try {
				long sender = Long.parseLong(parts[0]);
				return sender == userId;
			} catch (NumberFormatException e) {
				return false;
			}
		}
		log.warn("[IM] PUBLISH rejected: unknown topic pattern, userId={}, topic={}", userId, topic);
		return false;
	}
}
