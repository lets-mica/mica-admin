package net.dreamlu.mica.admin.project.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.project.im.entity.ImConversation;
import net.dreamlu.mica.admin.project.im.entity.ImConversationMember;
import net.dreamlu.mica.admin.project.im.mapper.ImConversationMapper;
import net.dreamlu.mica.admin.project.im.service.IImConversationMemberService;
import net.dreamlu.mica.admin.project.im.service.IImConversationService;
import net.dreamlu.mica.admin.project.im.topic.MqttTopicConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * IM 会话服务实现。
 *
 * @author L.cm
 */
@Service
@RequiredArgsConstructor
public class ImConversationServiceImpl extends ServiceImpl<ImConversationMapper, ImConversation>
	implements IImConversationService {

	private final IImConversationMemberService memberService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ImConversation getOrCreateP2pConversation(Long userA, Long userB) {
		if (userA == null || userB == null || userA.equals(userB)) {
			throw new IllegalArgumentException("invalid p2p participants");
		}
		String conversationId = MqttTopicConstants.conversationId(userA, userB);
		ImConversation conversation = getById(conversationId);
		if (conversation != null) {
			return conversation;
		}
		// 不存在则创建（user_a / user_b 强制升序，方便后续按 user_a 索引）
		long min = Math.min(userA, userB);
		long max = Math.max(userA, userB);
		ImConversation entity = new ImConversation();
		entity.setId(conversationId);
		entity.setType("p2p");
		entity.setUserA(min);
		entity.setUserB(max);
		save(entity);
		// 同步预插入 2 条成员记录
		Arrays.asList(min, max).forEach(userId -> {
			ImConversationMember member = new ImConversationMember();
			member.setConversationId(conversationId);
			member.setUserId(userId);
			member.setRole("member");
			member.setUnreadCount(0);
			member.setMute(false);
			member.setTop(false);
			memberService.save(member);
		});
		return entity;
	}

	@Override
	public List<ImConversation> listUserConversations(Long userId) {
		// 用户的会话 = 他是 user_a 的会话 ∪ 他是 user_b 的会话
		// 群聊（Phase 1.1 引入）由 im_conversation_member.user_id 过滤
		QueryWrapper<ImConversation> wrapper = new QueryWrapper<>();
		wrapper.lambda()
			.and(w -> w.eq(ImConversation::getUserA, userId)
				.or().eq(ImConversation::getUserB, userId))
			.orderByDesc(ImConversation::getLastMsgTime)
			.last("LIMIT 500");
		return list(wrapper);
	}

	@Override
	public boolean isMember(String conversationId, Long userId) {
		return memberService.count(new LambdaQueryWrapper<ImConversationMember>()
			.eq(ImConversationMember::getConversationId, conversationId)
			.eq(ImConversationMember::getUserId, userId)) > 0;
	}
}