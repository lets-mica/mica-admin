package net.dreamlu.mica.admin.project.system.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.project.system.entity.SysDict;
import net.dreamlu.mica.admin.project.system.pojo.DictQuery;
import net.dreamlu.mica.admin.project.system.service.ISysDictService;
import net.dreamlu.mica.core.validation.CreateGroup;
import net.dreamlu.mica.core.validation.UpdateGroup;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 字典 前端控制器
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Validated
@RestController
@RequestMapping("/api/system/dict")
@Tag(name = "系统：字典管理")
@RequiredArgsConstructor
public class SysDictController extends BaseController {
	private final ISysDictService dictService;

	@ApiLog("导出字典数据")
	@Operation(summary = "导出字典数据")
	@GetMapping("download")
	@ResponseExcel(name = "字典数据")
	@PreAuthorize("@sec.hasPermission('system:dict:export')")
	public List<SysDict> download(DictQuery query) {
		return dictService.list(dictService.getQueryWrapper(query));
	}

	@ApiLog("查询字典")
	@Operation(summary = "查询字典")
	@GetMapping
	public IPage<SysDict> query(Page<SysDict> page, DictQuery query) {
		return dictService.page(page, dictService.getQueryWrapper(query));
	}

	@ApiLog("新增字典")
	@Operation(summary = "新增字典")
	@PostMapping
	@PreAuthorize("@sec.hasPermission('system:dict:add')")
	public void create(@Validated(CreateGroup.class) @RequestBody SysDict entity) {
		dictService.save(entity);
	}

	@ApiLog("修改字典")
	@Operation(summary = "修改字典")
	@PutMapping
	@PreAuthorize("@sec.hasPermission('system:dict:edit')")
	public void update(@Validated(UpdateGroup.class) @RequestBody SysDict entity) {
		dictService.updateById(entity);
	}

	@ApiLog("删除字典")
	@Operation(summary = "删除字典")
	@DeleteMapping
	@PreAuthorize("@sec.hasPermission('system:dict:del')")
	public void delete(@NotEmpty @RequestBody Set<Long> ids) {
		dictService.deleteIfUnusedByIds(ids);
	}

}

