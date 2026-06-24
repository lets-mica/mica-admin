package net.dreamlu.mica.admin.project.im.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IM 模块用户在线会话注册表（进程内）。
 * <p>
 * 设计取舍：
 * <ul>
 *   <li>单机进程内 ConcurrentHashMap 维护，性能足够支撑单实例 5w 在线用户。</li>
 *   <li>支持多端登录（Web + App 同账号并行在线）。</li>
 *   <li>集群模式需替换为 Redis Hash 实现（PR-Phase 2 再做）。</li>
 * </ul>
 *
 * @author L.cm
 */
@Slf4j
@Component
public class ImSessionRegistry {

	/**
	 * userId -> Set<clientId>
	 */
	private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

	/**
	 * clientId -> userId，offline 时 O(1) 反查。
	 */
	private final Map<String, Long> clientUserIndex = new ConcurrentHashMap<>();

	/**
	 * 用户上线。
	 */
	public void online(Long userId, String clientId) {
		clientUserIndex.put(clientId, userId);
		userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(clientId);
		log.debug("[IM] user online: userId={}, clientId={}, onlineSize={}", userId, clientId, onlineSize(userId));
	}

	/**
	 * 用户下线。
	 */
	public void offline(String clientId) {
		Long userId = clientUserIndex.remove(clientId);
		if (userId == null) {
			return;
		}
		Set<String> sessions = userSessions.get(userId);
		if (sessions != null) {
			sessions.remove(clientId);
			if (sessions.isEmpty()) {
				userSessions.remove(userId);
			}
		}
		log.debug("[IM] user offline: userId={}, clientId={}, onlineSize={}", userId, clientId, onlineSize(userId));
	}

	/**
	 * 用户是否在线（任一端）。
	 */
	public boolean isOnline(Long userId) {
		Set<String> sessions = userSessions.get(userId);
		return sessions != null && !sessions.isEmpty();
	}

	/**
	 * 获取用户当前在线的 clientId 集合。
	 */
	public Set<String> getOnlineClients(Long userId) {
		Set<String> sessions = userSessions.get(userId);
		return sessions == null ? Collections.emptySet() : Collections.unmodifiableSet(sessions);
	}

	/**
	 * 用户当前的在线端数量。
	 */
	public int onlineSize(Long userId) {
		Set<String> sessions = userSessions.get(userId);
		return sessions == null ? 0 : sessions.size();
	}

	/**
	 * 全平台在线总端数。
	 */
	public int totalOnline() {
		return clientUserIndex.size();
	}
}