package net.dreamlu.mica.admin.project.im.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.framework.base.BaseModel;

/**
 * IM 群实体。
 * <p>
 * 群本身与 {@code im_conversation} 的 {@code type='group'} 记录一一对应：
 * 群 id 会被组装成 conversationId = {@code "g_" + id}（见
 * {@link net.dreamlu.mica.admin.project.im.topic.MqttTopicConstants#conversationIdForGroup}）。
 *
 * <p>群类型：
 * <ul>
 *   <li>{@code normal} - 普通群（用户自建）</li>
 *   <li>{@code department} - 部门群（与 mica-admin 的 sys_dept 关联）</li>
 * </ul>
 *
 * @author L.cm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_group")
public class ImGroup extends BaseModel {
	private static final long serialVersionUID = 1L;

	/**
	 * 群名称
	 */
	private String name;

	/**
	 * 群头像 URL
	 */
	private String avatar;

	/**
	 * 群类型：normal / department
	 */
	private String type;

	/**
	 * 群主 userId
	 */
	private Long ownerId;

	/**
	 * 关联部门 id（部门群专用）
	 */
	private Long deptId;

	/**
	 * 群公告
	 */
	private String announcement;

	/**
	 * 成员数（冗余字段，加速列表查询）
	 */
	private Integer memberCount;

	/**
	 * 最大成员数
	 */
	private Integer maxMembers;
}