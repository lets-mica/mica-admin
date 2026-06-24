package net.dreamlu.mica.admin.project.im.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM 会话实体（单聊 + 群聊）。
 * <p>
 * id 规则：
 * <ul>
 *   <li>单聊：{@code "{min(userId)}_{max(userId)}"}</li>
 *   <li>群聊：{@code "g_" + groupId}（Phase 1.1 引入）</li>
 * </ul>
 *
 * @author L.cm
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_conversation")
public class ImConversation implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 会话 id（业务主键）
	 */
	@TableId(value = "id", type = IdType.INPUT)
	private String id;

	/**
	 * 类型：p2p / group
	 */
	private String type;

	/**
	 * 单聊参与方 A（p2p 时非空）
	 */
	@TableField("user_a")
	private Long userA;

	/**
	 * 单聊参与方 B（p2p 时非空）
	 */
	@TableField("user_b")
	private Long userB;

	/**
	 * 最后一条消息 id
	 */
	@TableField("last_msg_id")
	private Long lastMsgId;

	/**
	 * 最后一条消息时间
	 */
	@TableField("last_msg_time")
	private LocalDateTime lastMsgTime;

	/**
	 * 最后一条消息预览（200 字内）
	 */
	@TableField("last_msg_preview")
	private String lastMsgPreview;

	/**
	 * 创建人
	 */
	@TableField(value = "created_by", fill = FieldFill.INSERT)
	private String createdBy;

	/**
	 * 创建时间
	 */
	@TableField(value = "created_at", fill = FieldFill.INSERT)
	private LocalDateTime createdAt;

	/**
	 * 更新人
	 */
	@TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
	private String updatedBy;

	/**
	 * 更新时间
	 */
	@TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updatedAt;
}