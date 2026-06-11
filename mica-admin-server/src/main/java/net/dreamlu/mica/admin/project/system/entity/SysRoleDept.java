package net.dreamlu.mica.admin.project.system.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 角色和部门关联表
 * </p>
 *
 * @author L.cm
 * @since 2020-07-07
 */
@Data
public class SysRoleDept implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 角色ID
	 */
	private Long roleId;
	/**
	 * 部门ID
	 */
	private Long deptId;

}
