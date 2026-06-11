/**
 * 字典管理 API
 */
import { api, parsePage } from '#/api/request';
import { exportExcel } from '#/utils/export-excel';

export interface DictTypeItem {
  id: number;
  name: string;
  description?: string;
}

export interface DictItem {
  id: number;
  label: string;
  value: string;
  type?: string;
  seq?: number;
  cssClass?: string;
  listClass?: string;
  isDefault?: boolean;
  status?: number;
  remark?: string;
}

export async function getDictList(params?: { page?: number; size?: number; blurry?: string }) {
  const data = await api.get<any>('/api/system/dict', { params });
  return parsePage<DictTypeItem>(data);
}

export async function getDictItems(name: string) {
  // 后端 DictInfoQuery.name 对应 SysDictInfo.type；分页 page 从 0 开始
  const data = await api.get<any>('/api/system/dict/info', {
    params: {
      name,
      page: 0,
      size: 9999,
      sort: ['seq,asc', 'id,desc'],
    },
    paramsSerializer: {
      indexes: null,
    },
  });
  return parsePage<DictItem>(data).list;
}

export async function addDict(data: { name: string; description?: string }) {
  return api.post('/api/system/dict', data);
}

export async function editDict(data: { id: number; name: string; description?: string }) {
  return api.put('/api/system/dict', data);
}

export async function deleteDict(ids: number[]) {
  return api.delete('/api/system/dict', { data: ids });
}

export async function addDictItem(data: {
  type: string;
  label: string;
  value: string;
  seq?: number;
}) {
  return api.post('/api/system/dict/info', data);
}

export async function editDictItem(data: {
  id: number;
  type: string;
  label: string;
  value: string;
  seq?: number;
}) {
  return api.put('/api/system/dict/info', data);
}

export async function deleteDictItem(id: number) {
  return api.delete(`/api/system/dict/info/${id}`);
}

export async function exportDictExcel(params?: Record<string, unknown>) {
  return exportExcel({ api, url: '/api/system/dict/download', filename: '字典数据', params });
}

export async function exportDictInfoExcel(params?: Record<string, unknown>) {
  return exportExcel({ api, url: '/api/system/dict/info/download', filename: '字典详情', params });
}
