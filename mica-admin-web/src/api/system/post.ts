import { api, parsePage } from '#/api/request';
import { exportExcel } from '#/utils/export-excel';

export interface PostItem {
  id: number;
  name: string;
  code?: string;
  sort?: number;
  seq?: number;
  enabled?: boolean;
  status?: boolean;
  remark?: string;
  createdAt?: string;
}

export async function getPostList(params?: {
  page?: number;
  size?: number;
  blurry?: string;
}) {
  const data = await api.get<any>('/api/system/post', { params });
  return parsePage<PostItem>(data);
}

export async function addPost(data: Partial<PostItem>) {
  return api.post('/api/system/post', data);
}

export async function editPost(data: Partial<PostItem>) {
  return api.put('/api/system/post', data);
}

export async function deletePost(ids: number[]) {
  return api.delete('/api/system/post', { data: ids });
}

export async function exportPostExcel(params?: Record<string, unknown>) {
  return exportExcel({ api, url: '/api/system/post/download', filename: '岗位数据', params });
}
