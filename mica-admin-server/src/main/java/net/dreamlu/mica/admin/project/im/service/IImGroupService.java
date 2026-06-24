package net.dreamlu.mica.admin.project.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.im.entity.ImGroup;
import net.dreamlu.mica.admin.project.im.entity.ImGroupMember;

import java.util.List;

/**
 * IM 群服务。
 *
 * @author L.cm
 */
public interface IImGroupService extends IService<ImGroup> {

	// ------------------------ 读接口（PR-1.1.1） ------------------------

	List<ImGroup> listMyGroups(Long userId);

	ImGroup getDetail(Long groupId);

	List<ImGroupMember> listMembers(Long groupId);

	boolean isMember(Long groupId, Long userId);

	// ------------------------ 写接口（PR-1.1.2） ------------------------

	/**
	 * 创建群。
	 * <p>
	 * 流程：
	 * <ol>
	 *   <li>保存 im_group（ownerId = 创建者）</li>
	 *   <li>创建对应 im_conversation（id = "g_{groupId}"，type = "group"）</li>
	 *   <li>批量写入 im_group_member（owner + memberIds）</li>
	 *   <li>批量写入 im_conversation_member（同 im_group_member）</li>
	 *   <li>推送"邀请入群"系统消息给所有新成员</li>
	 * </ol>
	 *
	 * @param ownerId      创建者（群主）userId
	 * @param name         群名称
	 * @param avatar       群头像（可为 null）
	 * @param type         群类型（normal / department）
	 * @param announcement 群公告（可为 null）
	 * @param memberIds    初始成员 userId（不包含创建者）
	 * @return 群实体
	 */
	ImGroup createGroup(Long ownerId, String name, String avatar, String type, String announcement, List<Long> memberIds);

	/**
	 * 邀请成员。
	 * <p>
	 * 权限：群主 / 管理员可执行。批量写入 im_group_member + im_conversation_member；
	 * 已在群中的成员被静默跳过。同时向所有新成员推送"加入群"系统消息。
	 *
	 * @param groupId    群 id
	 * @param operatorId 操作人 userId（用于权限校验 + 推送事件 senderId）
	 * @param newMembers 新成员 userId
	 * @return 实际加入的成员数
	 */
	int addMembers(Long groupId, Long operatorId, List<Long> newMembers);

	/**
	 * 踢人。
	 * <p>
	 * 权限：群主 / 管理员可执行；不能踢群主。
	 * 从 im_group_member 和 im_conversation_member 中移除。
	 *
	 * @param groupId    群 id
	 * @param operatorId 操作人 userId
	 * @param targetId   被踢用户 userId
	 */
	void removeMember(Long groupId, Long operatorId, Long targetId);

	/**
	 * 主动退群。群主不能退群（必须解散群或先移交群主）。
	 *
	 * @param groupId 群 id
	 * @param userId  退群人 userId
	 */
	void quitGroup(Long groupId, Long userId);

	/**
	 * 解散群。仅群主可执行。删除 im_group、im_group_member、
	 * im_conversation（及其 member）、相关 im_message。
	 *
	 * @param groupId    群 id
	 * @param operatorId 操作人 userId（必须是群主）
	 */
	void dismissGroup(Long groupId, Long operatorId);
}
