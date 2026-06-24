package net.dreamlu.mica.admin.project.im.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IM 消息展示 VO。
 *
 * @author L.cm
 */
@Data
@Schema(description = "IM 消息 VO")
public class MessageVO {

	/**
	 * 消息 id
	 */
	@Schema(description = "消息 id")
	private Long id;

	/**
	 * 会话 id
	 */
	@Schema(description = "会话 id")
	private String conversationId;

	/**
	 * 发送者 userId
	 */
	@Schema(description = "发送者 userId")
	private Long senderId;

	/**
	 * 接收者 userId（单聊）
	 */
	@Schema(description = "接收者 userId")
	private Long receiverId;

	/**
	 * 消息类型：text / image / file / system
	 */
	@Schema(description = "消息类型：text / image / file / system")
	private String msgType;

	/**
	 * 消息正文
	 */
	@Schema(description = "消息正文")
	private String content;

	/**
	 * 扩展字段（JSON 字符串）
	 */
	@Schema(description = "扩展字段（JSON 字符串）")
	private String extra;

	/**
	 * 消息状态：0 发送中 1 已送达 2 已撤回 3 失败
	 */
	@Schema(description = "消息状态：0 发送中 1 已送达 2 已撤回 3 失败")
	private Integer status;

	/**
	 * 服务端入库时间
	 */
	@Schema(description = "服务端入库时间")
	private LocalDateTime serverReceivedAt;

	/**
	 * 撤回人 userId
	 */
	@Schema(description = "撤回人 userId")
	private Long recallBy;

	/**
	 * 撤回时间
	 */
	@Schema(description = "撤回时间")
	private LocalDateTime recallAt;
}