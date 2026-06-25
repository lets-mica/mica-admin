/**
 * 用户消息 API
 */
import { api, parsePage } from '#/api/request';

export interface UserMessageItem {
  id: number;
  messageId: number;
  title: string;
  category?: string;
  content?: string;
  isRead: boolean;
  createdAt: string;
}

export async function getUnreadMessages() {
  return api.get<any>('/api/system/user/message/unread');
}

export async function getMyMessages(params?: Record<string, unknown>) {
  const data = await api.get<any>('/api/system/user/message', { params });
  return parsePage<UserMessageItem>(data);
}

export async function markAsRead(id: number) {
  return api.put(`/api/system/user/message/read/${id}`);
}

export async function markAllAsRead() {
  return api.put('/api/system/user/message/read-all');
}
