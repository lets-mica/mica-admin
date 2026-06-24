package net.dreamlu.mica.admin.project.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.admin.project.im.common.ImApiCode;
import net.dreamlu.mica.admin.project.im.entity.ImConversation;
import net.dreamlu.mica.admin.project.im.entity.ImConversationMember;
import net.dreamlu.mica.admin.project.im.entity.ImGroup;
import net.dreamlu.mica.admin.project.im.entity.ImGroupMember;
import net.dreamlu.mica.admin.project.im.mapper.ImConversationMapper;
import net.dreamlu.mica.admin.project.im.mapper.ImGroupMapper;
import net.dreamlu.mica.admin.project.im.mapper.ImGroupMemberMapper;
import net.dreamlu.mica.admin.project.im.service.IImConversationMemberService;
import net.dreamlu.mica.admin.project.im.service.IImGroupMemberService;
import net.dreamlu.mica.admin.project.im.service.IImGroupService;
import net.dreamlu.mica.admin.project.im.push.ImPushService;
import net.dreamlu.mica.admin.project.im.push.ImPushPayload;
import net.dreamlu.mica.admin.project.im.topic.MqttTopicConstants;
import net.dreamlu.mica.core.result.R;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * IM 群服务实现（PR-1.1.1 读 + PR-1.1.2 写）。
 *
 * @author L.cm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImGroupServiceImpl extends ServiceImpl<ImGroupMapper, ImGroup> implements IImGroupService {

	private static final int MAX_MEMBERS_DEFAULT = 200;

	private final ImGroupMemberMapper groupMemberMapper;
	private final IImGroupMemberService groupMemberService;
	private final ImConversationMapper conversationMapper;
	private final IImConversationMemberService conversationMemberService;
	private final ImPushService pushService;

	// ==================== 读接口 ====================

	@Override
	public List<ImGroup> listMyGroups(Long userId) {
		if (userId == null) {
			return new ArrayList<>();
		}
		return baseMapper.selectMyGroups(userId);
	}

	@Override
	public ImGroup getDetail(Long groupId) {
		ImGroup group = getById(groupId);
		if (group == null) {
			R.throwFail(ImApiCode.GROUP_NOT_FOUND);
		}
		return group;
	}

	@Override
	public List<ImGroupMember> listMembers(Long groupId) {
		getDetail(groupId);
		return groupMemberMapper.selectByGroupId(groupId);
	}

	@Override
	public boolean isMember(Long groupId, Long userId) {
		if (groupId == null || userId == null) {
			return false;
		}
		return groupMemberMapper.selectCount(
			new LambdaQueryWrapper<ImGroupMember>()
				.eq(ImGroupMember::getGroupId, groupId)
				.eq(ImGroupMember::getUserId, userId)) > 0;
	}

	// ==================== 写接口 ====================

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ImGroup createGroup(Long ownerId, String name, String avatar, String type,
								String announcement, List<Long> memberIds) {
		if (ownerId == null) {
			R.throwFail("创建者不能为空");
		}
		if (name == null || name.trim().isEmpty()) {
			R.throwFail("群名称不能为空");
		}

		// 去重成员，保证群主不在 memberIds 里（避免重复插入）
		LinkedHashSet<Long> uniqueMembers = memberIds == null
			? new LinkedHashSet<>()
			: new LinkedHashSet<>(memberIds);
		uniqueMembers.remove(ownerId);

		// 1. 保存群记录
		ImGroup group = new ImGroup();
		group.setName(name.trim());
		group.setAvatar(avatar == null || avatar.trim().isEmpty() ? null : avatar.trim());
		group.setType("department".equalsIgnoreCase(type) ? "department" : "normal");
		group.setOwnerId(ownerId);
		group.setAnnouncement(announcement);
		group.setMemberCount(1 + uniqueMembers.size());
		group.setMaxMembers(MAX_MEMBERS_DEFAULT);
		save(group);
		log.info("[IM] group created, groupId={}, name={}, ownerId={}, members={}",
			group.getId(), group.getName(), ownerId, uniqueMembers.size());

		// 2. 创建对应会话（id = "g_{groupId}"，type = group）
		ImConversation conv = new ImConversation();
		String conversationId = MqttTopicConstants.conversationIdForGroup(group.getId());
		conv.setId(conversationId);
		conv.setType("group");
		conv.setLastMsgId(null);
		conv.setLastMsgTime(null);
		conv.setLastMsgPreview(null);
		conversationMapper.insert(conv);

		// 3. 批量写入群成员（owner + 被邀请成员）
		List<Long> allMemberIds = new ArrayList<>(1 + uniqueMembers.size());
		allMemberIds.add(ownerId);
		allMemberIds.addAll(uniqueMembers);
		batchInsertGroupMembers(group.getId(), allMemberIds, ownerId);
		batchInsertConversationMembers(conversationId, allMemberIds, ownerId);

		// 4. 推送"群已创建"系统消息给所有成员
		ImPushPayload payload = ImPushPayload.builder()
			.msgId(java.util.UUID.randomUUID().toString())
			.msgType("group")
			.eventType("group_create")
			.groupId(group.getId())
			.conversationId(conversationId)
			.senderId(ownerId)
			.content(String.format("群聊「%s」已创建", name))
			.serverTime(LocalDateTime.now())
			.build();
		pushService.pushGroupMessage(group.getId(), allMemberIds, payload);

		// 5. 给被邀请人额外推邀请提示
		if (!uniqueMembers.isEmpty()) {
			ImPushPayload invite = ImPushPayload.builder()
				.msgId(java.util.UUID.randomUUID().toString())
				.msgType("group")
				.eventType("group_invite")
				.groupId(group.getId())
				.conversationId(conversationId)
				.senderId(ownerId)
				.content("被邀请加入「" + name + "」")
				.serverTime(LocalDateTime.now())
				.build();
			pushService.pushGroupMessage(group.getId(), new ArrayList<>(uniqueMembers), invite);
		}

		return group;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int addMembers(Long groupId, Long operatorId, List<Long> newMembers) {
		ImGroup group = getDetail(groupId);
		ensureCanManage(group, operatorId, "邀请成员");
		if (newMembers == null || newMembers.isEmpty()) {
			return 0;
		}

		// 已在群中的成员过滤掉
		Set<Long> existingIds = listMembers(groupId).stream()
			.map(ImGroupMember::getUserId)
			.collect(Collectors.toSet());
		List<Long> actualAdded = newMembers.stream()
			.distinct()
			.filter(u -> !existingIds.contains(u))
			.collect(Collectors.toList());
		if (actualAdded.isEmpty()) {
			return 0;
		}
		int max = group.getMaxMembers() != null ? group.getMaxMembers() : MAX_MEMBERS_DEFAULT;
		if (existingIds.size() + actualAdded.size() > max) {
			R.throwFail(ImApiCode.GROUP_FULL);
		}

		batchInsertGroupMembers(groupId, actualAdded, operatorId);
		batchInsertConversationMembers(
			MqttTopicConstants.conversationIdForGroup(groupId),
			actualAdded, operatorId);
		baseMapper.incrMemberCount(groupId, actualAdded.size());

		// 推送：给新成员
		ImPushPayload payload = ImPushPayload.builder()
			.msgId(java.util.UUID.randomUUID().toString())
			.msgType("group")
			.eventType("group_invite")
			.groupId(groupId)
			.conversationId(MqttTopicConstants.conversationIdForGroup(groupId))
			.senderId(operatorId)
			.content("被邀请加入「" + group.getName() + "」")
			.serverTime(LocalDateTime.now())
			.build();
		pushService.pushGroupMessage(groupId, actualAdded, payload);

		// 推送：给已有成员（提示有新成员加入）
		ImPushPayload joinNotice = ImPushPayload.builder()
			.msgId(java.util.UUID.randomUUID().toString())
			.msgType("group")
			.eventType("group_member_join")
			.groupId(groupId)
			.conversationId(MqttTopicConstants.conversationIdForGroup(groupId))
			.senderId(operatorId)
			.content("新成员加入，共 " + actualAdded.size() + " 人")
			.serverTime(LocalDateTime.now())
			.build();
		pushService.pushGroupMessage(groupId, new ArrayList<>(existingIds), joinNotice);

		log.info("[IM] addMembers groupId={}, operatorId={}, added={}", groupId, operatorId, actualAdded.size());
		return actualAdded.size();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeMember(Long groupId, Long operatorId, Long targetId) {
		ImGroup group = getDetail(groupId);
		ensureCanManage(group, operatorId, "移除成员");
		if (group.getOwnerId().equals(targetId)) {
			R.throwFail("不能移除群主");
		}
		if (!isMember(groupId, targetId)) {
			R.throwFail(ImApiCode.GROUP_NOT_MEMBER);
		}

		groupMemberService.remove(new LambdaQueryWrapper<ImGroupMember>()
			.eq(ImGroupMember::getGroupId, groupId)
			.eq(ImGroupMember::getUserId, targetId));
		conversationMemberService.remove(new LambdaQueryWrapper<ImConversationMember>()
			.eq(ImConversationMember::getConversationId, MqttTopicConstants.conversationIdForGroup(groupId))
			.eq(ImConversationMember::getUserId, targetId));
		baseMapper.incrMemberCount(groupId, -1);

		// 推送：给剩余成员
		List<Long> remainingIds = listMembers(groupId).stream()
			.map(ImGroupMember::getUserId)
			.collect(Collectors.toList());
		ImPushPayload payload = ImPushPayload.builder()
			.msgId(java.util.UUID.randomUUID().toString())
			.msgType("group")
			.eventType("group_kick")
			.groupId(groupId)
			.conversationId(MqttTopicConstants.conversationIdForGroup(groupId))
			.senderId(operatorId)
			.content("成员已被移出群「" + group.getName() + "」")
			.serverTime(LocalDateTime.now())
			.build();
		pushService.pushGroupMessage(groupId, remainingIds, payload);

		// 推送：给被踢人
		ImPushPayload kickNotice = ImPushPayload.builder()
			.msgId(java.util.UUID.randomUUID().toString())
			.msgType("group")
			.eventType("group_kicked")
			.groupId(groupId)
			.senderId(operatorId)
			.content("你已被移出群「" + group.getName() + "」")
			.serverTime(LocalDateTime.now())
			.build();
		pushService.pushSystemMessage(targetId, kickNotice);

		log.info("[IM] removeMember groupId={}, operatorId={}, targetId={}", groupId, operatorId, targetId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void quitGroup(Long groupId, Long userId) {
		ImGroup group = getDetail(groupId);
		if (group.getOwnerId().equals(userId)) {
			R.throwFail("群主不能直接退群，请先解散群或移交群主");
		}
		if (!isMember(groupId, userId)) {
			R.throwFail(ImApiCode.GROUP_NOT_MEMBER);
		}

		groupMemberService.remove(new LambdaQueryWrapper<ImGroupMember>()
			.eq(ImGroupMember::getGroupId, groupId)
			.eq(ImGroupMember::getUserId, userId));
		conversationMemberService.remove(new LambdaQueryWrapper<ImConversationMember>()
			.eq(ImConversationMember::getConversationId, MqttTopicConstants.conversationIdForGroup(groupId))
			.eq(ImConversationMember::getUserId, userId));
		baseMapper.incrMemberCount(groupId, -1);

		// 推送：给其他成员
		List<Long> remainingIds = listMembers(groupId).stream()
			.map(ImGroupMember::getUserId)
			.collect(Collectors.toList());
		ImPushPayload payload = ImPushPayload.builder()
			.msgId(java.util.UUID.randomUUID().toString())
			.msgType("group")
			.eventType("group_member_quit")
			.groupId(groupId)
			.conversationId(MqttTopicConstants.conversationIdForGroup(groupId))
			.senderId(userId)
			.content("成员退出群「" + group.getName() + "」")
			.serverTime(LocalDateTime.now())
			.build();
		pushService.pushGroupMessage(groupId, remainingIds, payload);

		log.info("[IM] quitGroup groupId={}, userId={}", groupId, userId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void dismissGroup(Long groupId, Long operatorId) {
		ImGroup group = getDetail(groupId);
		if (!group.getOwnerId().equals(operatorId)) {
			R.throwFail("只有群主可以解散群");
		}

		String conversationId = MqttTopicConstants.conversationIdForGroup(groupId);
		List<Long> memberIds = listMembers(groupId).stream()
			.map(ImGroupMember::getUserId)
			.collect(Collectors.toList());

		// 先推送解散消息
		ImPushPayload payload = ImPushPayload.builder()
			.msgId(java.util.UUID.randomUUID().toString())
			.msgType("group")
			.eventType("group_dismiss")
			.groupId(groupId)
			.conversationId(conversationId)
			.senderId(operatorId)
			.content("群「" + group.getName() + "」已被群主解散")
			.serverTime(LocalDateTime.now())
			.build();
		pushService.pushGroupMessage(groupId, memberIds, payload);

		// 按顺序清理表数据：会话成员 → 会话 → 群成员 → 群记录
		conversationMemberService.remove(new LambdaQueryWrapper<ImConversationMember>()
			.eq(ImConversationMember::getConversationId, conversationId));
		conversationMapper.deleteById(conversationId);
		groupMemberService.remove(new LambdaQueryWrapper<ImGroupMember>()
			.eq(ImGroupMember::getGroupId, groupId));
		removeById(groupId);

		log.info("[IM] dismissGroup groupId={}, operatorId={}", groupId, operatorId);
	}

	// ==================== 私有辅助 ====================

	/**
	 * 权限校验：群主 / 管理员可操作。
	 */
	private void ensureCanManage(ImGroup group, Long operatorId, String action) {
		if (group == null) {
			R.throwFail(ImApiCode.GROUP_NOT_FOUND);
		}
		if (operatorId == null) {
			R.throwFail("操作人不能为空");
		}
		if (group.getOwnerId().equals(operatorId)) {
			return;
		}
		ImGroupMember member = groupMemberService.getOne(new LambdaQueryWrapper<ImGroupMember>()
			.eq(ImGroupMember::getGroupId, group.getId())
			.eq(ImGroupMember::getUserId, operatorId));
		if (member == null) {
			R.throwFail(ImApiCode.GROUP_NOT_MEMBER);
		}
		if (!"admin".equalsIgnoreCase(member.getRole())) {
			R.throwFail("无权限执行「" + action + "」");
		}
	}

	/**
	 * 批量写入 im_group_member。
	 */
	private void batchInsertGroupMembers(Long groupId, List<Long> userIds, Long ownerId) {
		if (userIds == null || userIds.isEmpty()) {
			return;
		}
		int batchSize = 100;
		List<ImGroupMember> batch = new ArrayList<>(Math.min(batchSize, userIds.size()));
		for (Long uid : userIds) {
			ImGroupMember m = new ImGroupMember();
			m.setGroupId(groupId);
			m.setUserId(uid);
			m.setRole(uid.equals(ownerId) ? "owner" : "member");
			m.setJoinedAt(LocalDateTime.now());
			batch.add(m);
			if (batch.size() >= batchSize) {
				groupMemberService.saveBatch(batch);
				batch.clear();
			}
		}
		if (!batch.isEmpty()) {
			groupMemberService.saveBatch(batch);
		}
	}

	/**
	 * 批量写入 im_conversation_member。
	 */
	private void batchInsertConversationMembers(String conversationId, List<Long> userIds, Long ownerId) {
		if (userIds == null || userIds.isEmpty()) {
			return;
		}
		int batchSize = 100;
		List<ImConversationMember> batch = new ArrayList<>(Math.min(batchSize, userIds.size()));
		for (Long uid : userIds) {
			ImConversationMember m = new ImConversationMember();
			m.setConversationId(conversationId);
			m.setUserId(uid);
			m.setRole(uid.equals(ownerId) ? "owner" : "member");
			m.setUnreadCount(0);
			m.setMute(false);
			m.setTop(false);
			batch.add(m);
			if (batch.size() >= batchSize) {
				conversationMemberService.saveBatch(batch);
				batch.clear();
			}
		}
		if (!batch.isEmpty()) {
			conversationMemberService.saveBatch(batch);
		}
	}
}
