package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 邮箱模型
 *
 * @author L.cm
 */
@Data
public class EmailCodeVo {

	/**
	 * 邮箱
	 */
	@NotBlank
	private String email;

}
