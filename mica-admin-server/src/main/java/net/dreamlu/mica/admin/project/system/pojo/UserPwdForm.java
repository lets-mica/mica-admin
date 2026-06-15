package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户密码 vo
 *
 * @author L.cm
 */
@Data
public class UserPwdForm {
	@NotBlank
	private String oldPass;
	@NotBlank
	private String newPass;
}
