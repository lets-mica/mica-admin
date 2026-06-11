/**
 * 消息管理 API
 */
import { api, parsePage } from '#/api/request';
import { exportExcel } from '#/utils/export-excel';

export interface MessageItem {
  id: number;
  category?: string;
  title: string;
  content?: string;
  sendFlag?: string;
  seq?: number;
  enabled?: boolean;
  remark?: string;
  createdAt?: string;
}

export async function getMessageList(params?: { page?: number; size?: number; blurry?: string }) {
  const data = await api.get<any>('/api/system/message', { params });
  return parsePage<MessageItem>(data);
}

export async function getMessage(id: number) {
  return api.get<any>(`/api/system/message/${id}`);
}

export async function addMessage(data: Partial<MessageItem>) {
  return api.post('/api/system/message', data);
}

export async function editMessage(data: Partial<MessageItem>) {
  return api.put('/api/system/message', data);
}

export async function deleteMessage(ids: number[]) {
  return api.delete('/api/system/message', { data: ids });
}

export async function publishMessage(id: number, userIds?: number[], deptIds?: number[]) {
  const body: Record<string, number[]> = {};
  if (userIds && userIds.length > 0) body.userIds = userIds;
  if (deptIds && deptIds.length > 0) body.deptIds = deptIds;
  return api.put(`/api/system/message/publish/${id}`, Object.keys(body).length > 0 ? body : undefined);
}

export async function exportMessageExcel(params?: Record<string, unknown>) {
  return exportExcel({ api, url: '/api/system/message/download', filename: '系统消息', params });
}
