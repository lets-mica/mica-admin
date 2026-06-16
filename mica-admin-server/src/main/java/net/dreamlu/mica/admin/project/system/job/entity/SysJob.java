package net.dreamlu.mica.admin.project.system.job.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.common.enums.BoolYesNoEnum;
import net.dreamlu.mica.admin.framework.base.BaseModel;
import net.dreamlu.mica.admin.framework.job.core.SysJobScheduler;

/**
 * <p>
 * 数据库驱动定时任务
 * </p>
 * <p>
 * 调度参数（cron、启停、参数 schema）均存储在此表，应用启动时由
 * {@link SysJobScheduler} 读取并初始化调度。
 * </p>
 *
 * @author L.cm
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysJob extends BaseModel {
	private static final long serialVersionUID = 1L;

	/**
	 * 任务唯一标识，对应 @SysJob.value()
	 */
	@ExcelProperty(value = "任务Key", index = 0)
	@ColumnWidth(22)
	private String jobKey;

	/**
	 * 任务名称
	 */
	@ExcelProperty(value = "任务名称", index = 1)
	@ColumnWidth(22)
	private String jobName;

	/**
	 * cron 表达式
	 */
	@ExcelProperty(value = "Cron", index = 2)
	@ColumnWidth(22)
	private String cronExpression;

	/**
	 * 是否启用（0否 1是）。控制任务是否被调度。
	 */
	@ExcelProperty(value = "启用", index = 3, converter = BoolYesNoEnum.Converter.class)
	@ColumnWidth(10)
	private Boolean enabled;

	/**
	 * 参数结构定义 JSON，例如：{"bizDate":"DATE","force":"BOOLEAN"}
	 */
	@ExcelProperty(value = "参数结构", index = 4)
	@ColumnWidth(36)
	@TableField("param_schema")
	private String paramSchema;

	/**
	 * 任务描述
	 */
	@ExcelProperty(value = "描述", index = 5)
	@ColumnWidth(28)
	private String description;
}
