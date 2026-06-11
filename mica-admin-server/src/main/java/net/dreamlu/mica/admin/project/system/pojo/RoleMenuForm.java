package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 角色菜单请求模型
 *
 * @author l.cm
 */
@Data
public class RoleMenuForm {

	/**
	 * 角色id
	 */
	@NotNull
	private Long id;
	/**
	 * 菜单id列表
	 */
	@NotEmpty
	private List<Long> menuIds;

}
