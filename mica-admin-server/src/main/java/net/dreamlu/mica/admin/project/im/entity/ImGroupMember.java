package net.dreamlu.mica.admin.project.im.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IM 群成员实体。
 * <p>
 * 与 {@code im_conversation_member} 冗余存储，便于群管理操作（踢人 / 解散 / 角色设置）；
 * 两份数据在写入时需要保持一致（群创建、加人、退人三个入口）。
 *
 * @author L.cm
 */
@Data
@TableName("im_group_member")
public class ImGroupMember implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 群 id
	 */
	private Long groupId;

	/**
	 * 用户 id
	 */
	private Long userId;

	/**
	 * 角色：owner / admin / member
	 */
	private String role;

	/**
	 * 群内昵称（可选）
	 */
	private String nickname;

	/**
	 * 加入时间
	 */
	private LocalDateTime joinedAt;
}