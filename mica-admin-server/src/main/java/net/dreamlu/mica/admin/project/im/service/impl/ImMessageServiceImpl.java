package net.dreamlu.mica.admin.project.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.project.im.entity.ImConversation;
import net.dreamlu.mica.admin.project.im.entity.ImConversationMember;
import net.dreamlu.mica.admin.project.im.entity.ImMessage;
import net.dreamlu.mica.admin.project.im.mapper.ImMessageMapper;
import net.dreamlu.mica.admin.project.im.service.IImConversationMemberService;
import net.dreamlu.mica.admin.project.im.service.IImConversationService;
import net.dreamlu.mica.admin.project.im.service.IImMessageService;
import net.dreamlu.mica.admin.project.im.service.ImUnreadService;
import net.dreamlu.mica.core.result.R;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * IM 消息服务实现。
 *
 * @author L.cm
 */
@Service
@RequiredArgsConstructor
public class ImMessageServiceImpl extends ServiceImpl<ImMessageMapper, ImMessage> implements IImMessageService {

	/**
	 * 消息可撤回的窗口期（2 分钟）。
	 */
	private static final long RECALL_WINDOW_MINUTES = 2L;

	private final IImConversationService conversationService;
	private final IImConversationMemberService memberService;
	private final ImUnreadService unreadService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ImMessage saveP2pMessage(ImMessage message) {
		if (message == null || message.getSenderId() == null || message.getReceiverId() == null) {
			throw new IllegalArgumentException("senderId/receiverId must not be null");
		}
		if (StringUtil.isBlank(message.getContent())) {
			throw new IllegalArgumentException("content must not be blank");
		}
		// 1. 确保会话存在
		ImConversation conversation = conversationService
			.getOrCreateP2pConversation(message.getSenderId(), message.getReceiverId());
		message.setConversationId(conversation.getId());
		// 2. 默认值
		if (message.getMsgType() == null) {
			message.setMsgType("text");
		}
		if (message.getStatus() == null) {
			message.setStatus(1);
		}
		if (message.getServerReceivedAt() == null) {
			message.setServerReceivedAt(LocalDateTime.now());
		}
		save(message);
		// 3. 更新会话最后一条消息
		String preview = buildPreview(message);
		conversation.setLastMsgId(message.getId());
		conversation.setLastMsgTime(message.getServerReceivedAt());
		conversation.setLastMsgPreview(preview);
		conversationService.updateById(conversation);
		// 4. 增加接收方未读数：Redis 权威 incr（DB 同步兜底）
		Long receiverId = message.getReceiverId();
		memberService.update(new LambdaUpdateWrapper<ImConversationMember>()
			.eq(ImConversationMember::getConversationId, conversation.getId())
			.eq(ImConversationMember::getUserId, receiverId)
			.setSql("unread_count = unread_count + 1"));
		unreadService.incrementUnread(receiverId, conversation.getId());
		return message;
	}

	@Override
	public List<ImMessage> listByConversation(String conversationId, Long beforeId, Integer size) {
		int limit = size == null || size <= 0 ? 20 : Math.min(size, 100);
		QueryWrapper<ImMessage> wrapper = new QueryWrapper<>();
		wrapper.lambda()
			.eq(ImMessage::getConversationId, conversationId)
			.lt(beforeId != null, ImMessage::getId, beforeId)
			.orderByDesc(ImMessage::getId)
			.last("LIMIT " + limit);
		List<ImMessage> messages = list(wrapper);
		// 倒序查询的结果反转成时间正序，前端渲染时按时间正序追加
		Collections.reverse(messages);
		return messages;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recallMessage(Long messageId, Long userId) {
		ImMessage message = getById(messageId);
		if (message == null) {
			R.throwFail(net.dreamlu.mica.admin.project.im.common.ImApiCode.CONVERSATION_NOT_FOUND);
		}
		if (!message.getSenderId().equals(userId)) {
			R.throwFail("仅发送者本人可撤回消息");
		}
		LocalDateTime sentAt = message.getServerReceivedAt();
		if (sentAt != null && sentAt.isBefore(LocalDateTime.now().minusMinutes(RECALL_WINDOW_MINUTES))) {
			R.throwFail("仅支持撤回 " + RECALL_WINDOW_MINUTES + " 分钟内的消息");
		}
		ImMessage update = new ImMessage();
		update.setId(messageId);
		update.setStatus(2);
		update.setRecallBy(userId);
		update.setRecallAt(LocalDateTime.now());
		updateById(update);
	}

	private static String buildPreview(ImMessage message) {
		String preview = "[文件]";
		if ("text".equalsIgnoreCase(message.getMsgType())) {
			preview = message.getContent();
		} else if ("image".equalsIgnoreCase(message.getMsgType())) {
			preview = "[图片]";
		}
		if (preview != null && preview.length() > 200) {
			preview = preview.substring(0, 200);
		}
		return preview;
	}
}