package net.dreamlu.mica.admin.framework.job.loader;

import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.job.core.SysJobScheduler;
import net.dreamlu.mica.admin.project.system.entity.SysJob;
import net.dreamlu.mica.admin.project.system.service.ISysJobService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据库驱动的 {@link SysJobScheduler.JobDefinitionLoader} 实现
 * <p>
 * 从 sys_job 表读取任务定义，转换后提供给调度器。
 * </p>
 *
 * @author L.cm
 */
@Component
@RequiredArgsConstructor
public class DbJobDefinitionLoader implements SysJobScheduler.JobDefinitionLoader {

	private final ISysJobService sysJobService;

	@Override
	public Map<String, SysJobScheduler.JobDefinition> loadAll() {
		List<SysJob> all = sysJobService.list();
		if (all == null || all.isEmpty()) {
			return Collections.emptyMap();
		}
		return all.stream()
			.map(DbJobDefinitionLoader::toDefinition)
			.collect(Collectors.toMap(
				SysJobScheduler.JobDefinition::getJobKey,
				Function.identity(),
				(a, b) -> a));
	}

	@Override
	public SysJobScheduler.JobDefinition load(String jobKey) {
		SysJob job = sysJobService.getByJobKey(jobKey);
		return job == null ? null : toDefinition(job);
	}

	private static SysJobScheduler.JobDefinition toDefinition(SysJob job) {
		return new SysJobScheduler.JobDefinition(
			job.getJobKey(),
			job.getJobName(),
			job.getCronExpression(),
			Boolean.TRUE.equals(job.getEnabled()),
			job.getDescription()
		);
	}
}
