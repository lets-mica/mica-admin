package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户消息视图对象
 *
 * @author L.cm
 */
@Data
public class UserMessageVo {

	/**
	 * 用户消息ID
	 */
	private Long id;
	/**
	 * 消息ID
	 */
	private Long messageId;
	/**
	 * 消息标题
	 */
	private String title;
	/**
	 * 消息分类
	 */
	private String category;
	/**
	 * 消息内容
	 */
	private String content;
	/**
	 * 是否已读(0否 1是)
	 */
	private Boolean isRead;
	/**
	 * 创建时间
	 */
	private LocalDateTime createdAt;

}
