package net.dreamlu.mica.admin.project.system.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.common.enums.EnabledEnum;
import net.dreamlu.mica.admin.framework.base.BaseModel;

/**
 * <p>
 * 部门表
 * </p>
 *
 * @author L.cm
 * @since 2020-07-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDept extends BaseModel {
	private static final long serialVersionUID = 1L;

	/**
	 * 父部门id
	 */
	@ExcelIgnore
	private Long parentId;
	/**
	 * 祖级列表
	 */
	@ExcelIgnore
	private String ancestors;
	/**
	 * 部门名称
	 */
	@ExcelProperty(value = "部门名称", index = 0)
	@ColumnWidth(20)
	private String name;
	/**
	 * 显示顺序
	 */
	@ExcelProperty(value = "排序", index = 1)
	@ColumnWidth(10)
	private Integer seq;
	/**
	 * 负责人
	 */
	@ExcelProperty(value = "负责人", index = 2)
	@ColumnWidth(12)
	private String leader;
	/**
	 * 联系电话
	 */
	@ExcelProperty(value = "电话", index = 3)
	@ColumnWidth(14)
	private String phone;
	/**
	 * 邮箱
	 */
	@ExcelProperty(value = "邮箱", index = 4)
	@ColumnWidth(22)
	private String email;
	/**
	 * 部门状态（0停用 1正常）
	 */
	@ExcelProperty(value = "状态", index = 5, converter = EnabledEnum.Converter.class)
	@ColumnWidth(10)
	private Integer enabled;
	/**
	 * 删除标志（0代表正常 1代表删除）
	 */
	@TableLogic
	@ExcelIgnore
	private Boolean delFlag;

}
