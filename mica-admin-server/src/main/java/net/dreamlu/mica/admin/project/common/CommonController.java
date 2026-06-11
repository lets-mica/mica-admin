package net.dreamlu.mica.admin.project.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.framework.base.BaseController;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用请求处理
 *
 * <p>通用文件上传底层基于 <a href="https://x-file-storage.xuyanwu.cn/">x-file-storage</a>，
 * 走 application.yml 中 dromara.x-file-storage.default-platform 配置的默认平台
 * （当前为 local 本地存储）。</p>
 *
 * @author L.cm
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common")
@Tag(name = "系统：公共接口")
public class CommonController extends BaseController {

	private final FileStorageService fileStorageService;

	@Operation(summary = "通用文件上传")
	@PostMapping("upload")
	@PreAuthorize("@sec.isAuthenticated()")
	public Map<String, Object> upload(MultipartFile file) {
		// 使用默认平台（当前为 local）上传，文件名由 x-file-storage 自动生成
		FileInfo info = fileStorageService.of(file)
			.setOriginalFilename(file.getOriginalFilename())
			.upload();
		Map<String, Object> result = new HashMap<>(2);
		result.put("fileName", file.getOriginalFilename());
		result.put("url", info.getUrl());
		return result;
	}

}
