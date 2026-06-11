package net.dreamlu.mica.admin.project.system.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.core.validation.CreateGroup;
import net.dreamlu.mica.core.validation.UpdateGroup;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.project.system.entity.SysMenu;
import net.dreamlu.mica.admin.project.system.pojo.MenuQuery;
import net.dreamlu.mica.admin.project.system.service.ISysMenuService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单权限表 前端控制器
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Validated
@RestController
@RequestMapping("/api/system/menus")
@Tag(name = "系统：菜单管理")
@RequiredArgsConstructor
public class SysMenuController extends BaseController {
	private final ISysMenuService menuService;

	@ApiLog("导出菜单数据")
	@Operation(summary = "导出菜单数据")
	@GetMapping("download")
	@ResponseExcel(name = "菜单数据")
	@PreAuthorize("@sec.hasPermission('system:menu:export')")
	public List<SysMenu> download(MenuQuery query) {
		return menuService.list(menuService.getQueryWrapper(query));
	}

	@Operation(summary = "返回全部的菜单")
	@GetMapping("all")
	@PreAuthorize("@sec.hasPermission('system:menu:list', 'system:roles:list')")
	public List<SysMenu> query() {
		return menuService.list().stream()
			.sorted(Comparator
				.comparing(SysMenu::getParentId, Comparator.nullsFirst(Long::compare))
				.thenComparing(SysMenu::getSeq, Comparator.nullsFirst(Integer::compare))
				.thenComparing(SysMenu::getId))
			.collect(Collectors.toList());
	}

	@ApiLog("查询菜单")
	@Operation(summary = "查询菜单")
	@GetMapping
	@PreAuthorize("@sec.hasPermission('system:menu:list')")
	public Page<SysMenu> query(MenuQuery query) {
		List<SysMenu> menuList = menuService.list(menuService.getQueryWrapper(query));
		Page<SysMenu> page = new Page<>(1, menuList.size());
		page.setRecords(menuList);
		return page;
	}

	@ApiLog("查询菜单")
	@Operation(summary = "查询菜单:根据ID获取同级与上级数据")
	@PostMapping("superior")
	@PreAuthorize("@sec.hasPermission('system:menu:list')")
	public List<SysMenu> getSuperior(@RequestBody List<Long> ids) {
		List<SysMenu> result;
		if (ids.isEmpty()) {
			result = menuService.list();
		} else {
			List<SysMenu> deptList = menuService.listByIds(ids);
			result = new ArrayList<>(deptList);
			menuService.getSuperior(deptList, result);
		}
		// 按 parentId、seq、id 排序
		return result.stream()
			.sorted(Comparator
				.comparing(SysMenu::getParentId, Comparator.nullsFirst(Long::compare))
				.thenComparing(SysMenu::getSeq, Comparator.nullsFirst(Integer::compare))
				.thenComparing(SysMenu::getId))
			.collect(Collectors.toList());
	}

	@ApiLog("新增菜单")
	@Operation(summary = "新增菜单")
	@PostMapping
	@PreAuthorize("@sec.hasPermission('system:menu:add')")
	public void create(@Validated(CreateGroup.class) @RequestBody SysMenu entity) {
		menuService.save(entity);
	}

	@ApiLog("修改菜单")
	@Operation(summary = "修改菜单")
	@PutMapping
	@PreAuthorize("@sec.hasPermission('system:menu:edit')")
	public void update(@Validated(UpdateGroup.class) @RequestBody SysMenu entity) {
		menuService.updateById(entity);
	}

	@ApiLog("删除菜单")
	@Operation(summary = "删除菜单")
	@DeleteMapping
	@PreAuthorize("@sec.hasPermission('system:menu:del')")
	public void delete(@NotEmpty @RequestBody Set<Long> ids) {
		menuService.deleteIfUnusedByIds(ids);
	}

}

