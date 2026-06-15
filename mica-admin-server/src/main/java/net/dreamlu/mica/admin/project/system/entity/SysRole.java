package net.dreamlu.mica.admin.project.system.entity;

import org.apache.fesod.sheet.annotation.ExcelIgnore;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.common.enums.DataScopeEnum;
import net.dreamlu.mica.admin.common.enums.StatusEnum;
import net.dreamlu.mica.admin.framework.base.BaseModel;

/**
 * <p>
 * 角色信息表
 * </p>
 *
 * @author L.cm
 * @since 2020-07-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseModel {
	private static final long serialVersionUID = 1L;

	/**
	 * 角色名称
	 */
	@ExcelProperty(value = "角色名称", index = 0)
	@ColumnWidth(16)
	private String name;
	/**
	 * 角色权限字符串
	 */
	@ExcelProperty(value = "角色权限", index = 1)
	@ColumnWidth(20)
	private String title;
	/**
	 * 显示顺序
	 */
	@ExcelProperty(value = "显示顺序", index = 2)
	@ColumnWidth(12)
	private Integer seq;
	/**
	 * 数据范围（1：全部 2：部门 3：自定义）
	 */
	@ExcelProperty(value = "数据权限", index = 3, converter = DataScopeEnum.Converter.class)
	@ColumnWidth(14)
	private Integer dataScope;
	/**
	 * 角色状态（0正常 1停用）
	 */
	@ExcelProperty(value = "角色级别", index = 4, converter = StatusEnum.Converter.class)
	@ColumnWidth(12)
	@TableField(fill = FieldFill.INSERT)
	private Integer status;
	/**
	 * 删除标志（0代表存在 1代表删除）
	 */
	@TableLogic
	@ExcelIgnore
	private Boolean delFlag;
	/**
	 * 备注
	 */
	@ExcelProperty(value = "描述", index = 5)
	@ColumnWidth(24)
	private String remark;

}
