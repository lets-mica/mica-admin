package net.dreamlu.mica.admin.project.im.push;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

/**
 * IM 模块下行 payload 统一格式。
 * <p>
 * 客户端订阅 {@code im/sys/{userId}} 后，broker 下发的 body 均为本结构。
 * 单聊 / 群聊复用 {@link #msgType} 区分。
 *
 * @author L.cm
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImPushPayload {

	/**
	 * 消息唯一 id（UUID），用于去重 / ACK。
	 */
	private String msgId;

	/**
	 * 消息类型：sys / p2p / group。
	 */
	private String msgType;

	/**
	 * 会话 id：单聊 = "{min}_{max}"；群聊 = groupId；系统消息 = null。
	 */
	private String conversationId;

	/**
	 * 发送者 userId（系统消息为 0）。
	 */
	private Long senderId;

	/**
	 * 接收者 userId（系统消息 / 群聊时为 null）。
	 */
	private Long receiverId;

	/**
	 * 群 id（仅群聊）。
	 */
	private Long groupId;

	/**
	 * 消息标题（系统消息专用，单聊/群聊通常为空）。
	 */
	private String title;

	/**
	 * 消息正文。
	 */
	private String content;

	/**
	 * 扩展字段，单聊/群聊可塞 JSON 字符串。
	 */
	private String extra;

	/**
	 * 离线兜底消息 id（对应 sys_user_message.id）。
	 */
	private Long offlineMsgId;

	/**
	 * 服务端下发时间戳，客户端不要自己生成。
	 */
	private LocalDateTime serverTime;

	// ---------- 事件类（非消息内容） ----------

	/**
	 * 事件类型：recall / read。消息 payload 为空；事件 payload 该字段非空。
	 */
	private String eventType;

	/**
	 * 事件关联的消息 id（撤回事件 = 被撤回消息 id；已读事件 = 已读到的最新消息 id）。
	 */
	private Long eventMsgId;
}