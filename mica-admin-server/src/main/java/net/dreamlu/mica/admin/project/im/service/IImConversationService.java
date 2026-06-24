package net.dreamlu.mica.admin.project.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.im.entity.ImConversation;

import java.util.List;

/**
 * IM 会话服务。
 *
 * @author L.cm
 */
public interface IImConversationService extends IService<ImConversation> {

	/**
	 * 获取或创建单聊会话。
	 *
	 * @param userA 参与方 A
	 * @param userB 参与方 B
	 * @return 会话
	 */
	ImConversation getOrCreateP2pConversation(Long userA, Long userB);

	/**
	 * 列出某用户参与的所有会话（p2p + 群聊都包含），按最后消息时间倒序。
	 *
	 * @param userId 用户 id
	 * @return 会话列表（最多 500 条，超出请使用分页接口）
	 */
	List<ImConversation> listUserConversations(Long userId);

	/**
	 * 检查用户是否是会话成员。
	 *
	 * @param conversationId 会话 id
	 * @param userId         用户 id
	 * @return true 是成员
	 */
	boolean isMember(String conversationId, Long userId);
}