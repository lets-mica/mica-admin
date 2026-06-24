package net.dreamlu.mica.admin.project.im.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.project.im.session.ImSessionRegistry;
import net.dreamlu.mica.net.core.ChannelContext;
import org.dromara.mica.mqtt.core.server.event.IMqttConnectStatusListener;
import org.springframework.stereotype.Component;

/**
 * IM 模块 MQTT 上下线状态监听。
 * <p>
 * 在客户端断开（无论主动 close 还是心跳超时）时，从 {@link ImSessionRegistry} 中清理会话，
 * 避免长时间运行后内存泄漏。后续 Phase 2 集群化时，本监听器会改为发 Redis 广播。
 *
 * @author L.cm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqttConnectStatusListener implements IMqttConnectStatusListener {

	private final ImSessionRegistry sessionRegistry;

	@Override
	public void online(ChannelContext context, String clientId, String username) {
		// 真正的"上线登记"在 MqttAuthInterceptor.authenticate() 中完成，
		// 此处仅做日志，方便对账。
		log.info("[IM] MQTT online: clientId={}, username(len)={}", clientId, username == null ? 0 : username.length());
	}

	@Override
	public void offline(ChannelContext context, String clientId, String username, String reason) {
		sessionRegistry.offline(clientId);
		log.info("[IM] MQTT offline: clientId={}, reason={}", clientId, reason);
	}
}
