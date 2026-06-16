package net.dreamlu.mica.admin.project.system.job.pojo;

import lombok.Data;
import net.dreamlu.mica.core.validation.CreateGroup;
import net.dreamlu.mica.core.validation.UpdateGroup;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;

/**
 * 任务表单（新增 / 修改）
 *
 * @author L.cm
 */
@Data
public class SysJobForm implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键（修改时必填）
	 */
	@Null(groups = CreateGroup.class)
	@NotNull(groups = UpdateGroup.class)
	private Long id;

	/**
	 * 任务Key
	 */
	@NotBlank
	private String jobKey;

	/**
	 * 任务名称
	 */
	@NotBlank
	private String jobName;

	/**
	 * cron 表达式
	 */
	private String cronExpression;

	/**
	 * 是否启用
	 */
	@NotNull
	private Boolean enabled;

	/**
	 * 参数结构 JSON
	 */
	private String paramSchema;

	/**
	 * 任务描述
	 */
	private String description;
}
