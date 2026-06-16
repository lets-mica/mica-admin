package net.dreamlu.mica.admin.project.system.job.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.framework.job.core.SysJobRegistry;
import net.dreamlu.mica.admin.framework.job.core.SysJobScheduler;
import net.dreamlu.mica.admin.project.system.job.entity.SysJob;
import net.dreamlu.mica.admin.project.system.job.pojo.SysJobForm;
import net.dreamlu.mica.admin.project.system.job.pojo.SysJobQuery;
import net.dreamlu.mica.admin.project.system.job.pojo.SysJobRunOnceForm;
import net.dreamlu.mica.admin.project.system.job.service.ISysJobService;
import net.dreamlu.mica.core.validation.CreateGroup;
import net.dreamlu.mica.core.validation.UpdateGroup;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 数据库驱动定时任务 前端控制器
 * </p>
 *
 * <p>
 * 启停 / 立即执行 等行为通过 {@link SysJobScheduler} 完成，CRUD 通过 {@link ISysJobService} 完成。
 * </p>
 *
 * @author L.cm
 */
@Validated
@RestController
@RequestMapping("/api/system/job")
@Tag(name = "系统：任务调度")
@RequiredArgsConstructor
public class SysJobController extends BaseController {

	private final ISysJobService jobService;
	private final SysJobScheduler jobScheduler;
	private final SysJobRegistry jobRegistry;

	@Operation(summary = "任务导出")
	@ApiLog("任务导出")
	@GetMapping("download")
	@ResponseExcel(name = "任务数据")
	@PreAuthorize("@sec.hasPermission('system:job:export')")
	public List<SysJob> download(SysJobQuery query) {
		Wrapper<SysJob> wrapper = jobService.getQueryWrapper(query);
		return jobService.list(wrapper);
	}

	@Operation(summary = "任务列表")
	@GetMapping
	@PreAuthorize("@sec.hasPermission('system:job:list')")
	public Page<SysJob> list(Page<SysJob> page, SysJobQuery query) {
		Wrapper<SysJob> wrapper = jobService.getQueryWrapper(query);
		return jobService.page(page, wrapper);
	}

	@Operation(summary = "任务详情")
	@GetMapping("{jobId}")
	@PreAuthorize("@sec.hasPermission('system:job:query')")
	public SysJob getInfo(@PathVariable Long jobId) {
		return jobService.getById(jobId);
	}

	@Operation(summary = "新增任务")
	@ApiLog("新增任务")
	@PostMapping
	@PreAuthorize("@sec.hasPermission('system:job:add')")
	public void add(@Validated(CreateGroup.class) @RequestBody SysJobForm form) {
		SysJob entity = toEntity(form);
		jobService.save(entity);
		// 新增后刷新调度（enabled=1 时立即生效）
		jobScheduler.refresh(entity.getJobKey());
	}

	@Operation(summary = "修改任务")
	@ApiLog("修改任务")
	@PutMapping
	@PreAuthorize("@sec.hasPermission('system:job:edit')")
	public void edit(@Validated(UpdateGroup.class) @RequestBody SysJobForm form) {
		SysJob old = jobService.getById(form.getId());
		if (old == null) {
			return;
		}
		SysJob entity = toEntity(form);
		jobService.updateById(entity);
		// 刷新调度（cron / enabled 变更生效）
		jobScheduler.refresh(entity.getJobKey());
	}

	@Operation(summary = "删除任务")
	@ApiLog("删除任务")
	@DeleteMapping
	@PreAuthorize("@sec.hasPermission('system:job:remove')")
	public void remove(@NotEmpty @RequestBody Set<Long> ids) {
		List<SysJob> list = jobService.listByIds(ids);
		jobService.removeByIds(ids);
		// 删除后停掉调度
		if (list != null) {
			list.forEach(j -> jobScheduler.stop(j.getJobKey()));
		}
	}

	@Operation(summary = "启动任务调度")
	@ApiLog("启动任务调度")
	@PutMapping("start/{jobKey}")
	@PreAuthorize("@sec.hasPermission('system:job:edit')")
	public void start(@PathVariable String jobKey) {
		SysJob job = jobService.getByJobKey(jobKey);
		if (job == null) {
			return;
		}
		job.setEnabled(true);
		jobService.updateById(job);
		jobScheduler.start(jobKey);
	}

	@Operation(summary = "停止任务调度")
	@ApiLog("停止任务调度")
	@PutMapping("stop/{jobKey}")
	@PreAuthorize("@sec.hasPermission('system:job:edit')")
	public void stop(@PathVariable String jobKey) {
		SysJob job = jobService.getByJobKey(jobKey);
		if (job == null) {
			return;
		}
		job.setEnabled(false);
		jobService.updateById(job);
		jobScheduler.stop(jobKey);
	}

	@Operation(summary = "刷新任务（从 DB 重读 cron / enabled 并对齐调度状态）")
	@ApiLog("刷新任务调度")
	@PutMapping("refresh/{jobKey}")
	@PreAuthorize("@sec.hasPermission('system:job:edit')")
	public void refresh(@PathVariable String jobKey) {
		jobScheduler.refresh(jobKey);
	}

	@Operation(summary = "立即执行一次（无参）")
	@ApiLog("立即执行任务")
	@PostMapping("run-once/{jobKey}")
	@PreAuthorize("@sec.hasPermission('system:job:edit')")
	public void runOnce(@PathVariable String jobKey) {
		jobScheduler.runOnceAsync(jobKey, null);
	}

	@Operation(summary = "立即执行一次（带参，补数场景）")
	@ApiLog("带参执行任务")
	@PostMapping("run-once")
	@PreAuthorize("@sec.hasPermission('system:job:edit')")
	public void runOnceWithParams(@Validated @RequestBody SysJobRunOnceForm form) {
		jobScheduler.runOnceAsync(form.getJobKey(), form.getParams());
	}

	@Operation(summary = "校验 jobKey 是否已注册（前端配置 @SysJob 后可调用）")
	@GetMapping("registered/{jobKey}")
	public boolean registered(@PathVariable String jobKey) {
		return jobRegistry.contains(jobKey);
	}

	private static SysJob toEntity(SysJobForm form) {
		SysJob job = new SysJob();
		job.setId(form.getId());
		job.setJobKey(form.getJobKey());
		job.setJobName(form.getJobName());
		job.setCronExpression(form.getCronExpression());
		job.setEnabled(form.getEnabled());
		job.setParamSchema(form.getParamSchema());
		job.setDescription(form.getDescription());
		return job;
	}
}
