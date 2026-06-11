package net.dreamlu.mica.admin.project.system.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.framework.base.BaseModel;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 * @author L.cm
 * @since 2020-07-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseModel {
	private static final long serialVersionUID = 1L;

	/**
	 * 部门ID
	 */
	private Long deptId;
	/**
	 * 用户账号
	 */
	private String userName;
	/**
	 * 用户昵称
	 */
	private String nickName;
	/**
	 * 用户邮箱
	 */
	private String email;
	/**
	 * 手机号码
	 */
	private String phone;
	/**
	 * 用户性别（0男 1女 2未知）
	 */
	private Integer gender;
	/**
	 * 头像地址
	 */
	private String avatar;
	/**
	 * 密码
	 */
	private String password;
	/**
	 * 用户类型（0系统用户 1管理员）
	 */
	private Boolean isAdmin;
	/**
	 * 帐号状态（0停用 1正常）
	 */
	private Boolean enabled;
	/**
	 * 登录状态（0:正常 1:锁定）
	 */
	private Boolean locked;
	/**
	 * 删除标志（0代表存在 1代表删除）
	 */
	@TableLogic
	private Boolean delFlag;
	/**
	 * 备注
	 */
	private String remark;

}
