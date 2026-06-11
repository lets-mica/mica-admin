package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * 邮箱更新 vo
 *
 * @author L.cm
 */
@Data
public class EmailUpdateVo {

	@Email
	private String email;
	@NotBlank
	private String password;
	@NotBlank
	private String code;

}
