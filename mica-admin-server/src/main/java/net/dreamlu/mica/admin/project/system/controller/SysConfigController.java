package net.dreamlu.mica.admin.project.system.controller;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.project.system.entity.SysConfig;
import net.dreamlu.mica.admin.project.system.pojo.ConfigQuery;
import net.dreamlu.mica.admin.project.system.service.ISysConfigService;
import net.dreamlu.mica.core.validation.CreateGroup;
import net.dreamlu.mica.core.validation.UpdateGroup;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 参数配置表 前端控制器
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Validated
@RestController
@RequestMapping("/api/system/config")
@Tag(name = "系统：参数管理")
@RequiredArgsConstructor
public class SysConfigController extends BaseController {
	private final ISysConfigService configService;
	private final ObjectMapper objectMapper;

	@Operation(summary = "参数导出")
	@ApiLog("参数导出")
	@GetMapping("download")
	@ResponseExcel(name = "参数数据")
	@PreAuthorize("@sec.hasPermission('system:config:export')")
	public List<SysConfig> export(ConfigQuery query) {
		Wrapper<SysConfig> wrapper = configService.getQueryWrapper(query);
		return configService.list(wrapper);
	}

	@Operation(summary = "参数配置列表")
	@GetMapping
	@PreAuthorize("@sec.hasPermission('system:config:list')")
	public Page<SysConfig> list(Page<SysConfig> page, ConfigQuery query) {
		Wrapper<SysConfig> wrapper = configService.getQueryWrapper(query);
		return configService.page(page, wrapper);
	}

	@Operation(summary = "获取参数详细信息")
	@GetMapping("{configId}")
	@PreAuthorize("@sec.hasPermission('system:config:query')")
	public SysConfig getInfo(@PathVariable Long configId) {
		return configService.getById(configId);
	}

	@Operation(summary = "参数值查询")
	@GetMapping("configKey/{field}")
	public SysConfig getConfigKey(@PathVariable String field) {
		LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(SysConfig::getField, field);
		return configService.getOne(wrapper);
	}

	@Operation(summary = "新增参数配置")
	@ApiLog("新增参数配置")
	@PostMapping
	@PreAuthorize("@sec.hasPermission('system:config:add')")
	public void add(@Validated(CreateGroup.class) @RequestBody SysConfig entity) {
		configService.save(entity);
	}

	@Operation(summary = "修改参数配置")
	@ApiLog("修改参数配置")
	@PutMapping
	@PreAuthorize("@sec.hasPermission('system:config:edit')")
	public void edit(@Validated(UpdateGroup.class) @RequestBody SysConfig entity) {
		configService.updateById(entity);
	}

	@Operation(summary = "删除参数配置")
	@ApiLog("删除参数")
	@DeleteMapping
	@PreAuthorize("@sec.hasPermission('system:config:remove')")
	public void remove(@NotEmpty @RequestBody Set<Long> ids) {
		configService.removeByIds(ids);
	}

	@Operation(summary = "获取系统默认偏好设置（整存 JSON）")
	@GetMapping("preference/default")
	public Map<String, Object> getPreferences() throws IOException {
		String json = configService.getPreferenceJson();
		return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
	}

	@Operation(summary = "保存系统默认偏好设置（整存 JSON）")
	@ApiLog("保存系统默认偏好")
	@PutMapping("preference/default")
	@PreAuthorize("@sec.hasPermission('system:config:edit')")
	public void savePreferences(@RequestBody Map<String, Object> json) throws IOException {
		configService.savePreferenceJson(objectMapper.writeValueAsString(json));
	}

}

