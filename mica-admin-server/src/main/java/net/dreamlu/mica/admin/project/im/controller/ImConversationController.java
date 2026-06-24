package net.dreamlu.mica.admin.project.im.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.project.im.common.ImApiCode;
import net.dreamlu.mica.admin.project.im.entity.ImConversation;
import net.dreamlu.mica.admin.project.im.entity.ImConversationMember;
import net.dreamlu.mica.admin.project.im.entity.ImMessage;
import net.dreamlu.mica.admin.project.im.push.ImPushService;
import net.dreamlu.mica.admin.project.im.service.IImConversationMemberService;
import net.dreamlu.mica.admin.project.im.service.IImConversationService;
import net.dreamlu.mica.admin.project.im.service.IImMessageService;
import net.dreamlu.mica.admin.project.im.service.ImUnreadService;
import net.dreamlu.mica.admin.project.im.vo.ConversationVO;
import net.dreamlu.mica.admin.project.im.vo.MessageVO;
import net.dreamlu.mica.admin.project.im.vo.P2pConversationForm;
import net.dreamlu.mica.admin.project.system.entity.SysUser;
import net.dreamlu.mica.admin.project.system.service.ISysUserService;
import net.dreamlu.mica.core.result.R;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * IM 会话与消息接口。
 *
 * @author L.cm
 */
@Tag(name = "IM：会话与消息")
@RestController
@RequestMapping("/api/im/conversations")
@RequiredArgsConstructor
public class ImConversationController extends BaseController {

	private final IImConversationService conversationService;
	private final IImConversationMemberService memberService;
	private final IImMessageService messageService;
	private final ImUnreadService unreadService;
	private final ImPushService pushService;
	private final ISysUserService userService;

	/**
	 * 创建/获取单聊会话。
	 */
	@Operation(summary = "创建/获取单聊会话")
	@PostMapping("/p2p")
	public Map<String, Object> createP2p(@Valid @RequestBody P2pConversationForm form, AuthUser authUser) {
		Long me = authUser.getUserId();
		Long peer = form.getPeerUserId();
		if (me.equals(peer)) {
			R.throwFail("不能与自己创建会话");
		}
		ImConversation conversation = conversationService.getOrCreateP2pConversation(me, peer);
		// 构建 member 记录（确保当前用户有 member 信息）
		ImConversationMember member = memberService.lambdaQuery()
			.eq(ImConversationMember::getConversationId, conversation.getId())
			.eq(ImConversationMember::getUserId, me)
			.one();
		Map<String, Object> data = new HashMap<>(2);
		data.put("conversation", toVO(conversation, me, member,
			unreadService.getUnread(me, conversation.getId(), member != null ? member.getUnreadCount() : 0)));
		return data;
	}

	/**
	 * 获取当前用户的会话列表（按最后消息时间倒序）。
	 * <p>
	 * 未读数优先从 Redis 读取，缺失时使用 DB 的 {@code unread_count} 回灌。
	 */
	@Operation(summary = "获取会话列表")
	@GetMapping
	public List<ConversationVO> list(AuthUser authUser) {
		Long me = authUser.getUserId();
		List<ImConversation> list = conversationService.listUserConversations(me);
		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		List<ImConversationMember> members = memberService.lambdaQuery()
			.eq(ImConversationMember::getUserId, me)
			.in(ImConversationMember::getConversationId,
				list.stream().map(ImConversation::getId).collect(Collectors.toList()))
			.list();
		Map<String, ImConversationMember> memberMap = members.stream()
			.collect(Collectors.toMap(ImConversationMember::getConversationId, m -> m));
		Map<String, Integer> unreadMap = unreadService.getUnreadMap(me, members);
		List<ConversationVO> result = new ArrayList<>(list.size());
		for (ImConversation conv : list) {
			ImConversationMember member = memberMap.get(conv.getId());
			int unread = unreadMap.getOrDefault(conv.getId(), 0);
			result.add(toVO(conv, me, member, unread));
		}
		return result;
	}

	/**
	 * 拉取会话消息历史（按时间正序分页）。
	 *
	 * @param convId   会话 id
	 * @param beforeId 翻页锚点（查询该 id 之前的消息，null = 最新页）
	 * @param size     页大小（默认 20，最大 100）
	 */
	@Operation(summary = "拉取会话消息历史")
	@GetMapping("/{convId}/messages")
	public List<MessageVO> history(@PathVariable String convId,
								   @RequestParam(required = false) Long beforeId,
								   @RequestParam(required = false, defaultValue = "20") Integer size,
								   AuthUser authUser) {
		// 校验权限：必须是会话成员
		if (!conversationService.isMember(convId, authUser.getUserId())) {
			R.throwFail(ImApiCode.CONVERSATION_NOT_MEMBER);
		}
		List<ImMessage> messages = messageService.listByConversation(convId, beforeId, size);
		List<MessageVO> result = new ArrayList<>(messages.size());
		for (ImMessage message : messages) {
			MessageVO vo = new MessageVO();
			vo.setId(message.getId());
			vo.setConversationId(message.getConversationId());
			vo.setSenderId(message.getSenderId());
			vo.setReceiverId(message.getReceiverId());
			vo.setMsgType(message.getMsgType());
			vo.setContent(message.getContent());
			vo.setExtra(message.getExtra());
			vo.setStatus(message.getStatus());
			vo.setServerReceivedAt(message.getServerReceivedAt());
			vo.setRecallBy(message.getRecallBy());
			vo.setRecallAt(message.getRecallAt());
			result.add(vo);
		}
		return result;
	}

	/**
	 * 标记单会话已读：
	 * <ol>
	 *   <li>Redis 未读数清零</li>
	 *   <li>DB unread_count = 0</li>
	 *   <li>如果传入 {@code toMsgId}，向对端推送已读回传事件</li>
	 * </ol>
	 */
	@Operation(summary = "标记会话已读")
	@PostMapping("/{convId}/mark-read")
	public Map<String, Object> markRead(@PathVariable String convId,
										@RequestParam(required = false) Long toMsgId,
										AuthUser authUser) {
		Long me = authUser.getUserId();
		if (!conversationService.isMember(convId, me)) {
			R.throwFail(ImApiCode.CONVERSATION_NOT_MEMBER);
		}
		// 1. Redis 清零
		unreadService.markConversationRead(me, convId);
		// 2. DB unread_count = 0
		memberService.update(new LambdaUpdateWrapper<ImConversationMember>()
			.eq(ImConversationMember::getConversationId, convId)
			.eq(ImConversationMember::getUserId, me)
			.set(ImConversationMember::getUnreadCount, 0)
			.set(ImConversationMember::getLastReadTime, LocalDateTime.now()));
		// 3. 向对端推送已读回传事件（仅 p2p 需要）
		ImConversation conv = conversationService.getById(convId);
		if (conv != null && "p2p".equals(conv.getType())) {
			Long peer = conv.getUserA().equals(me) ? conv.getUserB() : conv.getUserA();
			long readTo = toMsgId != null ? toMsgId : (conv.getLastMsgId() != null ? conv.getLastMsgId() : 0L);
			pushService.pushReadReceipt(convId, peer, me, readTo);
		}
		Map<String, Object> data = new HashMap<>(2);
		data.put("conversationId", convId);
		data.put("unreadCount", 0);
		return data;
	}

	/**
	 * 查询当前用户的总未读数（顶部小红点用）。
	 */
	@Operation(summary = "查询总未读数")
	@GetMapping("/unread-total")
	public Map<String, Object> unreadTotal(AuthUser authUser) {
		Long me = authUser.getUserId();
		List<ImConversationMember> members = memberService.lambdaQuery()
			.eq(ImConversationMember::getUserId, me)
			.list();
		int total = 0;
		if (!members.isEmpty()) {
			Map<String, Integer> unreadMap = unreadService.getUnreadMap(me, members);
			for (int v : unreadMap.values()) {
				total += Math.max(0, v);
			}
		}
		Map<String, Object> data = new HashMap<>(2);
		data.put("total", total);
		return data;
	}

	/**
	 * 全部标记已读（移动端常用一键清零）。
	 */
	@Operation(summary = "全部标记已读")
	@PostMapping("/mark-all-read")
	public Map<String, Object> markAllRead(AuthUser authUser) {
		Long me = authUser.getUserId();
		List<ImConversationMember> members = memberService.lambdaQuery()
			.eq(ImConversationMember::getUserId, me)
			.gt(ImConversationMember::getUnreadCount, 0)
			.list();
		if (members.isEmpty()) {
			Map<String, Object> data = new HashMap<>(2);
			data.put("cleared", 0);
			return data;
		}
		List<String> convIds = members.stream().map(ImConversationMember::getConversationId)
			.collect(Collectors.toList());
		unreadService.markAllRead(me, convIds);
		memberService.update(new LambdaUpdateWrapper<ImConversationMember>()
			.eq(ImConversationMember::getUserId, me)
			.in(ImConversationMember::getConversationId, convIds)
			.set(ImConversationMember::getUnreadCount, 0)
			.set(ImConversationMember::getLastReadTime, LocalDateTime.now()));
		Map<String, Object> data = new HashMap<>(2);
		data.put("cleared", convIds.size());
		return data;
	}

	/**
	 * 撤回消息（仅发送者本人，2 分钟内）+ 向双方推送撤回事件。
	 */
	@Operation(summary = "撤回消息")
	@DeleteMapping("/messages/{messageId}")
	public Map<String, Object> recall(@PathVariable Long messageId, AuthUser authUser) {
		messageService.recallMessage(messageId, authUser.getUserId());
		ImMessage message = messageService.getById(messageId);
		if (message != null) {
			pushService.pushRecallEvent(message.getConversationId(),
				java.util.Arrays.asList(message.getSenderId(), message.getReceiverId()),
				messageId, authUser.getUserId());
		}
		Map<String, Object> data = new HashMap<>(2);
		data.put("messageId", messageId);
		data.put("status", 2);
		return data;
	}

	private ConversationVO toVO(ImConversation conversation, Long me, ImConversationMember member, int unreadCount) {
		ConversationVO vo = new ConversationVO();
		vo.setId(conversation.getId());
		vo.setType(conversation.getType());
		vo.setLastMsgId(conversation.getLastMsgId());
		vo.setLastMsgTime(conversation.getLastMsgTime());
		vo.setLastMsgPreview(conversation.getLastMsgPreview());
		vo.setUnreadCount(unreadCount);
		if (member != null) {
			vo.setTop(member.getTop());
			vo.setMute(member.getMute());
		}
		// 单聊：计算对端 userId + 对端用户信息
		if ("p2p".equals(conversation.getType())) {
			Long peer = conversation.getUserA().equals(me) ? conversation.getUserB() : conversation.getUserA();
			vo.setPeerUserId(peer);
			SysUser peerUser = userService.getById(peer);
			if (peerUser != null) {
				vo.setPeerUserName(peerUser.getNickName() != null ? peerUser.getNickName() : peerUser.getUserName());
				vo.setPeerAvatar(peerUser.getAvatar());
			}
		}
		return vo;
	}
}