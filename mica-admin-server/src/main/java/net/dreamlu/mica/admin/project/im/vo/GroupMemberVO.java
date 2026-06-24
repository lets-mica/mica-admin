package net.dreamlu.mica.admin.project.im.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IM 群成员 VO。
 *
 * @author L.cm
 */
@Data
@Schema(description = "IM 群成员 VO")
public class GroupMemberVO {

	/**
	 * 群 id
	 */
	@Schema(description = "群 id")
	private Long groupId;

	/**
	 * 用户 id
	 */
	@Schema(description = "用户 id")
	private Long userId;

	/**
	 * 角色：owner / admin / member
	 */
	@Schema(description = "角色：owner / admin / member")
	private String role;

	/**
	 * 群内昵称
	 */
	@Schema(description = "群内昵称")
	private String nickname;

	/**
	 * 加入时间
	 */
	@Schema(description = "加入时间")
	private LocalDateTime joinedAt;

	/**
	 * 用户名（联表 sys_user）
	 */
	@Schema(description = "用户名")
	private String userName;

	/**
	 * 昵称（联表 sys_user 的 nickName，避免与 {@link #nickname} 重名导致 Lombok 合并）
	 */
	@Schema(description = "昵称")
	private String userNickName;

	/**
	 * 头像（联表 sys_user）
	 */
	@Schema(description = "头像")
	private String avatar;
}