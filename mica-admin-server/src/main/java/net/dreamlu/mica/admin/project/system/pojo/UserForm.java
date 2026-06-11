package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;
import net.dreamlu.mica.core.validation.UpdateGroup;

import javax.validation.constraints.*;
import java.util.List;

/**
 * 用户请求模型
 *
 * @author L.cm
 */
@Data
public class UserForm {

	/**
	 * 账号id
	 */
	@NotNull(groups = UpdateGroup.class)
	private Long id;
	/**
	 * 用户账号
	 */
	@NotBlank
	@Size(min = 6, max = 12)
	private String userName;
	/**
	 * 用户昵称
	 */
	@NotBlank
	@Size(min = 4, max = 12)
	private String nickName;
	/**
	 * 用户邮箱
	 */
	@NotBlank
	@Email
	private String email;
	/**
	 * 手机号码
	 */
	@NotBlank
	private String phone;
	/**
	 * 用户性别（0男 1女 2未知）
	 */
	private Integer gender;
	/**
	 * 帐号状态（0停用 1正常）
	 */
	@NotNull
	private Boolean enabled;
	/**
	 * 备注
	 */
	@NotNull
	private String remark;
	/**
	 * 部门ID
	 */
	@NotNull
	private Long deptId;
	/**
	 * 角色id列表
	 */
	@NotEmpty
	private List<Long> roleIds;
	/**
	 * 岗位id列表
	 */
	@NotEmpty
	private List<Long> postIds;

}
