package net.dreamlu.mica.admin.framework.job.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.framework.config.SysJobProperties;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 任务调度器
 * <p>
 * 核心职责：
 * <ul>
 *     <li>启动时根据 sys_job 表初始化所有 enabled=1 的定时任务</li>
 *     <li>支持根据 jobKey 启动 / 停止 / 刷新单个任务（供 Controller 页面操作）</li>
 *     <li>支持手动执行一次（runOnce），不走调度器，不影响调度状态，专用于补数</li>
 *     <li>支持异步执行一次（runOnceAsync），立即返回，前台按钮不阻塞</li>
 * </ul>
 *
 * <p>
 * 调度线程来自 {@link ThreadPoolTaskScheduler}；任务逻辑执行由 {@link SysJobExecutor} 完成。
 * </p>
 *
 * @author L.cm
 */
@Slf4j
@RequiredArgsConstructor
public class SysJobScheduler implements SmartInitializingSingleton, DisposableBean {

	private final SysJobProperties properties;
	private final SysJobRegistry registry;
	private final SysJobExecutor executor;
	/** 提供 sys_job 表的查询能力（DB 驱动） */
	private final JobDefinitionLoader definitionLoader;

	/**
	 * 当前正在调度的任务 future 映射。
	 */
	private final Map<String, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();

	/**
	 * Spring 任务调度器
	 */
	private ThreadPoolTaskScheduler taskScheduler;

	@Override
	public void afterSingletonsInstantiated() {
		if (!properties.isEnabled()) {
			log.info("[mica-admin-job] scheduler disabled by mica.job.enabled=false.");
			return;
		}
		// 1. 初始化调度线程池
		this.taskScheduler = new ThreadPoolTaskScheduler();
		this.taskScheduler.setPoolSize(properties.getPoolSize());
		this.taskScheduler.setThreadNamePrefix(properties.getThreadNamePrefix());
		this.taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
		this.taskScheduler.setAwaitTerminationSeconds(properties.getAwaitTerminationSeconds());
		this.taskScheduler.setErrorHandler(t ->
			log.error("[mica-admin-job] scheduled task error", t));
		this.taskScheduler.initialize();
		// 2. 注册数据库中 enabled=1 的任务
		Map<String, JobDefinition> definitions = definitionLoader.loadAll();
		int scheduled = 0;
		for (Map.Entry<String, JobDefinition> entry : definitions.entrySet()) {
			JobDefinition def = entry.getValue();
			if (Boolean.TRUE.equals(def.getEnabled()) && register(def.getJobKey(), def)) {
				scheduled++;
			}
		}
		log.info("[mica-admin-job] scheduler started, scheduled {} job(s) from database.", scheduled);
	}

	@Override
	public void destroy() {
		log.info("[mica-admin-job] scheduler destroying, cancelling {} future(s).", futures.size());
		futures.values().forEach(f -> f.cancel(false));
		futures.clear();
		if (taskScheduler != null) {
			taskScheduler.destroy();
		}
	}

	/**
	 * 启动（注册）一个 jobKey 的调度
	 *
	 * @param jobKey 任务唯一标识
	 * @return 是否成功
	 */
	public boolean start(String jobKey) {
		return register(jobKey);
	}

	/**
	 * 停止（取消）一个 jobKey 的调度，不会注销 jobKey
	 *
	 * @param jobKey 任务唯一标识
	 * @return 是否成功
	 */
	public boolean stop(String jobKey) {
		ScheduledFuture<?> future = futures.remove(jobKey);
		if (future != null) {
			future.cancel(false);
			log.info("[mica-admin-job] stopped jobKey={}", jobKey);
			return true;
		}
		log.info("[mica-admin-job] stop skipped, jobKey={} not scheduled.", jobKey);
		return false;
	}

	/**
	 * 刷新任务（重新从数据库加载并与当前状态对齐：应启动则启动、应停止则停止）
	 *
	 * @param jobKey 任务唯一标识
	 * @return 是否成功
	 */
	public boolean refresh(String jobKey) {
		JobDefinition def = definitionLoader.load(jobKey);
		if (def == null) {
			return stop(jobKey);
		}
		boolean shouldRun = Boolean.TRUE.equals(def.getEnabled())
			&& def.getCron() != null
			&& !def.getCron().isEmpty();
		if (shouldRun) {
			return register(jobKey, def);
		}
		return stop(jobKey);
	}

	/**
	 * 异步执行一次：走调度线程池，不影响定时调度状态，专用于补数 / 测试。
	 *
	 * @param jobKey 任务唯一标识
	 * @param params 补数参数
	 */
	public void runOnceAsync(String jobKey, Map<String, Object> params) {
		JobDefinition def = definitionLoader.load(jobKey);
		if (def == null) {
			throw new IllegalArgumentException("jobKey=" + jobKey + " 不存在");
		}
		JobContext context = JobContext.of(toMeta(def), params);
		taskScheduler.execute(() -> {
			try {
				executor.execute(context);
			} catch (Exception e) {
				log.error("[mica-admin-job] runOnceAsync error, jobKey={}", jobKey, e);
			}
		});
	}

	/**
	 * 同步执行一次：当前线程同步调用，适用于外部脚本触发或测试。
	 *
	 * @param jobKey 任务唯一标识
	 * @param params 补数参数
	 */
	public void runOnce(String jobKey, Map<String, Object> params) {
		JobDefinition def = definitionLoader.load(jobKey);
		if (def == null) {
			throw new IllegalArgumentException("jobKey=" + jobKey + " 不存在");
		}
		executor.execute(JobContext.of(toMeta(def), params));
	}

	/**
	 * 判断任务是否正在被调度
	 *
	 * @param jobKey 任务唯一标识
	 * @return 是否在调度
	 */
	public boolean isRunning(String jobKey) {
		ScheduledFuture<?> future = futures.get(jobKey);
		return future != null && !future.isCancelled();
	}

	/**
	 * 当前被调度的任务数量
	 *
	 * @return 数量
	 */
	public int runningCount() {
		return (int) futures.values().stream().filter(f -> !f.isCancelled()).count();
	}

	// ---------------------------------------------------------------
	// 内部方法
	// ---------------------------------------------------------------

	private boolean register(String jobKey) {
		JobDefinition def = definitionLoader.load(jobKey);
		if (def == null) {
			log.warn("[mica-admin-job] register skipped, jobKey={} not in database.", jobKey);
			return false;
		}
		return register(jobKey, def);
	}

	private boolean register(String jobKey, JobDefinition def) {
		if (def.getCron() == null || def.getCron().isEmpty()) {
			log.warn("[mica-admin-job] register skipped, jobKey={} has empty cron.", jobKey);
			return false;
		}
		if (!registry.contains(jobKey)) {
			log.warn("[mica-admin-job] register skipped, jobKey={} has no @SysJob method.", jobKey);
			return false;
		}
		// 已存在则先取消
		ScheduledFuture<?> old = futures.remove(jobKey);
		if (old != null) {
			old.cancel(false);
		}
		JobMeta meta = toMeta(def);
		CronTrigger trigger = new CronTrigger(def.getCron());
		ScheduledFuture<?> future = taskScheduler.schedule(() -> {
			JobContext context = JobContext.of(meta);
			try {
				executor.execute(context);
			} catch (Exception e) {
				log.error("[mica-admin-job] scheduled run error, jobKey={}", jobKey, e);
			}
		}, trigger);
		futures.put(jobKey, future);
		log.info("[mica-admin-job] scheduled jobKey={}, cron={}", jobKey, def.getCron());
		return true;
	}

	private static JobMeta toMeta(JobDefinition def) {
		return new JobMeta(def.getJobKey(), def.getJobName(), def.getCron(), def.getDescription());
	}

	// ---------------------------------------------------------------
	// 辅助类型
	// ---------------------------------------------------------------

	/**
	 * 任务定义（数据库驱动）。由 {@link JobDefinitionLoader} 实现加载逻辑，
	 * 业务侧只需要把 sys_job 表的内容映射成该对象。
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class JobDefinition {
		private String jobKey;
		private String jobName;
		private String cron;
		private Boolean enabled;
		private String description;
	}

	/**
	 * 任务定义加载器 SPI。{@link SysJobScheduler} 通过此接口读取数据库。
	 */
	public interface JobDefinitionLoader {
		/**
		 * 加载所有任务定义（含 enabled=0 的）。返回的 map 以 jobKey 为主键。
		 *
		 * @return 全部任务定义
		 */
		Map<String, JobDefinition> loadAll();

		/**
		 * 加载单个任务定义。
		 *
		 * @param jobKey 任务唯一标识
		 * @return JobDefinition，任务不存在时返回 null
		 */
		JobDefinition load(String jobKey);
	}
}
