package net.dreamlu.mica.admin.project.system.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.project.system.entity.SysFileStorage;
import net.dreamlu.mica.admin.project.system.pojo.FileStorageQuery;
import net.dreamlu.mica.admin.project.system.service.ISysFileStorageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

/**
 * <p>
 * 统一文件存储 前端控制器
 * </p>
 *
 * @author L.cm
 * @since 2026-06-03
 */
@RestController
@RequestMapping("/api/system/file/storage")
@Tag(name = "系统-文件存储管理")
@RequiredArgsConstructor
public class SysFileStorageController extends BaseController {
	private final ISysFileStorageService fileStorageService;

	@Operation(summary = "文件存储分页列表")
	@GetMapping
	@PreAuthorize("@sec.hasPermission('system:file:list')")
	public Page<SysFileStorage> list(Page<SysFileStorage> page, FileStorageQuery query) {
		return fileStorageService.page(page, fileStorageService.getQueryWrapper(query));
	}

	@Operation(summary = "文件存储详情")
	@GetMapping("{fileId}")
	@PreAuthorize("@sec.hasPermission('system:file:query')")
	public SysFileStorage getInfo(@PathVariable Long fileId) {
		return fileStorageService.getById(fileId);
	}

	@Operation(summary = "文件上传")
	@ApiLog("文件上传")
	@PostMapping("upload")
	@PreAuthorize("@sec.hasPermission('system:file:add')")
	public SysFileStorage upload(@RequestParam MultipartFile file,
								 @RequestParam(required = false) String fileType,
								 @RequestParam(required = false) Boolean isPrivate,
								 @RequestParam(required = false) String storageType,
								 AuthUser authUser) {
		return fileStorageService.upload(file, fileType, isPrivate, storageType, authUser);
	}

	@Operation(summary = "删除文件")
	@ApiLog("删除文件")
	@DeleteMapping
	@PreAuthorize("@sec.hasPermission('system:file:remove')")
	public void remove(@NotEmpty @RequestBody Set<Long> ids) {
		fileStorageService.removeByIds(ids);
	}

}
