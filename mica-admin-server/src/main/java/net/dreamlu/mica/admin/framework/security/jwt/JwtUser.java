package net.dreamlu.mica.admin.framework.security.jwt;

import lombok.Data;
import net.dreamlu.mica.admin.framework.security.pojo.DeptInfo;
import net.dreamlu.mica.admin.framework.security.pojo.PostInfo;
import net.dreamlu.mica.admin.framework.security.pojo.RoleInfo;

import java.util.List;

/**
 * jwt token 中存储的用户新
 *
 * @author L.cm
 */
@Data
public class JwtUser {

	/**
	 * 用户id
	 */
	private Long id;
	/**
	 * 登录名
	 */
	private String userName;
	/**
	 * 用户昵称
	 */
	private String nickName;
	/**
	 * 用户性别（0男 1女 2未知）
	 */
	private Integer gender;
	/**
	 * 头像地址
	 */
	private String avatar;
	/**
	 * 用户邮箱
	 */
	private String email;
	/**
	 * 手机号码
	 */
	private String phone;
	/**
	 * 是否admin
	 */
	private Boolean isAdmin;
	/**
	 * 部门信息
	 */
	private DeptInfo dept;
	/**
	 * 岗位信息
	 */
	private List<PostInfo> posts;
	/**
	 * 角色信息
	 */
	private List<RoleInfo> roles;
	/**
	 * 角色信息
	 */
	private List<String> roleList;
	/**
	 * 权限标识列表
	 */
	private List<String> permissions;

}
