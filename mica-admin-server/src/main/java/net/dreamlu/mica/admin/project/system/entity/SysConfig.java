package net.dreamlu.mica.admin.project.system.entity;

import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.common.enums.BoolYesNoEnum;
import net.dreamlu.mica.admin.framework.base.BaseModel;

/**
 * <p>
 * 参数配置表
 * </p>
 *
 * @author L.cm
 * @since 2020-07-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysConfig extends BaseModel {
	private static final long serialVersionUID = 1L;

	/**
	 * 参数名称
	 */
	@ExcelProperty(value = "参数名称", index = 0)
	@ColumnWidth(22)
	private String name;
	/**
	 * 参数键名
	 */
	@ExcelProperty(value = "参数键", index = 1)
	@ColumnWidth(22)
	private String field;
	/**
	 * 参数键值
	 */
	@ExcelProperty(value = "参数值", index = 2)
	@ColumnWidth(28)
	private String value;
	/**
	 * 系统内置（0否 1是 ）
	 */
	@ExcelProperty(value = "系统内置", index = 3, converter = BoolYesNoEnum.Converter.class)
	@ColumnWidth(12)
	private Boolean isSystem;
	/**
	 * 备注
	 */
	@ExcelProperty(value = "备注", index = 4)
	@ColumnWidth(22)
	private String remark;

}
