package net.dreamlu.mica.admin.framework.security.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色信息
 *
 * @author L.cm
 */
@Data
public class RoleInfo implements Serializable {
	/**
	 * 角色id
	 */
	private Long id;
	/**
	 * 角色名称
	 */
	private String name;
	/**
	 * 角色权限字符串
	 */
	private String title;
	/**
	 * 数据范围（0:全部,1:自定义,2:本级及子级,3:本级）
	 */
	private Integer dataScope;
}
