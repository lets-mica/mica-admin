package net.dreamlu.mica.admin.project.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.project.system.entity.SysDictInfo;
import net.dreamlu.mica.admin.project.system.pojo.DictInfoQuery;
import net.dreamlu.mica.admin.project.system.service.ISysDictInfoService;
import net.dreamlu.mica.core.validation.CreateGroup;
import net.dreamlu.mica.core.validation.UpdateGroup;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 字典详情表 前端控制器
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Validated
@RestController
@RequestMapping("/api/system/dict/info")
@Tag(name = "系统：字典详情管理")
@RequiredArgsConstructor
public class SysDictInfoController extends BaseController {
	private final ISysDictInfoService dictInfoService;

	@ApiLog("导出字典详情")
	@Operation(summary = "导出字典详情")
	@GetMapping("download")
	@ResponseExcel(name = "字典详情")
	@PreAuthorize("@sec.hasPermission('system:dict:export')")
	public List<SysDictInfo> download(DictInfoQuery query) {
		return dictInfoService.list(dictInfoService.getQueryWrapper(query));
	}

	@ApiLog("查询字典详情")
	@Operation(summary = "查询字典详情")
	@GetMapping("all")
	@PreAuthorize("@sec.hasPermission('system:dict:list')")
	public List<SysDictInfo> queryAll() {
		return dictInfoService.list();
	}

	@ApiLog("查询字典详情")
	@Operation(summary = "查询字典详情")
	@GetMapping
	@PreAuthorize("@sec.hasPermission('system:dict:list')")
	public IPage<SysDictInfo> query(Page<SysDictInfo> page, DictInfoQuery query) {
		return dictInfoService.page(page, dictInfoService.getQueryWrapper(query));
	}

	@ApiLog("查询多个字典详情")
	@Operation(summary = "查询多个字典详情")
	@GetMapping("map")
	public Map<String, List<SysDictInfo>> getDictMaps(@RequestParam List<String> names) {
		if (names == null || names.isEmpty()) {
			return Collections.emptyMap();
		}
		LambdaQueryWrapper<SysDictInfo> wrapper = Wrappers.lambdaQuery();
		wrapper.in(SysDictInfo::getType, names);
		return dictInfoService.list(wrapper).stream()
			.collect(Collectors.groupingBy(SysDictInfo::getType));
	}

	@ApiLog("新增字典详情")
	@Operation(summary = "新增字典详情")
	@PostMapping
	@PreAuthorize("@sec.hasPermission('system:dict:add')")
	public void create(@Validated(CreateGroup.class) @RequestBody SysDictInfo entity) {
		dictInfoService.save(entity);
	}

	@ApiLog("修改字典详情")
	@Operation(summary = "修改字典详情")
	@PutMapping
	@PreAuthorize("@sec.hasPermission('system:dict:edit')")
	public void update(@Validated(UpdateGroup.class) @RequestBody SysDictInfo entity) {
		dictInfoService.updateById(entity);
	}

	@ApiLog("删除字典详情")
	@Operation(summary = "删除字典详情")
	@DeleteMapping("{id}")
	@PreAuthorize("@sec.hasPermission('system:dict:del')")
	public void delete(@NotNull @PathVariable Long id) {
		dictInfoService.removeById(id);
	}

}

