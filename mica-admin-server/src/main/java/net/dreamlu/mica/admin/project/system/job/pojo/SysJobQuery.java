package net.dreamlu.mica.admin.project.system.job.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务分页查询
 *
 * @author L.cm
 */
@Data
public class SysJobQuery {

	/**
	 * 任务Key（模糊匹配）
	 */
	private String jobKey;

	/**
	 * 任务名称（模糊匹配）
	 */
	private String jobName;

	/**
	 * 是否启用
	 */
	private Boolean enabled;

	/**
	 * 创建时间区间
	 */
	private List<LocalDateTime> createTime;
}
