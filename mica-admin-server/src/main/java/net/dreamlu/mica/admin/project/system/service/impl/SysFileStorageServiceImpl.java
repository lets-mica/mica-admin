package net.dreamlu.mica.admin.project.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.core.exception.ServiceException;
import net.dreamlu.mica.core.utils.DatePattern;
import net.dreamlu.mica.core.utils.StringUtil;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.project.system.entity.SysFileStorage;
import net.dreamlu.mica.admin.project.system.mapper.SysFileStorageMapper;
import net.dreamlu.mica.admin.project.system.pojo.FileStorageQuery;
import net.dreamlu.mica.admin.project.system.service.ISysFileStorageService;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.spring.SpringFileStorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 统一文件存储 服务实现类
 * </p>
 * <p>
 * 底层基于 <a href="https://x-file-storage.xuyanwu.cn/">x-file-storage</a> 实现，
 * 通过 {@link FileStorageService} 上传到当前配置的存储平台（默认 LOCAL 本地），
 * 再将返回的 {@link FileInfo} 元信息持久化到 sys_file_storage 表。
 * </p>
 *
 * @author L.cm
 * @since 2026-06-03
 */
@Service
@RequiredArgsConstructor
public class SysFileStorageServiceImpl extends ServiceImpl<SysFileStorageMapper, SysFileStorage> implements ISysFileStorageService {
	private final SpringFileStorageProperties fileStorageProperties;
	private final FileStorageService fileStorageService;

	@Override
	public Wrapper<SysFileStorage> getQueryWrapper(FileStorageQuery query) {
		if (query == null) {
			return new LambdaQueryWrapper<>();
		}
		LambdaQueryWrapper<SysFileStorage> wrapper = new LambdaQueryWrapper<>();
		wrapper.like(StringUtil.isNotBlank(query.getFileName()), SysFileStorage::getFileName, query.getFileName());
		wrapper.eq(StringUtil.isNotBlank(query.getFileType()), SysFileStorage::getFileType, query.getFileType());
		wrapper.eq(StringUtil.isNotBlank(query.getStorageType()), SysFileStorage::getStorageType, query.getStorageType());
		wrapper.eq(query.getUserId() != null, SysFileStorage::getUserId, query.getUserId());
		wrapper.eq(query.getIsPrivate() != null, SysFileStorage::getIsPrivate, query.getIsPrivate());
		List<LocalDateTime> createdAt = query.getCreatedAt();
		if (createdAt != null && createdAt.size() > 1) {
			wrapper.between(SysFileStorage::getCreatedAt, createdAt.get(0), createdAt.get(1));
		}
		// 默认按创建时间倒序
		wrapper.orderByDesc(SysFileStorage::getCreatedAt);
		return wrapper;
	}

	@Override
	public SysFileStorage upload(MultipartFile file, String fileType, Boolean isPrivate, String storageType, AuthUser authUser) {
		if (file == null || file.isEmpty()) {
			throw new ServiceException("上传文件不能为空");
		}
		// 平台选择：默认 local-plus；前端传入 storageType 时按其值匹配 yml 中配置的 platform。
		// 实体里 storageType 用大写（与表注释一致），x-file-storage 用小写，这里做归一。
		String defaultPlatform = fileStorageProperties.getDefaultPlatform();
		String platform = (StringUtil.isBlank(storageType) ? defaultPlatform : storageType).toLowerCase();
		String date = DatePattern.PURE_DATE_FORMAT.format(LocalDate.now());
		// 文件路径
		String filePath = Stream.of(fileType, date)
			.filter(StringUtil::isNotBlank)
			.collect(Collectors.joining("/", "", "/"));
		// x-file-storage 上传
		FileInfo info = fileStorageService.of(file)
			.setPlatform(platform)
			.setPath(filePath)
			.setOriginalFilename(file.getOriginalFilename())
			// 本地不支持 putMetadata
//			.putMetadata("fileType", fileType == null ? "" : fileType)
//			.putMetadata("isPrivate", String.valueOf(Boolean.TRUE.equals(isPrivate)))
			.upload();
		if (info == null) {
			throw new ServiceException("文件上传失败");
		}
		// 构造并落库
		SysFileStorage entity = new SysFileStorage();
		entity.setStorageType(platform.toUpperCase());
		entity.setBucket(info.getBasePath());
		entity.setFileKey(buildFileKey(info));
		entity.setMd5(info.getHashInfo() == null ? null : info.getHashInfo().getMd5());
		entity.setUrl(info.getUrl());
		entity.setFileName(info.getOriginalFilename());
		entity.setFileRealName(info.getFilename());
		entity.setSuffix(info.getExt());
		entity.setSize(info.getSize());
		entity.setMimeType(info.getContentType());
		entity.setFileType(StrUtil.isBlank(fileType) ? null : fileType);
		entity.setIsPrivate(Boolean.TRUE.equals(isPrivate));
		if (authUser != null) {
			entity.setUserId(authUser.getUserId());
		}
		this.save(entity);
		return entity;
	}

	/**
	 * 组装存储 key：x-file-storage 中 path 不以 / 结尾，filename 不含 path。
	 * 本地存储场景下，key 即 base-path 之后的相对路径，便于以后迁移或定位文件。
	 */
	private static String buildFileKey(FileInfo info) {
		String path = info.getPath();
		String filename = info.getFilename();
		if (StrUtil.isBlank(path)) {
			return filename;
		}
		// 归一化分隔符
		if (path.endsWith("/") || path.endsWith("\\")) {
			return path + filename;
		}
		return path + "/" + filename;
	}

}
