package net.dreamlu.mica.admin.project.im.topic;

/**
 * IM 模块 MQTT Topic 常量与构造工具。
 * <p>
 * Topic 设计（详见 docs/im/architecture.md §2.3）：
 * <ul>
 *   <li>{@code im/sys/{userId}}      - 系统消息推送（PR-0.3）</li>
 *   <li>{@code im/p2p/{sender}/{receiver}} - 单聊上行（Phase 1）</li>
 *   <li>{@code im/p2p/{convId}/out} - 单聊下行（订阅）</li>
 *   <li>{@code im/group/{groupId}/out} - 群聊下行（Phase 1.1）</li>
 * </ul>
 *
 * @author L.cm
 */
public final class MqttTopicConstants {

	/**
	 * Topic 前缀：服务端下发的所有 IM 消息都以前缀开头，便于在 broker 上做 ACL。
	 */
	public static final String TOPIC_PREFIX = "im/";

	/**
	 * 系统消息 topic 模板（服务端推送给某个用户）：
	 * im/sys/{userId}
	 */
	public static final String SYS_TOPIC_PREFIX = TOPIC_PREFIX + "sys/";

	/**
	 * 单聊上行 topic（客户端发送到服务端，转发给对端）：
	 * im/p2p/{sender}/{receiver}
	 */
	public static final String P2P_TOPIC_PREFIX = TOPIC_PREFIX + "p2p/";

	/**
	 * 单聊下行 topic（服务端转发给对端，对端订阅）：
	 * im/p2p/{convId}/out
	 * 其中 {@code convId = "{min}_{max}"}（用户 id 升序拼接），保证一个会话只有一个 topic。
	 */
	public static final String P2P_OUT_TOPIC_PREFIX = TOPIC_PREFIX + "p2p/";

	/**
	 * 群聊下行 topic（服务端广播给群成员）：
	 * im/group/{groupId}/out
	 */
	public static final String GROUP_OUT_TOPIC_PREFIX = TOPIC_PREFIX + "group/";

	private MqttTopicConstants() {
	}

	/**
	 * 构造系统消息 topic。
	 *
	 * @param userId 用户 id
	 * @return topic
	 */
	public static String sysTopic(Long userId) {
		return SYS_TOPIC_PREFIX + userId;
	}

	/**
	 * 构造单聊上行 topic（客户端发送方向）。
	 */
	public static String p2pSendTopic(Long senderId, Long receiverId) {
		return P2P_TOPIC_PREFIX + senderId + "/" + receiverId;
	}

	/**
	 * 构造单聊下行 topic（订阅方向），convId = min(sender,receiver) + "_" + max。
	 */
	public static String p2pOutTopic(Long userA, Long userB) {
		String convId = conversationId(userA, userB);
		return P2P_OUT_TOPIC_PREFIX + convId + "/out";
	}

	/**
	 * 计算单聊会话 id。
	 */
	public static String conversationId(Long userA, Long userB) {
		long min = Math.min(userA, userB);
		long max = Math.max(userA, userB);
		return min + "_" + max;
	}

	/**
	 * 构造群聊下行 topic。
	 */
	public static String groupOutTopic(Long groupId) {
		return GROUP_OUT_TOPIC_PREFIX + groupId + "/out";
	}

	/**
	 * 通过 conversationId 直接构造单聊下行 topic（无需拆出 min/max）。
	 * <p>
	 * 约定：{@code conversationId = "{minUserId}_{maxUserId}"}，这里直接拼接后缀即可。
	 */
	public static String p2pOutTopicByConv(String conversationId) {
		return P2P_OUT_TOPIC_PREFIX + conversationId + "/out";
	}

	/**
	 * 群会话 id 统一前缀：{@code g_{groupId}}，便于在 {@code im_conversation.id} 中与单聊区分。
	 */
	public static final String GROUP_CONVERSATION_PREFIX = "g_";

	/**
	 * 由群 id 推导对应的 conversationId（{@code im_conversation.id} 使用的字符串）。
	 *
	 * @param groupId 群 id
	 * @return conversationId（{@code "g_" + groupId}）
	 */
	public static String conversationIdForGroup(Long groupId) {
		return GROUP_CONVERSATION_PREFIX + groupId;
	}

	/**
	 * 由 conversationId 反解群 id。{@code conversationId} 必须以 {@link #GROUP_CONVERSATION_PREFIX} 开头，
	 * 否则抛出 {@link IllegalArgumentException}。
	 *
	 * @param conversationId 会话 id
	 * @return 群 id
	 */
	public static Long groupIdFromConversation(String conversationId) {
		if (conversationId == null || !conversationId.startsWith(GROUP_CONVERSATION_PREFIX)) {
			throw new IllegalArgumentException("not a group conversationId: " + conversationId);
		}
		return Long.parseLong(conversationId.substring(GROUP_CONVERSATION_PREFIX.length()));
	}

	/**
	 * 判断 conversationId 是否是群会话。
	 */
	public static boolean isGroupConversation(String conversationId) {
		return conversationId != null && conversationId.startsWith(GROUP_CONVERSATION_PREFIX);
	}
}