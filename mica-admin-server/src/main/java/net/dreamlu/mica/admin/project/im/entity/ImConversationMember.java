package net.dreamlu.mica.admin.project.im.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.framework.base.BaseModel;

import java.time.LocalDateTime;

/**
 * IM 会话成员实体。
 * <p>
 * 单聊场景下，会在创建会话时预插入 2 条成员记录（type=p2p）。
 * 群聊场景待 Phase 1.1。
 *
 * @author L.cm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_conversation_member")
public class ImConversationMember extends BaseModel {
	private static final long serialVersionUID = 1L;

	/**
	 * 会话 id
	 */
	private String conversationId;

	/**
	 * 用户 id
	 */
	private Long userId;

	/**
	 * 成员角色：owner / admin / member（Phase 1.1 群聊使用）
	 */
	private String role;

	/**
	 * 未读消息数（DB 缓存，权威值在 Redis）
	 */
	private Integer unreadCount;

	/**
	 * 已读到的最大消息 id
	 */
	private Long lastReadMsgId;

	/**
	 * 最后一次已读时间
	 */
	private LocalDateTime lastReadTime;

	/**
	 * 是否免打扰 0否 1是
	 */
	private Boolean mute;

	/**
	 * 是否置顶 0否 1是
	 */
	private Boolean top;
}