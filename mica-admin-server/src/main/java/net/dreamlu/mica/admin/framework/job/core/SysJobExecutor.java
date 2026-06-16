package net.dreamlu.mica.admin.framework.job.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.utils.Exceptions;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 任务执行器
 * <p>
 * 负责把 {@link JobContext} 实际投递给 {@link SysJobRegistry.JobInvocation} 中记录的方法。
 * 异常统一封装后向上抛出，由 {@link SysJobScheduler} 或调用方决定是否记录日志。
 * </p>
 *
 * @author L.cm
 */
@Slf4j
@RequiredArgsConstructor
public class SysJobExecutor {
	/**
	 * 全局执行计数器（监控用）
	 */
	private final AtomicLong executedCount = new AtomicLong();

	private final SysJobRegistry registry;

	/**
	 * 执行指定 jobKey 对应的方法
	 *
	 * @param context 任务上下文
	 * @return 任务执行后是否成功（异常时返回 false）
	 */
	public boolean execute(JobContext context) {
		if (context == null || context.getMeta() == null) {
			log.warn("[mica-admin-job] execute skipped, context or meta is null.");
			return false;
		}
		String jobKey = context.getMeta().getJobKey();
		SysJobRegistry.JobInvocation invocation = registry.get(jobKey);
		if (invocation == null) {
			log.error("[mica-admin-job] execute failed, jobKey={} not registered.", jobKey);
			return false;
		}
		executedCount.incrementAndGet();
		long start = System.nanoTime();
		try {
			invocation.getMethod().invoke(invocation.getBean(), context);
			if (log.isDebugEnabled()) {
				log.debug("[mica-admin-job] execute success, jobKey={}, cost={}ms",
					jobKey, (System.nanoTime() - start) / 1_000_000);
			}
			return true;
		} catch (IllegalAccessException | InvocationTargetException e) {
			Throwable cause = e instanceof InvocationTargetException && e.getCause() != null
				? e.getCause() : e;
			log.error("[mica-admin-job] execute error, jobKey={}, cost={}ms",
				jobKey, (System.nanoTime() - start) / 1_000_000, cause);
			throw Exceptions.unchecked(cause);
		}
	}

	/**
	 * 全局执行总数
	 *
	 * @return 累计执行次数
	 */
	public long getExecutedCount() {
		return executedCount.get();
	}
}
