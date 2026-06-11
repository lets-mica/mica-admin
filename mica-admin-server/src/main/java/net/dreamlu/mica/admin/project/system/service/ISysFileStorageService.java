package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.project.system.entity.SysFileStorage;
import net.dreamlu.mica.admin.project.system.pojo.FileStorageQuery;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 统一文件存储 服务类
 * </p>
 *
 * @author L.cm
 * @since 2026-06-03
 */
public interface ISysFileStorageService extends IService<SysFileStorage> {

	/**
	 * 构造分页查询条件
	 *
	 * @param query 查询参数
	 * @return MyBatis Plus Wrapper
	 */
	Wrapper<SysFileStorage> getQueryWrapper(FileStorageQuery query);

	/**
	 * 上传文件并落库。
	 * <p>
	 * 底层基于 <a href="https://x-file-storage.xuyanwu.cn/">x-file-storage</a>，
	 * 通过 {@code FileStorageService} 上传到 application.yml 中
	 * {@code dromara.x-file-storage.default-platform} 指定的平台（当前默认 LOCAL）。
	 * 业务类型、是否私有等元信息会写入 sys_file_storage 表。
	 *
	 * @param file        上传的 MultipartFile
	 * @param fileType    业务类型（可选）
	 * @param isPrivate   是否私有（可选）
	 * @param storageType 存储类型：LOCAL/OSS/S3/MINIO（可选，默认 LOCAL）
	 * @param authUser    当前登录用户
	 * @return 落库后的实体
	 */
	SysFileStorage upload(MultipartFile file, String fileType, Boolean isPrivate, String storageType, AuthUser authUser);

}
