package net.dreamlu.mica.admin.project.im.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 创建群请求。
 *
 * @author L.cm
 */
@Data
@Schema(description = "创建群请求")
public class GroupCreateForm {

	/**
	 * 群名称。
	 */
	@NotBlank(message = "群名称不能为空")
	@Size(max = 100, message = "群名称不能超过 100 字")
	@Schema(description = "群名称", example = "产品研发组")
	private String name;

	/**
	 * 群头像 URL（可为空，前端提供默认头像）。
	 */
	@Size(max = 255, message = "头像 URL 不能超过 255 字")
	@Schema(description = "群头像 URL")
	private String avatar;

	/**
	 * 群类型。
	 */
	@Schema(description = "群类型：normal / department", example = "normal")
	private String type;

	/**
	 * 群公告（可为空）。
	 */
	@Size(max = 500, message = "群公告不能超过 500 字")
	@Schema(description = "群公告")
	private String announcement;

	/**
	 * 初始成员 userId 列表（不包含创建者）。
	 */
	@NotEmpty(message = "至少需要指定 1 个成员")
	@Schema(description = "初始成员 userId 列表（不包含创建者）")
	private List<Long> memberIds;
}
