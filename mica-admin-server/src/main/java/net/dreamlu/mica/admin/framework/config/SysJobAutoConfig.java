package net.dreamlu.mica.admin.framework.config;

import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.job.core.SysJobExecutor;
import net.dreamlu.mica.admin.framework.job.core.SysJobRegistry;
import net.dreamlu.mica.admin.framework.job.core.SysJobScanner;
import net.dreamlu.mica.admin.framework.job.core.SysJobScheduler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * @SysJob 自动配置
 * <p>
 * 装配顺序：registry → executor → scanner → scheduler
 * </p>
 *
 * <p>
 * 排序原因：{@link SysJobScanner} 需要在所有单例构造完成后扫描 @SysJob 方法，
 * {@link SysJobScheduler} 需要在 {@link SysJobRegistry} 被填充之后才执行初始化。
 * </p>
 *
 * <p>
 * 显式通过 {@code @DependsOn("sysJobScanner")} 约束扫描器在调度器之前完成实例化，
 * 进一步确保 {@code preInstantiateSingletons} 阶段二者的
 * {@code SmartInitializingSingleton#afterSingletonsInstantiated} 顺序固定。
 * </p>
 *
 * @author L.cm
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@EnableConfigurationProperties(SysJobProperties.class)
public class SysJobAutoConfig {

	private final SysJobProperties sysJobProperties;

	@Bean
	public SysJobRegistry sysJobRegistry() {
		return new SysJobRegistry();
	}

	@Bean
	public SysJobExecutor sysJobExecutor(SysJobRegistry registry) {
		return new SysJobExecutor(registry);
	}

	@Bean
	public SysJobScanner sysJobScanner(ApplicationContext applicationContext, SysJobRegistry registry) {
		return new SysJobScanner(applicationContext, registry, sysJobProperties);
	}

	@Bean
	@DependsOn("sysJobScanner")
	public SysJobScheduler sysJobScheduler(SysJobRegistry registry,
										   SysJobExecutor executor,
										   SysJobScheduler.JobDefinitionLoader definitionLoader) {
		return new SysJobScheduler(sysJobProperties, registry, executor, definitionLoader);
	}
}
