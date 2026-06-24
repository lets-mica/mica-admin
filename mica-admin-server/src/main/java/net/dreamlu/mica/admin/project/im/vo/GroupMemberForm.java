package net.dreamlu.mica.admin.project.im.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量邀请/新增群成员请求。
 *
 * @author L.cm
 */
@Data
@Schema(description = "批量邀请群成员请求")
public class GroupMemberForm {

	/**
	 * 需要邀请的 userId 列表。
	 */
	@NotEmpty(message = "成员列表不能为空")
	@Schema(description = "要邀请的 userId 列表")
	private List<Long> userIds;
}
