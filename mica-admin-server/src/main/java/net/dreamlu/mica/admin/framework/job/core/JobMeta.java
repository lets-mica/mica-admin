package net.dreamlu.mica.admin.framework.job.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务元信息
 *
 * @author L.cm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobMeta {

	/** 任务唯一标识 */
	private String jobKey;

	/** 任务名称 */
	private String jobName;

	/** cron 表达式 */
	private String cron;

	/** 任务描述 */
	private String description;
}
