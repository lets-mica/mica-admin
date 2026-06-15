package net.dreamlu.mica.admin.project.system.entity;

import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.common.enums.NoticeTypeEnum;
import net.dreamlu.mica.admin.common.enums.StatusEnum;
import net.dreamlu.mica.admin.framework.base.BaseModel;

/**
 * <p>
 * 通知公告表
 * </p>
 *
 * @author L.cm
 * @since 2020-07-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNotice extends BaseModel {
	private static final long serialVersionUID = 1L;

	/**
	 * 公告标题
	 */
	@ExcelProperty(value = "标题", index = 0)
	@ColumnWidth(28)
	private String title;
	/**
	 * 公告类型（1通知 2公告）
	 */
	@ExcelProperty(value = "类型", index = 1, converter = NoticeTypeEnum.Converter.class)
	@ColumnWidth(10)
	private Integer type;
	/**
	 * 公告内容
	 */
	@ExcelProperty(value = "内容", index = 2)
	@ColumnWidth(40)
	private String content;
	/**
	 * 公告状态（0正常 1关闭）
	 */
	@ExcelProperty(value = "状态", index = 3, converter = StatusEnum.Converter.class)
	@ColumnWidth(10)
	@TableField(fill = FieldFill.INSERT)
	private Integer status;
	/**
	 * 备注
	 */
	@ExcelProperty(value = "备注", index = 4)
	@ColumnWidth(20)
	private String remark;

}
