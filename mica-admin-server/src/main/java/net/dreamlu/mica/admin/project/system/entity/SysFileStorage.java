package net.dreamlu.mica.admin.project.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.framework.base.BaseModel;

/**
 * <p>
 * 统一文件存储
 * </p>
 *
 * @author L.cm
 * @since 2026-06-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFileStorage extends BaseModel {

	/**
	 * 存储类型：LOCAL / OSS / S3 / MINIO
	 */
	@TableField("storage_type")
	private String storageType;

	/**
	 * OSS bucket（本地可为空）
	 */
	private String bucket;

	/**
	 * OSS endpoint
	 */
	private String endpoint;

	/**
	 * 文件唯一 key（OSS key / 本地路径）
	 */
	@TableField("file_key")
	private String fileKey;

	/**
	 * 文件 MD5
	 */
	private String md5;

	/**
	 * 访问地址
	 */
	private String url;

	/**
	 * 原始文件名
	 */
	@TableField("file_name")
	private String fileName;

	/**
	 * 真实存储名
	 */
	@TableField("file_real_name")
	private String fileRealName;

	/**
	 * 后缀
	 */
	private String suffix;

	/**
	 * 文件大小（字节）
	 */
	private Long size;

	/**
	 * MIME 类型
	 */
	@TableField("mime_type")
	private String mimeType;

	/**
	 * 业务类型
	 */
	@TableField("file_type")
	private String fileType;

	/**
	 * 上传用户ID（关联 sys_user.id）
	 */
	@TableField("user_id")
	private Long userId;

	/**
	 * 是否私有：1=私有 0=公开
	 */
	@TableField("is_private")
	private Boolean isPrivate;

}
