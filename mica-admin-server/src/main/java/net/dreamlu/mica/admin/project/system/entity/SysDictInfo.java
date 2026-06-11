package net.dreamlu.mica.admin.project.system.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.common.enums.BoolYesNoEnum;
import net.dreamlu.mica.admin.common.enums.StatusEnum;
import net.dreamlu.mica.admin.framework.base.BaseModel;

/**
 * <p>
 * 字典详情表
 * </p>
 *
 * @author L.cm
 * @since 2020-07-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictInfo extends BaseModel {
	private static final long serialVersionUID = 1L;

	/**
	 * 字典排序
	 */
	@ExcelProperty(value = "排序", index = 0)
	@ColumnWidth(10)
	private Integer seq;
	/**
	 * 字典标签
	 */
	@ExcelProperty(value = "字典标签", index = 1)
	@ColumnWidth(16)
	private String label;
	/**
	 * 字典键值
	 */
	@ExcelProperty(value = "字典键值", index = 2)
	@ColumnWidth(16)
	private String value;
	/**
	 * 字典类型
	 */
	@ExcelProperty(value = "字典类型", index = 3)
	@ColumnWidth(20)
	private String type;
	/**
	 * 样式属性（其他样式扩展）
	 */
	@ExcelIgnore
	private String cssClass;
	/**
	 * 表格回显样式
	 */
	@ExcelIgnore
	private String listClass;
	/**
	 * 是否默认（0否1是 ）
	 */
	@ExcelProperty(value = "是否默认", index = 4, converter = BoolYesNoEnum.Converter.class)
	@ColumnWidth(12)
	private Boolean isDefault;
	/**
	 * 状态（0正常 1停用）
	 */
	@ExcelProperty(value = "状态", index = 5, converter = StatusEnum.Converter.class)
	@ColumnWidth(10)
	@TableField(fill = FieldFill.INSERT)
	private Integer status;
	/**
	 * 备注
	 */
	@ExcelProperty(value = "备注", index = 6)
	@ColumnWidth(24)
	private String remark;

}
