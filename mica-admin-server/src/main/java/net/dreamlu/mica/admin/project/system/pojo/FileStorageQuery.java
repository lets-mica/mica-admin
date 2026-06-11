package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件存储查询
 *
 * @author L.cm
 */
@Data
public class FileStorageQuery {

	/**
	 * 原始文件名（模糊）
	 */
	private String fileName;

	/**
	 * 业务类型（精确）
	 */
	private String fileType;

	/**
	 * 存储类型（精确）
	 */
	private String storageType;

	/**
	 * 上传用户ID
	 */
	private Long userId;

	/**
	 * 是否私有
	 */
	private Boolean isPrivate;

	/**
	 * 创建时间范围
	 */
	private List<LocalDateTime> createdAt;

}
