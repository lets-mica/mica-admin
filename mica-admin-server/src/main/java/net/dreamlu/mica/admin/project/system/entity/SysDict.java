package net.dreamlu.mica.admin.project.system.entity;

import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.common.enums.StatusEnum;
import net.dreamlu.mica.admin.framework.base.BaseModel;

/**
 * <p>
 * 字典表
 * </p>
 *
 * @author L.cm
 * @since 2020-07-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDict extends BaseModel {
	private static final long serialVersionUID = 1L;

	/**
	 * 字典名称
	 */
	@ExcelProperty(value = "字典名称", index = 0)
	@ColumnWidth(20)
	private String name;
	/**
	 * 字典描述
	 */
	@ExcelProperty(value = "描述", index = 1)
	@ColumnWidth(24)
	private String description;
	/**
	 * 状态（0正常 1停用）
	 */
	@ExcelProperty(value = "状态", index = 2, converter = StatusEnum.Converter.class)
	@ColumnWidth(10)
	@TableField(fill = FieldFill.INSERT)
	private Integer status;
	/**
	 * 备注
	 */
	@ExcelProperty(value = "备注", index = 3)
	@ColumnWidth(24)
	private String remark;

}
