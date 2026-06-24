package net.dreamlu.mica.admin.project.im.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IM 会话列表 VO。
 *
 * @author L.cm
 */
@Data
@Schema(description = "IM 会话列表 VO")
public class ConversationVO {

	/**
	 * 会话 id
	 */
	@Schema(description = "会话 id")
	private String id;

	/**
	 * 会话类型：p2p / group
	 */
	@Schema(description = "会话类型：p2p / group")
	private String type;

	/**
	 * 单聊时对端的 userId（便于前端跳转聊天对象详情页）
	 */
	@Schema(description = "单聊时对端 userId，群聊时为 null")
	private Long peerUserId;

	/**
	 * 单聊时对端的用户名/昵称
	 */
	@Schema(description = "对端展示名")
	private String peerUserName;

	/**
	 * 单聊时对端头像
	 */
	@Schema(description = "对端头像")
	private String peerAvatar;

	/**
	 * 最后一条消息 id
	 */
	@Schema(description = "最后一条消息 id")
	private Long lastMsgId;

	/**
	 * 最后一条消息时间
	 */
	@Schema(description = "最后一条消息时间")
	private LocalDateTime lastMsgTime;

	/**
	 * 最后一条消息预览（200 字内）
	 */
	@Schema(description = "最后一条消息预览")
	private String lastMsgPreview;

	/**
	 * 未读消息数
	 */
	@Schema(description = "未读消息数（Redis 权威 + DB 兜底）")
	private Integer unreadCount;

	/**
	 * 是否置顶
	 */
	@Schema(description = "是否置顶")
	private Boolean top;

	/**
	 * 是否免打扰
	 */
	@Schema(description = "是否免打扰")
	private Boolean mute;
}