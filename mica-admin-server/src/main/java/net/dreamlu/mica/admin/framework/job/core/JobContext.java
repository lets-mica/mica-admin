package net.dreamlu.mica.admin.framework.job.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dreamlu.mica.admin.framework.job.annotation.SysJob;

import java.util.Collections;
import java.util.Map;

/**
 * 任务执行上下文
 * <p>
 * 由调度器或手动触发器构造，传递给被 {@link SysJob} 标注的方法。
 * 任务方法内部可通过 {@code context.getParams()} 获取页面手动执行 / 补数场景下传入的业务参数。
 * </p>
 *
 * @author L.cm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobContext {

	/** 任务元信息 */
	private JobMeta meta;

	/** 执行参数（补数 / 页面手动执行），定时调度时为空 Map */
	private Map<String, Object> params;

	/**
	 * 创建一个空参数的任务上下文
	 *
	 * @param meta 任务元信息
	 * @return JobContext
	 */
	public static JobContext of(JobMeta meta) {
		return new JobContext(meta, Collections.emptyMap());
	}

	/**
	 * 根据元信息和参数构造任务上下文（参数允许为空）
	 *
	 * @param meta   任务元信息
	 * @param params 执行参数
	 * @return JobContext
	 */
	public static JobContext of(JobMeta meta, Map<String, Object> params) {
		return new JobContext(meta, params == null ? Collections.emptyMap() : params);
	}
}
