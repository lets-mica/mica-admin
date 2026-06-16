package net.dreamlu.mica.admin.framework.job.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.framework.job.annotation.SysJob;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @SysJob 注册中心
 * <p>
 * 应用启动时由 {@link SysJobScanner} 扫描所有 {@link SysJob @SysJob} 注解方法，
 * 以 {@code jobKey -> JobInvocation} 的形式注册到本类，供调度器和手动触发器查找。
 * </p>
 *
 * @author L.cm
 */
@Slf4j
public class SysJobRegistry {

	/**
	 * 任务执行包装，记录 Spring Bean 与原始 Method
	 */
	@Getter
	@AllArgsConstructor
	public static class JobInvocation {
		private final Object bean;
		private final Method method;
		private final SysJob annotation;
	}

	private final Map<String, JobInvocation> registry = new ConcurrentHashMap<>();

	/**
	 * 注册一个 @SysJob 方法
	 *
	 * @param jobKey     任务唯一标识
	 * @param bean       Spring Bean
	 * @param method     被标注的方法
	 * @param annotation @SysJob 注解
	 */
	public void register(String jobKey, Object bean, Method method, SysJob annotation) {
		JobInvocation invocation = new JobInvocation(bean, method, annotation);
		JobInvocation old = registry.putIfAbsent(jobKey, invocation);
		if (old != null) {
			log.warn("[mica-admin-job] duplicate jobKey={}, skip registration of {}.{}",
				jobKey, bean.getClass().getName(), method.getName());
		} else {
			log.info("[mica-admin-job] registered jobKey={}, method={}.{}",
				jobKey, bean.getClass().getName(), method.getName());
		}
	}

	/**
	 * 根据 jobKey 获取执行包装
	 *
	 * @param jobKey 任务唯一标识
	 * @return JobInvocation
	 */
	public JobInvocation get(String jobKey) {
		return registry.get(jobKey);
	}

	/**
	 * 获取所有已注册的任务 key
	 *
	 * @return 不可变的 jobKey 集合
	 */
	public Collection<String> jobKeys() {
		return Collections.unmodifiableCollection(registry.keySet());
	}

	/**
	 * 判断 jobKey 是否已注册
	 *
	 * @param jobKey 任务唯一标识
	 * @return 是否已注册
	 */
	public boolean contains(String jobKey) {
		return registry.containsKey(jobKey);
	}
}
