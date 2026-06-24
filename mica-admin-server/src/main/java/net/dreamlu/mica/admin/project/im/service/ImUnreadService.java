package net.dreamlu.mica.admin.project.im.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.project.im.entity.ImConversationMember;
import net.dreamlu.mica.admin.project.im.topic.MqttTopicConstants;
import net.dreamlu.mica.redis.cache.MicaRedisCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * IM 未读消息计数服务（Redis 权威，DB 兜底）。
 * <p>
 * Redis key 设计：
 * <ul>
 *   <li>{@code im:unread:{userId}:{conversationId}} — 某用户在某会话中的未读数（String / incr）</li>
 *   <li>{@code im:unread:total:{userId}} — 某用户全局未读数（用于小红点，可选）</li>
 *   <li>TTL：30 天。长期不活跃的会话无需精确未读数，0 即可。</li>
 * </ul>
 * <p>
 * DB 中的 {@code im_conversation_member.unread_count} 仅作兜底，启动 / Redis 缺失时回灌。
 *
 * @author L.cm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImUnreadService {

	private static final String UNREAD_KEY_PREFIX = "im:unread:";
	private static final Duration TTL = Duration.ofDays(30);

	private final MicaRedisCache redisCache;

	/**
	 * 新消息到来：接收方未读数 +1。
	 */
	public void incrementUnread(Long userId, String conversationId) {
		String key = unreadKey(userId, conversationId);
		try {
			RedisTemplate<String, Object> rt = redisCache.getRedisTemplate();
			Long value = rt.opsForValue().increment(key);
			rt.expire(key, TTL);
			log.debug("[IM] unread incr: userId={}, conv={}, value={}", userId, conversationId, value);
		} catch (Exception e) {
			log.error("[IM] unread incr failed: userId={}, conv={}", userId, conversationId, e);
		}
	}

	/**
	 * 读取单会话未读数。Redis 有值用 Redis，否则回查 DB，再没有就是 0。
	 */
	public int getUnread(Long userId, String conversationId, Integer dbFallback) {
		String key = unreadKey(userId, conversationId);
		try {
			RedisTemplate<String, Object> rt = redisCache.getRedisTemplate();
			Object value = rt.opsForValue().get(key);
			if (value != null) {
				long v = (value instanceof Number) ? ((Number) value).longValue() : Long.parseLong(value.toString());
				return (int) Math.max(0, v);
			}
			// Redis 缺失 -> 用 DB 回灌（仅当 DB 有值）
			if (dbFallback != null && dbFallback > 0) {
				rt.opsForValue().set(key, dbFallback, TTL);
				return dbFallback;
			}
		} catch (Exception e) {
			log.error("[IM] unread get failed: userId={}, conv={}", userId, conversationId, e);
			if (dbFallback != null && dbFallback > 0) {
				return dbFallback;
			}
		}
		return 0;
	}

	/**
	 * 批量读取某用户多个会话的未读数。返回 map: conversationId -> unreadCount。
	 */
	public Map<String, Integer> getUnreadMap(Long userId, List<ImConversationMember> members) {
		if (members == null || members.isEmpty()) {
			return Collections.emptyMap();
		}
		Set<String> conversationIds = members.stream()
			.map(ImConversationMember::getConversationId)
			.collect(Collectors.toSet());
		if (conversationIds.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, Integer> result = new java.util.HashMap<>(conversationIds.size());
		// DB 兜底 map
		Map<String, Integer> dbMap = members.stream()
			.collect(Collectors.toMap(
				ImConversationMember::getConversationId,
				m -> m.getUnreadCount() == null ? 0 : m.getUnreadCount(),
				(a, b) -> a));
		try {
			RedisTemplate<String, Object> rt = redisCache.getRedisTemplate();
			List<String> keys = conversationIds.stream()
				.map(convId -> unreadKey(userId, convId))
				.collect(Collectors.toList());
			List<Object> values = rt.opsForValue().multiGet(new java.util.ArrayList<>(keys));
			int i = 0;
			for (String convId : conversationIds) {
				Object value = values == null ? null : values.get(i++);
				if (value != null) {
					long v = (value instanceof Number) ? ((Number) value).longValue() : Long.parseLong(value.toString());
					result.put(convId, (int) Math.max(0, v));
				} else {
					int db = dbMap.getOrDefault(convId, 0);
					result.put(convId, db);
					// 回灌 Redis
					if (db > 0) {
						try {
							rt.opsForValue().set(keys.get(i - 1), db, TTL);
						} catch (Exception ignore) {
						}
					}
				}
			}
			return result;
		} catch (Exception e) {
			log.error("[IM] unread multiGet failed: userId={}", userId, e);
			return dbMap;
		}
	}

	/**
	 * 标记单个会话已读：Redis 清零。调用方需同步更新 DB。
	 */
	public void markConversationRead(Long userId, String conversationId) {
		String key = unreadKey(userId, conversationId);
		try {
			redisCache.getRedisTemplate().delete(key);
		} catch (Exception e) {
			log.error("[IM] markConversationRead failed: userId={}, conv={}", userId, conversationId, e);
		}
	}

	/**
	 * 标记该用户全部会话已读（遍历 member 记录逐个删 key，简单可靠）。
	 */
	public void markAllRead(Long userId, Iterable<String> conversationIds) {
		if (conversationIds == null) {
			return;
		}
		List<String> keys = new java.util.ArrayList<>();
		conversationIds.forEach(convId -> keys.add(unreadKey(userId, convId)));
		if (keys.isEmpty()) {
			return;
		}
		try {
			redisCache.getRedisTemplate().delete(keys);
		} catch (Exception e) {
			log.error("[IM] markAllRead failed: userId={}", userId, e);
		}
	}

	private static String unreadKey(Long userId, String conversationId) {
		return UNREAD_KEY_PREFIX + userId + ":" + conversationId;
	}
}
