import { api, parsePage } from '#/api/request';
import { exportExcel } from '#/utils/export-excel';

export interface RoleItem {
  id: number;
  name: string;
  title?: string;
  level?: number;
  dataScope?: number;
  seq?: number;
  depts?: number[];
  remark?: string;
  createdAt?: string;
}

export async function getRoleList(params?: {
  page?: number;
  size?: number;
  blurry?: string;
  createTime?: string[];
  sort?: string;
}) {
  const data = await api.get<any>('/api/system/roles', { params });
  return parsePage<RoleItem>(data);
}

export async function getRole(id: number) {
  return api.get<RoleItem>(`/api/system/roles/${id}`);
}

export async function getRoleMenu(roleId: number) {
  const data = await api.get<any>(`/api/system/roles/${roleId}/menus`);
  if (!Array.isArray(data)) return [] as number[];
  return data
    .map((item) => (typeof item === 'number' ? item : Number(item?.id)))
    .filter((id) => !Number.isNaN(id));
}

export async function addRole(data: Partial<RoleItem>) {
  return api.post('/api/system/roles', data);
}

export async function editRole(data: Partial<RoleItem>) {
  return api.put('/api/system/roles', data);
}

export async function deleteRole(ids: number[]) {
  return api.delete('/api/system/roles', { data: ids });
}

export async function editRoleMenu(data: { id: number; menuIds: number[] }) {
  return api.put('/api/system/roles/menu', data);
}

export async function exportRoleExcel(params?: Record<string, unknown>) {
  return exportExcel({ api, url: '/api/system/roles/download', filename: '角色数据', params });
}
