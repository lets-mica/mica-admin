import { api, parsePage, pickData } from '#/api/request';
import { exportExcel } from '#/utils/export-excel';

export interface ConfigItem {
  id: number;
  name: string;
  code?: string;
  key?: string;
  value: string;
  description?: string;
  enabled?: boolean;
  status?: boolean;
  remark?: string;
  category?: string;
  createdAt?: string;
}

export async function getConfigList(params?: { page?: number; size?: number; blurry?: string }) {
  const data = await api.get<any>('/api/system/config', { params });
  return parsePage<ConfigItem>(data);
}

export async function getConfig(id: number) {
  const data = await api.get<any>(`/api/system/config/${id}`);
  return pickData<ConfigItem>(data);
}

export async function addConfig(data: Partial<ConfigItem>) {
  return api.post('/api/system/config', data);
}

export async function editConfig(data: Partial<ConfigItem>) {
  return api.put('/api/system/config', data);
}

export async function deleteConfig(ids: number[]) {
  return api.delete('/api/system/config', { data: ids });
}

export async function exportConfigExcel(params?: Record<string, unknown>) {
  return exportExcel({ api, url: '/api/system/config/download', filename: '参数数据', params });
}
