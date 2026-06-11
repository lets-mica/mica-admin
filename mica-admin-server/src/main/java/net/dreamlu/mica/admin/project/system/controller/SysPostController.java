package net.dreamlu.mica.admin.project.system.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.project.system.entity.SysPost;
import net.dreamlu.mica.admin.project.system.pojo.PostQuery;
import net.dreamlu.mica.admin.project.system.service.ISysPostService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 岗位信息表 前端控制器
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Validated
@RestController
@RequestMapping("/api/system/post")
@Tag(name = "系统-岗位管理")
@RequiredArgsConstructor
public class SysPostController extends BaseController {
	private final ISysPostService postService;

	@ApiLog("导出岗位")
	@GetMapping("download")
	@ResponseExcel(name = "岗位数据")
	@PreAuthorize("@sec.hasPermission('system:post:export')")
	public List<SysPost> export(PostQuery query) {
		return postService.list(postService.getQueryWrapper(query));
	}

	@Operation(summary = "岗位列表")
	@GetMapping
	@PreAuthorize("@sec.hasPermission('system:post:list')")
	public IPage<SysPost> list(Page<SysPost> page, PostQuery query) {
		return postService.page(page, postService.getQueryWrapper(query));
	}

	@Operation(summary = "岗位详情")
	@GetMapping("{postId}")
	@PreAuthorize("@sec.hasPermission('system:post:query')")
	public SysPost getInfo(@PathVariable Long postId) {
		return postService.getById(postId);
	}

	@Operation(summary = "新增岗位")
	@ApiLog("新增岗位")
	@PostMapping
	@PreAuthorize("@sec.hasPermission('system:post:add')")
	public void add(@Validated @RequestBody SysPost entity) {
		postService.save(entity);
	}

	@Operation(summary = "修改岗位")
	@ApiLog("修改岗位")
	@PutMapping
	@PreAuthorize("@sec.hasPermission('system:post:edit')")
	public void edit(@Validated @RequestBody SysPost entity) {
		postService.updateById(entity);
	}

	@Operation(summary = "删除岗位")
	@ApiLog("删除岗位")
	@DeleteMapping
	@PreAuthorize("@sec.hasPermission('system:post:remove')")
	public void remove(@NotEmpty @RequestBody Set<Long> ids) {
		postService.deleteIfUnusedByIds(ids);
	}

	@Operation(summary = "全部岗位")
	@GetMapping("all")
	public List<SysPost> getAll() {
		return postService.list();
	}

}

