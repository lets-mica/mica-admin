package net.dreamlu.mica.admin.project.system.entity;

import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.framework.base.BaseModel;

/**
 * <p>
 * 系统消息
 * </p>
 *
 * @author L.cm
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessage extends BaseModel {
	private static final long serialVersionUID = 1L;

	/**
	 * 分类
	 */
	@ExcelProperty(value = "分类", index = 0)
	@ColumnWidth(15)
	private String category;
	/**
	 * 标题
	 */
	@ExcelProperty(value = "标题", index = 1)
	@ColumnWidth(28)
	private String title;
	/**
	 * 内容
	 */
	@ExcelProperty(value = "内容", index = 2)
	@ColumnWidth(40)
	private String content;
	/**
	 * 是否推送（0否 1是）
	 */
	@ExcelProperty(value = "是否推送", index = 3)
	@ColumnWidth(10)
	private String sendFlag;
	/**
	 * 排序（越大越在前）
	 */
	@ExcelProperty(value = "排序", index = 4)
	@ColumnWidth(10)
	private Integer seq;
	/**
	 * 状态（0停用,1正常）
	 */
	@ExcelProperty(value = "状态", index = 5)
	@ColumnWidth(10)
	private Boolean enabled;
	/**
	 * 备注
	 */
	@ExcelProperty(value = "备注", index = 6)
	@ColumnWidth(20)
	private String remark;

}
