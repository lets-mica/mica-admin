package net.dreamlu.mica.admin.project.im.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.im.entity.ImMessage;

import java.util.List;

/**
 * IM 消息服务。
 *
 * @author L.cm
 */
public interface IImMessageService extends IService<ImMessage> {

	/**
	 * 保存单聊消息（自动创建会话、更新未读）。
	 *
	 * @param message 消息实体（id 留空，由雪花算法生成）
	 * @return 保存后的消息（含 id）
	 */
	ImMessage saveP2pMessage(ImMessage message);

	/**
	 * 拉取会话消息历史（按 server_received_at 倒序，倒序分页）。
	 *
	 * @param conversationId 会话 id
	 * @param beforeId       翻页锚点（查该 id 之前的消息，传 null 表示最新页）
	 * @param size           页大小（默认 20，最大 100）
	 * @return 消息列表（已按时间升序）
	 */
	List<ImMessage> listByConversation(String conversationId, Long beforeId, Integer size);

	/**
	 * 撤回消息：只有发送者本人，且 2 分钟内可撤回。
	 *
	 * @param messageId 消息 id
	 * @param userId    操作用户
	 */
	void recallMessage(Long messageId, Long userId);
}