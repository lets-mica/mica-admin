package net.dreamlu.mica.admin.framework.job.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.framework.job.annotation.SysJob;
import net.dreamlu.mica.admin.framework.config.SysJobProperties;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * @SysJob 启动扫描器
 * <p>
 * Spring 容器所有单例初始化完成后，遍历所有 Bean 寻找带 {@link SysJob @SysJob} 注解的方法，
 * 校验方法签名必须接收 {@link JobContext} 作为唯一参数，然后注册到 {@link SysJobRegistry}。
 * </p>
 *
 * <p>
 * 实现 {@link SmartInitializingSingleton} 是为了在 {@code @PostConstruct} 之后、容器
 * 标记为 ready 之前完成扫描，避免漏掉延迟初始化的 Bean；同时由 {@code SysJobScheduler}
 * 通过 {@code @Order} 确保注册发生在调度启动之前。
 * </p>
 *
 * @author L.cm
 */
@Slf4j
@RequiredArgsConstructor
public class SysJobScanner implements SmartInitializingSingleton {

	private final ApplicationContext applicationContext;
	private final SysJobRegistry registry;
	private final SysJobProperties properties;

	@Override
	public void afterSingletonsInstantiated() {
		if (!properties.isEnabled()) {
			log.info("[mica-admin-job] disabled by mica.job.enabled=false, skip scanning.");
			return;
		}
		Map<String, Object> beans = applicationContext.getBeansOfType(Object.class, true, false);
		int registered = 0;
		for (Object bean : beans.values()) {
			Class<?> beanClass = bean.getClass();
			Method[] methods = beanClass.getDeclaredMethods();
			for (Method method : methods) {
				SysJob annotation = method.getAnnotation(SysJob.class);
				if (annotation == null) {
					continue;
				}
				validate(annotation, method);
				method.setAccessible(true);
				registry.register(annotation.value(), bean, method, annotation);
				registered++;
			}
		}
		log.info("[mica-admin-job] scan completed, registered {} job method(s).", registered);
	}

	/**
	 * 校验任务方法签名：必须为 public、void 返回值、唯一参数为 JobContext
	 */
	private void validate(SysJob annotation, Method method) {
		if (!Modifier.isPublic(method.getModifiers())) {
			throw new IllegalArgumentException(String.format(
				"@SysJob method must be public, but %s.%s is not public.",
				method.getDeclaringClass().getName(), method.getName()));
		}
		Class<?> returnType = method.getReturnType();
		if (returnType != void.class && returnType != Void.class) {
			throw new IllegalArgumentException(String.format(
				"@SysJob method must return void, but %s.%s returns %s.",
				method.getDeclaringClass().getName(), method.getName(), returnType.getName()));
		}
		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes.length != 1 || paramTypes[0] != JobContext.class) {
			throw new IllegalArgumentException(String.format(
				"@SysJob method must accept a single JobContext parameter, but %s.%s has signature %s.",
				method.getDeclaringClass().getName(), method.getName(),
				method.toGenericString()));
		}
		if (annotation.value() == null || annotation.value().isEmpty()) {
			throw new IllegalArgumentException(String.format(
				"@SysJob value(jobKey) must not be empty, method %s.%s",
				method.getDeclaringClass().getName(), method.getName()));
		}
	}
}
