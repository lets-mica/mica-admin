package net.dreamlu.mica.admin.project.system.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 用户与岗位关联表
 * </p>
 *
 * @author L.cm
 * @since 2020-07-07
 */
@Data
public class SysUserPost implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	private Long userId;
	/**
	 * 岗位ID
	 */
	private Long postId;

}
