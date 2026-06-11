package net.dreamlu.mica.admin.project.system.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.project.system.entity.SysLog;
import net.dreamlu.mica.admin.project.system.pojo.LogQuery;
import net.dreamlu.mica.admin.project.system.service.ISysLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 * 系统访问记录 前端控制器
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Validated
@RestController
@RequestMapping("/api/system/logs")
@RequiredArgsConstructor
@Tag(name = "系统：日志管理")
public class SysLogController extends BaseController {
	private final ISysLogService logService;

	@ApiLog("导出数据")
	@Operation(summary = "导出数据")
	@GetMapping("/info/download")
	@ResponseExcel(name = "操作日志")
	@PreAuthorize("@sec.hasPermission('system:logs:info:export')")
	public List<SysLog> downloadInfo(LogQuery query) {
		query.setSuccessful(Boolean.TRUE);
		return logService.list(logService.getQueryWrapper(query));
	}

	@ApiLog("导出错误数据")
	@Operation(summary = "导出错误数据")
	@GetMapping("/error/download")
	@ResponseExcel(name = "错误日志")
	@PreAuthorize("@sec.hasPermission('system:logs:error:export')")
	public List<SysLog> downloadError(LogQuery query) {
		query.setSuccessful(Boolean.FALSE);
		return logService.list(logService.getQueryWrapper(query));
	}

	@GetMapping("info")
	@Operation(summary = "日志查询")
	@PreAuthorize("@sec.hasPermission('system:logs:info:list')")
	public IPage<SysLog> queryInfo(Page<SysLog> page, LogQuery query) {
		query.setSuccessful(Boolean.TRUE);
		return logService.page(page, logService.getQueryWrapper(query));
	}

	@GetMapping("user")
	@Operation(summary = "用户日志查询")
	@PreAuthorize("@sec.isAuthenticated()")
	public IPage<SysLog> queryUserLog(AuthUser authUser, Page<SysLog> page) {
		LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(SysLog::getUserId, authUser.getUserId());
		return logService.page(page, wrapper);
	}

	@GetMapping("error")
	@Operation(summary = "错误日志查询")
	@PreAuthorize("@sec.hasPermission('system:logs:error:list')")
	public IPage<SysLog> queryError(Page<SysLog> page, LogQuery query) {
		query.setSuccessful(Boolean.FALSE);
		return logService.page(page, logService.getQueryWrapper(query));
	}

	@GetMapping("error/{id}")
	@Operation(summary = "日志异常详情查询")
	@PreAuthorize("@sec.hasPermission('system:logs:error:list')")
	public SysLog queryErrorLogs(@NotNull @PathVariable Long id) {
		return logService.getById(id);
	}

	@DeleteMapping("info")
	@ApiLog("删除所有INFO日志")
	@Operation(summary = "删除所有INFO日志")
	@PreAuthorize("@sec.hasPermission('system:logs:info:del')")
	public void delAllInfo() {
		logService.removeAllByInfo();
	}

	@DeleteMapping("error")
	@ApiLog("删除所有ERROR日志")
	@Operation(summary = "删除所有ERROR日志")
	@PreAuthorize("@sec.hasPermission('system:logs:error:del')")
	public void delAllError() {
		logService.removeAllByError();
	}

}

