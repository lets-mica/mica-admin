package net.dreamlu.mica.admin.project.system.entity;

import org.apache.fesod.sheet.annotation.ExcelIgnore;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.common.enums.BoolYesNoEnum;
import net.dreamlu.mica.admin.common.enums.MenuTypeEnum;
import net.dreamlu.mica.admin.common.enums.StatusEnum;
import net.dreamlu.mica.admin.framework.base.BaseModel;
import net.dreamlu.mica.core.validation.CreateGroup;
import net.dreamlu.mica.core.validation.UpdateGroup;

import jakarta.validation.constraints.NotBlank;

/**
 * <p>
 * 菜单权限表
 * </p>
 *
 * @author L.cm
 * @since 2020-07-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMenu extends BaseModel {
	private static final long serialVersionUID = 1L;

	/**
	 * 父菜单ID
	 */
	@ExcelIgnore
	private Long parentId;
	/**
	 * 菜单标题
	 */
	@NotBlank(groups = {CreateGroup.class, UpdateGroup.class}, message = "菜单标题不能为空")
	@ExcelProperty(value = "菜单标题", index = 0)
	@ColumnWidth(20)
	private String title;
	/**
	 * 菜单名称（路由用，英文标识）
	 */
	@NotBlank(groups = {CreateGroup.class, UpdateGroup.class}, message = "菜单名称不能为空")
	@ExcelProperty(value = "菜单名称", index = 1)
	@ColumnWidth(20)
	private String name;
	/**
	 * 显示顺序
	 */
	@ExcelProperty(value = "显示顺序", index = 2)
	@ColumnWidth(12)
	private Integer seq;
	/**
	 * 路由地址
	 */
	@ExcelProperty(value = "路由地址", index = 3)
	@ColumnWidth(24)
	private String path;
	/**
	 * 权限标识
	 */
	@ExcelProperty(value = "权限标识", index = 4)
	@ColumnWidth(24)
	private String permission;
	/**
	 * 组件路径
	 */
	@ExcelProperty(value = "组件路径", index = 5)
	@ColumnWidth(30)
	private String component;
	/**
	 * 菜单图标
	 */
	@ExcelIgnore
	private String icon;
	/**
	 * 是否为外链（0否 1是）
	 */
	@ExcelProperty(value = "外链", index = 6, converter = BoolYesNoEnum.Converter.class)
	@ColumnWidth(10)
	private Boolean isFrame;
	/**
	 * 菜单类型（0目录 1菜单 2按钮）
	 */
	@ExcelProperty(value = "菜单类型", index = 7, converter = MenuTypeEnum.Converter.class)
	@ColumnWidth(12)
	private Integer type;
	/**
	 * 缓存（0否 1是）
	 */
	@ExcelProperty(value = "缓存", index = 8, converter = BoolYesNoEnum.Converter.class)
	@ColumnWidth(10)
	private Boolean cache;
	/**
	 * 显示状态（0显示，1隐藏）
	 */
	@ExcelProperty(value = "隐藏", index = 9, converter = BoolYesNoEnum.Converter.class)
	@ColumnWidth(10)
	private Boolean hidden;
	/**
	 * 菜单状态（0正常 1停用）
	 */
	@ExcelProperty(value = "状态", index = 10, converter = StatusEnum.Converter.class)
	@ColumnWidth(10)
	@TableField(fill = FieldFill.INSERT)
	private Integer status;
	/**
	 * 备注
	 */
	@ExcelProperty(value = "备注", index = 11)
	@ColumnWidth(24)
	private String remark;

}
