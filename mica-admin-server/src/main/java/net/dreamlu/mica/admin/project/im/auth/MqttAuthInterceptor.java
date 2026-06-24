package net.dreamlu.mica.admin.project.im.auth;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.framework.security.jwt.JwtTokenService;
import net.dreamlu.mica.admin.project.im.session.ImSessionRegistry;
import net.dreamlu.mica.admin.project.system.entity.SysUser;
import net.dreamlu.mica.admin.project.system.service.ISysUserService;
import net.dreamlu.mica.net.core.ChannelContext;
import org.dromara.mica.mqtt.core.server.auth.IMqttServerAuthHandler;
import org.springframework.stereotype.Component;

/**
 * IM 模块 MQTT CONNECT 鉴权：校验 clientId / username / password。
 * <p>
 * 设计要点（详见 docs/im/architecture.md §2.4）：
 * <ul>
 *   <li>username 必须是 mica-admin 颁发的 JWT，不接受明文用户名密码。</li>
 *   <li>clientId 由前端生成，格式约定为 {@code im-{userId}-{uuid}}，便于日志观测。</li>
 *   <li>password 暂留空，后续如需 mTLS / device secret 可启用。</li>
 *   <li>鉴权通过后，在 {@link ChannelContext#get()} 中存放 userId，
 *       供 {@link net.dreamlu.mica.admin.project.im.topic.MqttTopicFilter} 和
 *       {@link MqttConnectStatusListener} 使用。</li>
 * </ul>
 *
 * @author L.cm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqttAuthInterceptor implements IMqttServerAuthHandler {

	/**
	 * ChannelContext 中 userId 的 key，与本模块其他组件共享。
	 */
	public static final String USER_ID_KEY = "IM_USER_ID";

	private final JwtTokenService jwtTokenService;
	private final ImSessionRegistry sessionRegistry;
	private final ISysUserService userService;

	@Override
	public boolean authenticate(ChannelContext context, String clientId, String username, String password, String willTopic) {
		// 1. username 随机的 clientId
		if (StrUtil.isBlank(username)) {
			log.warn("[IM] MQTT CONNECT rejected: empty username, clientId={}", clientId);
			return false;
		}
		// 2. password 即 JWT token（mica-admin 的接口规范）
		String usernameFromJwt;
		try {
			usernameFromJwt = jwtTokenService.getSubject(password);
		} catch (Exception e) {
			log.warn("[IM] MQTT CONNECT rejected: invalid JWT, clientId={}, err={}", clientId, e.getMessage());
			return false;
		}
		if (usernameFromJwt == null || usernameFromJwt.isEmpty()) {
			log.warn("[IM] MQTT CONNECT rejected: empty JWT subject, clientId={}", clientId);
			return false;
		}
		// 2. 通过用户名查询用户获取 userId
		SysUser user = userService.getByUserName(usernameFromJwt);
		if (user == null) {
			log.warn("[IM] MQTT CONNECT rejected: user not found, username={}, clientId={}", usernameFromJwt, clientId);
			return false;
		}
		Long userId = user.getId();
		// 3. 写入 ChannelContext，便于订阅/发布校验直接读取
		context.set(USER_ID_KEY, userId);
		// 4. 记录用户在线会话（允许同账号多端）
		sessionRegistry.online(userId, clientId);
		log.info("[IM] MQTT CONNECT accepted: userId={}, username={}, clientId={}", userId, usernameFromJwt, clientId);
		return true;
	}

	/**
	 * 从 ChannelContext 中提取 userId，无 userId 时返回 null。
	 */
	public static Long getUserId(ChannelContext context) {
		Object value = context.get(USER_ID_KEY);
		return value instanceof Long ? (Long) value : null;
	}
}
