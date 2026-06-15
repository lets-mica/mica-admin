package net.dreamlu.mica.admin.project.system.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.common.enums.DataScopeEnum;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.framework.utils.PageUtil;
import net.dreamlu.mica.admin.project.system.entity.SysMenu;
import net.dreamlu.mica.admin.project.system.entity.SysRole;
import net.dreamlu.mica.admin.project.system.pojo.RoleMenuForm;
import net.dreamlu.mica.admin.project.system.pojo.RoleQuery;
import net.dreamlu.mica.admin.project.system.pojo.RoleReq;
import net.dreamlu.mica.admin.project.system.pojo.RoleVo;
import net.dreamlu.mica.admin.project.system.service.ISysMenuService;
import net.dreamlu.mica.admin.project.system.service.ISysRoleDeptService;
import net.dreamlu.mica.admin.project.system.service.ISysRoleService;
import net.dreamlu.mica.core.result.R;
import net.dreamlu.mica.core.utils.BeanUtil;
import net.dreamlu.mica.core.validation.CreateGroup;
import net.dreamlu.mica.core.validation.UpdateGroup;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 角色信息表 前端控制器
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Validated
@RestController
@RequestMapping("/api/system/roles")
@Tag(name = "系统：角色管理")
@RequiredArgsConstructor
public class SysRoleController extends BaseController {
	private final ISysRoleService roleService;
	private final ISysMenuService menuService;
	private final ISysRoleDeptService roleDeptService;

	@Operation(summary = "获取单个role")
	@GetMapping("{id}")
	@PreAuthorize("@sec.hasPermission('system:role:export')")
	public SysRole query(@PathVariable Long id) {
		return roleService.getById(id);
	}

	@ApiLog("导出角色数据")
	@Operation(summary = "导出角色数据")
	@GetMapping("download")
	@ResponseExcel(name = "角色数据")
	@PreAuthorize("@sec.hasPermission('system:role:list')")
	public List<SysRole> download(RoleQuery query) {
		return roleService.list(roleService.getQueryWrapper(query));
	}

	@Operation(summary = "返回全部的角色")
	@GetMapping("all")
	@PreAuthorize("@sec.hasPermission('system:role:list','system:user:add','system:user:edit')")
	public List<SysRole> queryAll() {
		return roleService.list();
	}

	@ApiLog("查询角色")
	@Operation(summary = "查询角色")
	@GetMapping
	@PreAuthorize("@sec.hasPermission('system:role:list')")
	public IPage<RoleVo> query(Page<SysRole> page, RoleQuery query) {
		Page<SysRole> rolePage = roleService.page(page, roleService.getQueryWrapper(query));
		List<SysRole> records = rolePage.getRecords();
		if (records.isEmpty()) {
			return PageUtil.toPage(rolePage, RoleVo.class);
		}
		return PageUtil.toPage(page, sysRole -> {
			RoleVo roleVo = new RoleVo();
			BeanUtil.copy(sysRole, roleVo);
			Integer dataScope = sysRole.getDataScope();
			boolean isDataScopeCustom = dataScope == DataScopeEnum.CUSTOM.getValue();
			if (isDataScopeCustom) {
				List<Long> deptIdList = roleDeptService.findDeptIdListByRoleId(sysRole.getId());
				roleVo.setDepts(deptIdList);
			}
			return roleVo;
		});
	}

	@ApiLog("新增角色")
	@Operation(summary = "新增角色")
	@PostMapping
	@PreAuthorize("@sec.hasPermission('system:role:add')")
	public void create(@Validated(CreateGroup.class) @RequestBody RoleReq req) {
		Integer dataScope = req.getDataScope();
		List<Long> deptList = req.getDepts();
		boolean isDataScopeCustom = dataScope == DataScopeEnum.CUSTOM.getValue();
		if (isDataScopeCustom && deptList.isEmpty()) {
			R.throwFail("缺少数据权限数据");
		}
		SysRole entity = new SysRole();
		BeanUtil.copy(req, entity);
		roleService.saveRole(entity, isDataScopeCustom, deptList);
	}

	@ApiLog("修改角色")
	@Operation(summary = "修改角色")
	@PutMapping
	@PreAuthorize("@sec.hasPermission('system:role:edit')")
	public void update(@Validated(UpdateGroup.class) @RequestBody RoleReq req) {
		Integer dataScope = req.getDataScope();
		List<Long> deptList = req.getDepts();
		boolean isDataScopeCustom = dataScope == DataScopeEnum.CUSTOM.getValue();
		if (isDataScopeCustom && deptList.isEmpty()) {
			R.throwFail("缺少数据权限数据");
		}
		SysRole entity = new SysRole();
		BeanUtil.copy(req, entity);
		roleService.updateRoleById(entity, isDataScopeCustom, deptList);
	}

	@ApiLog("获取角色菜单")
	@Operation(summary = "获取角色菜单")
	@GetMapping("{id}/menus")
	@PreAuthorize("@sec.hasPermission('system:role:menus')")
	public List<SysMenu> getMenus(@NotNull @PathVariable("id") Long id) {
		return menuService.getListByRoleIds(Collections.singletonList(id));
	}

	@ApiLog("修改角色菜单")
	@Operation(summary = "修改角色菜单")
	@PutMapping("menu")
	@PreAuthorize("@sec.hasPermission('system:role:edit')")
	public void updateMenu(@Validated @RequestBody RoleMenuForm roleMenuForm) {
		SysRole role = roleService.getById(roleMenuForm.getId());
		roleService.updateMenus(role, roleMenuForm.getMenuIds());
	}

	@ApiLog("删除角色")
	@Operation(summary = "删除角色")
	@DeleteMapping
	@PreAuthorize("@sec.hasPermission('system:role:del')")
	public void delete(@NotEmpty @RequestBody Set<Long> ids) {
		roleService.deleteIfUnusedByIds(ids);
	}

}

