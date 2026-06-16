package net.dreamlu.mica.admin.project.system.pojo;

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
	 * 模糊查询 jobKey,jobName,description
	 */
	private String blurry;

	/**
	 * 是否启用
	 */
	private Boolean enabled;

	/**
	 * 创建时间区间
	 */
	private List<LocalDateTime> createTime;
}
