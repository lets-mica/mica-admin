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
 * IM 消息实体。
 * <p>
 * 关键设计：
 * <ul>
 *   <li>id 使用 MyBatis-Plus {@link IdType#ASSIGN_ID}（雪花算法），
 *       便于后续集群去重与跨表关联。</li>
 *   <li>顺序基准为 {@code server_received_at}，客户端不得自行重排。</li>
 *   <li>status 字段标记消息状态：0 发送中 1 已送达 2 已撤回 3 失败。</li>
 * </ul>
 *
 * @author L.cm
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_message")
public class ImMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 消息 id（雪花算法）
	 */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;

	/**
	 * 会话 id
	 */
	private String conversationId;

	/**
	 * 发送者 userId
	 */
	private Long senderId;

	/**
	 * 接收者 userId（单聊专用）
	 */
	private Long receiverId;

	/**
	 * 消息类型：text / image / file / system
	 */
	private String msgType;

	/**
	 * 消息正文
	 */
	private String content;

	/**
	 * 扩展字段（JSON 字符串）
	 */
	private String extra;

	/**
	 * 消息状态：0 发送中 1 已送达 2 已撤回 3 失败
	 */
	private Integer status;

	/**
	 * 服务端入库时间（顺序基准）
	 */
	private LocalDateTime serverReceivedAt;

	/**
	 * 撤回操作人 userId
	 */
	private Long recallBy;

	/**
	 * 撤回时间
	 */
	private LocalDateTime recallAt;

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