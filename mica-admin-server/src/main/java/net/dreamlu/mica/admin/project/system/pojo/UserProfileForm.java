package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 个人用户信息，编辑模型
 *
 * @author L.cm
 */
@Data
public class UserProfileForm {

	/**
	 * 昵称
	 */
	@NotBlank
	private String nickName;
	/**
	 * 手机号码
	 */
	private String phone;
	/**
	 * 用户性别（0男 1女 2未知）
	 */
	@NotNull
	private Integer gender;

}
