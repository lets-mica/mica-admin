package net.dreamlu.mica.admin.project.system.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.project.system.entity.SysDept;
import net.dreamlu.mica.admin.project.system.entity.SysUser;
import net.dreamlu.mica.admin.project.system.pojo.DeptQuery;
import net.dreamlu.mica.admin.project.system.service.ISysDeptService;
import net.dreamlu.mica.admin.project.system.service.ISysUserService;
import net.dreamlu.mica.core.result.R;
import net.dreamlu.mica.core.validation.CreateGroup;
import net.dreamlu.mica.core.validation.UpdateGroup;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 部门表 前端控制器
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Validated
@RestController
@RequestMapping("/api/system/dept")
@Tag(name = "系统：部门管理")
@RequiredArgsConstructor
public class SysDeptController extends BaseController {
	private final ISysDeptService deptService;
	private final ISysUserService userService;

	@ApiLog("导出部门数据")
	@Operation(summary = "导出部门数据")
	@GetMapping("download")
	@ResponseExcel(name = "部门数据")
	@PreAuthorize("@sec.hasPermission('system:dept:export')")
	public List<SysDept> download(DeptQuery query) {
		return deptService.list(deptService.getQueryWrapper(query));
	}

	@ApiLog("查询部门")
	@Operation(summary = "查询部门")
	@GetMapping
	@PreAuthorize("@sec.hasPermission('system:user:list', 'system:dept:list')")
	public Page<SysDept> query(DeptQuery query) {
		List<SysDept> deptList = deptService.list(deptService.getQueryWrapper(query));
		Page<SysDept> page = new Page<>(1, deptList.size());
		page.setRecords(deptList);
		return page;
	}

	@ApiLog("查询部门")
	@Operation(summary = "查询部门:根据ID获取同级与上级数据")
	@PostMapping("superior")
	@PreAuthorize("@sec.hasPermission('system:user:list') and @sec.hasPermission('system:dept:list')")
	public List<SysDept> getSuperior(@RequestBody List<Long> ids) {
		if (ids.isEmpty()) {
			return deptService.list();
		}
		List<SysDept> deptList = deptService.listByIds(ids);
		List<SysDept> superiorList = new ArrayList<>(deptList);
		deptService.getSuperior(deptList, superiorList);
		return superiorList;
	}

	@ApiLog("新增部门")
	@Operation(summary = "新增部门")
	@PostMapping
	@PreAuthorize("@sec.hasPermission('system:dept:add')")
	public void create(@Validated(CreateGroup.class) @RequestBody SysDept entity) {
		deptService.save(entity);
	}

	@ApiLog("修改部门")
	@Operation(summary = "修改部门")
	@PutMapping
	@PreAuthorize("@sec.hasPermission('system:dept:edit')")
	public void update(@Validated(UpdateGroup.class) @RequestBody SysDept entity) {
		deptService.updateById(entity);
	}

	@ApiLog("删除部门")
	@Operation(summary = "删除部门")
	@DeleteMapping
	@PreAuthorize("@sec.hasPermission('system:dept:del')")
	public void delete(@NotEmpty @RequestBody Set<Long> ids) {
		List<SysUser> userList = userService.findListByDeptIds(ids);
		if (userList != null && !userList.isEmpty()) {
			R.throwFail("存在用户岗位关系");
		}
		deptService.deleteIfUnusedByIds(ids);
	}

}

