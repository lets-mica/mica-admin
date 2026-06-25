package net.dreamlu.mica.admin.project.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.framework.base.BaseModel;

/**
 * <p>
 * 系统消息推送记录
 * </p>
 *
 * @author L.cm
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserMessage extends BaseModel {
	private static final long serialVersionUID = 1L;

	/**
	 * 消息ID
	 */
	private Long messageId;
	/**
	 * 接收人ID
	 */
	private Long userId;
	/**
	 * 是否已读（0否 1是）
	 */
	private Boolean isRead;

}
