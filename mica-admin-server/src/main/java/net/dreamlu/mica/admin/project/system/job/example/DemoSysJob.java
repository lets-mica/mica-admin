package net.dreamlu.mica.admin.project.system.job.example;

import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.framework.job.annotation.SysJob;
import net.dreamlu.mica.admin.framework.job.core.JobContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 演示 @SysJob 任务
 * <p>
 * 配套 sys_job 示例数据：
 * </p>
 *
 * <pre>
 * INSERT INTO sys_job (job_key, job_name, cron_expression, enabled, param_schema, description)
 * VALUES (
 *   'demoTask', '演示任务', '0/30 * * * * ?', 1,
 *   '{"bizDate":"DATE","force":"BOOLEAN"}',
 *   '演示任务：定时打印当前业务日期；支持补数（bizDate / force）'
 * );
 * </pre>
 *
 * @author L.cm
 */
@Slf4j
@Component
public class DemoSysJob {

	/**
	 * 演示任务：定时打印业务日期，支持补数。
	 * <p>
	 * 定时执行时 {@code context.getParams()} 为空 Map，
	 * 页面手动执行 / 补数时可传入 {@code bizDate} 与 {@code force} 参数。
	 * </p>
	 */
	@SysJob(value = "demoTask", description = "演示任务")
	public void run(JobContext context) {
		Object bizDateRaw = context.getParams().get("bizDate");
		Object forceRaw = context.getParams().get("force");
		String bizDate = bizDateRaw == null
			? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
			: bizDateRaw.toString();
		boolean force = forceRaw != null && Boolean.parseBoolean(forceRaw.toString());

		if (log.isInfoEnabled()) {
			log.info("[demoTask] trigger={}, bizDate={}, force={}",
				context.getParams().isEmpty() ? "scheduled" : "manual",
				bizDate, force);
		}
		// 业务逻辑……这里仅做演示打印
	}
}
