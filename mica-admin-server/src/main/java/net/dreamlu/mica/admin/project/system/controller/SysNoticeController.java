package net.dreamlu.mica.admin.project.system.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.project.system.entity.SysNotice;
import net.dreamlu.mica.admin.project.system.pojo.NoticeQuery;
import net.dreamlu.mica.admin.project.system.service.ISysNoticeService;
import net.dreamlu.mica.core.validation.CreateGroup;
import net.dreamlu.mica.core.validation.UpdateGroup;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 通知公告表 前端控制器
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Validated
@RestController
@RequestMapping("/api/system/notice")
@Tag(name = "系统-通知公告管理")
@RequiredArgsConstructor
public class SysNoticeController extends BaseController {
	private final ISysNoticeService noticeService;

	@ApiLog("导出通知公告")
	@Operation(summary = "导出通知公告")
	@GetMapping("download")
	@ResponseExcel(name = "通知公告")
	@PreAuthorize("@sec.hasPermission('system:notice:export')")
	public List<SysNotice> download(NoticeQuery query) {
		return noticeService.list(noticeService.getQueryWrapper(query));
	}

	@Operation(summary = "通知公告列表")
	@GetMapping
	@PreAuthorize("@sec.hasPermission('system:notice:list')")
	public Page<SysNotice> list(Page<SysNotice> page, NoticeQuery query) {
		return noticeService.page(page, noticeService.getQueryWrapper(query));
	}

	@Operation(summary = "通知公告详情")
	@GetMapping("{noticeId}")
	@PreAuthorize("@sec.hasPermission('system:notice:query')")
	public SysNotice getInfo(@PathVariable Long noticeId) {
		return noticeService.getById(noticeId);
	}

	@Operation(summary = "新增通知公告")
	@ApiLog("新增通知公告")
	@PostMapping
	@PreAuthorize("@sec.hasPermission('system:notice:add')")
	public void add(@Validated(CreateGroup.class) @RequestBody SysNotice entity) {
		noticeService.save(entity);
	}

	@Operation(summary = "修改通知公告")
	@ApiLog("修改通知公告")
	@PutMapping
	@PreAuthorize("@sec.hasPermission('system:notice:edit')")
	public void edit(@Validated(UpdateGroup.class) @RequestBody SysNotice entity) {
		noticeService.updateById(entity);
	}

	@Operation(summary = "删除通知公告")
	@ApiLog("删除通知公告")
	@DeleteMapping
	@PreAuthorize("@sec.hasPermission('system:notice:remove')")
	public void remove(@NotEmpty @RequestBody Set<Long> ids) {
		noticeService.removeByIds(ids);
	}

}
