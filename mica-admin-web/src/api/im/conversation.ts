/**
 * IM 会话 / 消息 API
 *
 * 后端接口路径：
 *   GET    /api/im/conversations                      会话列表
 *   POST   /api/im/conversations/p2p                  创建/获取单聊会话
 *   GET    /api/im/conversations/{convId}/messages    消息历史
 *   POST   /api/im/conversations/{convId}/mark-read   标记会话已读
 *   POST   /api/im/conversations/mark-all-read        全部标记已读
 *   GET    /api/im/conversations/unread-total         总未读数
 *   DELETE /api/im/conversations/messages/{msgId}     撤回消息
 */
import { api } from '#/api/request';

// ---------- Type ----------

export interface ConversationVO {
  id: string;
  type: 'p2p' | 'group';
  peerUserId?: number;
  peerUserName?: string;
  peerAvatar?: string;
  lastMsgId?: number;
  lastMsgTime?: string;
  lastMsgPreview?: string;
  unreadCount?: number;
  top?: boolean;
  mute?: boolean;
}

export interface MessageVO {
  id: number;
  conversationId: string;
  senderId: number;
  receiverId?: number;
  msgType: 'text' | 'image' | 'file' | 'system';
  content?: string;
  extra?: string;
  status?: number; // 0发送中 1已送达 2已撤回 3失败
  serverReceivedAt?: string;
  recallBy?: number;
  recallAt?: string;
}

// ---------- Conversation ----------

export async function getConversationList(): Promise<ConversationVO[]> {
  return api.get<ConversationVO[]>('/api/im/conversations');
}

export async function getOrCreateP2p(peerUserId: number): Promise<{ conversation: ConversationVO }> {
  return api.post<{ conversation: ConversationVO }>('/api/im/conversations/p2p', { peerUserId });
}

export async function getUnreadTotal(): Promise<{ total: number }> {
  return api.get<{ total: number }>('/api/im/conversations/unread-total');
}

// ---------- Message ----------

export async function getMessageList(
  convId: string,
  params?: { beforeId?: number; size?: number },
): Promise<MessageVO[]> {
  return api.get<MessageVO[]>(`/api/im/conversations/${convId}/messages`, { params });
}

export async function markConversationRead(convId: string, toMsgId?: number): Promise<void> {
  return api.post(`/api/im/conversations/${convId}/mark-read`, { toMsgId });
}

export async function markAllRead(): Promise<void> {
  return api.post('/api/im/conversations/mark-all-read');
}

export async function recallMessage(messageId: number): Promise<void> {
  return api.delete(`/api/im/conversations/messages/${messageId}`);
}
