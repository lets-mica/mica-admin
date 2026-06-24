package net.dreamlu.mica.admin.project.im.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IM 群列表 VO。
 *
 * @author L.cm
 */
@Data
@Schema(description = "IM 群列表 VO")
public class GroupVO {

	/**
	 * 群 id
	 */
	@Schema(description = "群 id")
	private Long id;

	/**
	 * 群名称
	 */
	@Schema(description = "群名称")
	private String name;

	/**
	 * 群头像 URL
	 */
	@Schema(description = "群头像")
	private String avatar;

	/**
	 * 群类型：normal / department
	 */
	@Schema(description = "群类型：normal / department")
	private String type;

	/**
	 * 群主 userId
	 */
	@Schema(description = "群主 userId")
	private Long ownerId;

	/**
	 * 群主展示名（昵称）
	 */
	@Schema(description = "群主展示名")
	private String ownerName;

	/**
	 * 关联部门 id（部门群专用）
	 */
	@Schema(description = "关联部门 id（部门群专用）")
	private Long deptId;

	/**
	 * 群公告
	 */
	@Schema(description = "群公告")
	private String announcement;

	/**
	 * 成员数
	 */
	@Schema(description = "成员数")
	private Integer memberCount;

	/**
	 * 最大成员数
	 */
	@Schema(description = "最大成员数")
	private Integer maxMembers;

	/**
	 * 当前用户在本群的角色：owner / admin / member
	 */
	@Schema(description = "当前用户在本群的角色")
	private String role;

	/**
	 * 当前用户的群内昵称
	 */
	@Schema(description = "当前用户的群内昵称")
	private String nickname;

	/**
	 * 群最后消息预览（PR-1.1.3 后才会填充）
	 */
	@Schema(description = "群最后消息预览")
	private String lastMsgPreview;

	/**
	 * 群最后消息时间（PR-1.1.3 后才会填充）
	 */
	@Schema(description = "群最后消息时间")
	private LocalDateTime lastMsgTime;
}