package net.dreamlu.mica.admin.project.system.pojo;

import org.apache.fesod.sheet.annotation.ExcelIgnore;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;
import lombok.Data;
import net.dreamlu.mica.admin.common.enums.BoolYesNoEnum;
import net.dreamlu.mica.admin.common.enums.EnabledEnum;
import net.dreamlu.mica.admin.common.enums.GenderEnum;
import net.dreamlu.mica.admin.common.enums.LockedEnum;
import net.dreamlu.mica.admin.project.system.entity.SysDept;
import net.dreamlu.mica.admin.project.system.entity.SysPost;
import net.dreamlu.mica.admin.project.system.entity.SysRole;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 去除密码的用户 vo
 *
 * @author L.cm
 */
@Data
public class UserVo implements Serializable {
	/**
	 * 主键ID
	 */
	@ExcelIgnore
	private Long id;
	/**
	 * 部门ID
	 */
	@ExcelIgnore
	private Long deptId;
	/**
	 * 部门信息 {id: 2, name: "研发部"}
	 */
	@ExcelIgnore
	private SysDept dept;
	/**
	 * 岗位信息 [{id: 12, name: "软件测试"}]
	 */
	@ExcelIgnore
	private List<SysPost> posts = new ArrayList<>();
	/**
	 * 角色信息 [{id: 2, name: "普通用户", level: 2, dataScope: "自定义"}, {id: 1, name: "超级管理员", level: 1, dataScope: "全部"}]
	 */
	@ExcelIgnore
	private List<SysRole> roles = new ArrayList<>();
	/**
	 * 用户账号
	 */
	@ExcelProperty(value = "用户账号", index = 0)
	@ColumnWidth(16)
	private String userName;
	/**
	 * 用户昵称
	 */
	@ExcelProperty(value = "用户昵称", index = 1)
	@ColumnWidth(16)
	private String nickName;
	/**
	 * 用户邮箱
	 */
	@ExcelProperty(value = "用户邮箱", index = 2)
	@ColumnWidth(24)
	private String email;
	/**
	 * 手机号码
	 */
	@ExcelProperty(value = "手机号码", index = 3)
	@ColumnWidth(16)
	private String phone;
	/**
	 * 用户性别（0男 1女 2未知）
	 */
	@ExcelProperty(value = "用户性别", index = 4, converter = GenderEnum.Converter.class)
	@ColumnWidth(12)
	private Integer gender;
	/**
	 * 头像地址
	 */
	@ExcelIgnore
	private String avatar;
	/**
	 * 用户类型（0系统用户 1管理员）
	 */
	@ExcelProperty(value = "用户类型", index = 5, converter = BoolYesNoEnum.Converter.class)
	@ColumnWidth(12)
	private Boolean isAdmin;
	/**
	 * 帐号状态（0停用 1正常）
	 */
	@ExcelProperty(value = "帐号状态", index = 6, converter = EnabledEnum.Converter.class)
	@ColumnWidth(12)
	private Boolean enabled;
	/**
	 * 登录状态（0:正常 1:锁定）
	 */
	@ExcelProperty(value = "登录状态", index = 7, converter = LockedEnum.Converter.class)
	@ColumnWidth(12)
	private Boolean locked;
	/**
	 * 删除标志（0代表存在 1代表删除）
	 */
	@ExcelIgnore
	private Boolean delFlag;
	/**
	 * 备注
	 */
	@ExcelProperty(value = "备注", index = 8)
	@ColumnWidth(24)
	private String remark;
	/**
	 * 创建者
	 */
	@ExcelProperty(value = "创建者", index = 9)
	@ColumnWidth(12)
	private String createdBy;
	/**
	 * 创建时间
	 */
	@ExcelProperty(value = "创建时间", index = 10)
	@ColumnWidth(20)
	private LocalDateTime createdAt;
	/**
	 * 更新者
	 */
	@ExcelProperty(value = "更新者", index = 11)
	@ColumnWidth(12)
	private String updatedBy;
	/**
	 * 更新时间
	 */
	@ExcelProperty(value = "更新时间", index = 12)
	@ColumnWidth(20)
	private LocalDateTime updatedAt;
}
