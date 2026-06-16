package net.dreamlu.mica.admin.framework.job.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库驱动定时任务标记注解
 * <p>
 * 标注在 Spring Bean 的方法上表示一个可被调度的任务。
 * 注解本身不携带任何调度参数（cron / 启停 / 参数），所有调度信息均来源于数据库 sys_job 表。
 * 被标注的方法必须接收 {@code JobContext} 作为唯一参数。
 * </p>
 *
 * <pre>{@code
 * @Component
 * public class DemoJobs {
 *
 *     @SysJob(value = "demoTask", description = "演示任务")
 *     public void run(JobContext context) {
 *         Object bizDate = context.getParams().get("bizDate");
 *     }
 * }
 * }</pre>
 *
 * @author L.cm
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface SysJob {

	/**
	 * 任务唯一标识，对应数据库 sys_job.job_key
	 */
	String value();

	/**
	 * 任务描述
	 */
	String description() default "";
}
