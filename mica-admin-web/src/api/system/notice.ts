/**
 * 通知公告 API
 */
import { api, parsePage } from '#/api/request';
import { exportExcel } from '#/utils/export-excel';

export interface NoticeItem {
  id: number;
  title: string;
  content?: string;
  type?: number; // 1=通知 2=公告
  status?: number | boolean;
  enabled?: boolean;
  createTime?: string;
  createdAt?: string;
}

export async function getNoticeList(params?: { page?: number; size?: number; blurry?: string }) {
  const data = await api.get<any>('/api/system/notice', { params });
  return parsePage<NoticeItem>(data);
}

export async function getNotice(id: number) {
  return api.get<any>(`/api/system/notice/${id}`);
}

export async function addNotice(data: Partial<NoticeItem>) {
  return api.post('/api/system/notice', data);
}

export async function editNotice(data: Partial<NoticeItem>) {
  return api.put('/api/system/notice', data);
}

export async function deleteNotice(ids: number[]) {
  return api.delete('/api/system/notice', { data: ids });
}

export async function exportNoticeExcel(params?: Record<string, unknown>) {
  return exportExcel({ api, url: '/api/system/notice/download', filename: '通知公告', params });
}
