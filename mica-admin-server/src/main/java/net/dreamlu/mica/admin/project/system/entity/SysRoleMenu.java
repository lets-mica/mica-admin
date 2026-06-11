package net.dreamlu.mica.admin.project.system.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 角色和菜单关联表
 * </p>
 *
 * @author L.cm
 * @since 2020-07-07
 */
@Data
public class SysRoleMenu implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 角色ID
	 */
	private Long roleId;
	/**
	 * 菜单ID
	 */
	private Long menuId;

}
