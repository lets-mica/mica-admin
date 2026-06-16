package net.dreamlu.mica.admin.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * mica-admin-job 配置
 *
 * @author L.cm
 */
@Data
@ConfigurationProperties(prefix = "mica.job")
public class SysJobProperties {

	/**
	 * 是否启用 @SysJob 调度系统，默认 true。
	 * <p>
	 * 设为 false 时将跳过 @SysJob 方法扫描与定时调度，仅保留手动执行能力。
	 * </p>
	 */
	private boolean enabled = true;

	/**
	 * 调度线程池大小，默认 8
	 */
	private int poolSize = 8;

	/**
	 * 调度线程名前缀
	 */
	private String threadNamePrefix = "mica-job-";

	/**
	 * 等待终止时间（秒），关闭时等待正在执行的任务完成
	 */
	private int awaitTerminationSeconds = 30;

	/**
	 * 手动执行任务超时时间（秒），0 表示不限制
	 */
	private long runOnceTimeoutSeconds = 0;
}
