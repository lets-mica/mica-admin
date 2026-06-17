import { api, parsePage } from '#/api/request';
import type { RoleItem } from './role';
import { exportExcel } from '#/utils/export-excel';

function serializeQueryParams(params: Record<string, unknown>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return;
    if (Array.isArray(value)) {
      value.forEach((item) => search.append(key, String(item)));
    } else {
      search.append(key, String(value));
    }
  });
  return search.toString();
}

export interface UserItem {
  id: number;
  userName: string;
  nickName: string;
  email?: string;
  phone?: string;
  gender?: number;
  enabled?: boolean;
  avatar?: string;
  remark?: string;
  createdAt?: string;
  deptId?: number;
  dept?: { id: number; name: string };
  roles?: { id: number; name: string; level?: number; dataScope?: string | number }[];
  posts?: { id: number; name: string }[];
}

export interface UserProfileForm {
    nickName: string;
    email?: string;
    phone?: string;
    gender?: number;
}

export type { RoleItem };

export async function getUserList(params?: {
  page?: number;
  size?: number;
  blurry?: string;
  deptId?: number;
  enabled?: boolean;
  createTime?: string[];
}) {
  const data = await api.get<any>('/api/system/users', {
    params,
    paramsSerializer: serializeQueryParams,
  });
  return parsePage<UserItem>(data);
}

/** 获取当前登录用户详情（后端无按 id 查询接口，走 /api/auth/info） */
export async function getUser(_id?: number) {
  const data = await api.get<any>('/api/auth/info');
  return data?.userInfo ?? data;
}

export async function addUser(data: Partial<UserItem>) {
  return api.post('/api/system/users', data);
}

export async function editUser(data: Partial<UserItem>) {
  return api.put('/api/system/users', data);
}

export async function editUserCenter(data: Partial<UserProfileForm>) {
    return api.put('/api/system/users/center', data);
}

export async function deleteUser(ids: number[]) {
  return api.delete('/api/system/users', { data: ids });
}

export async function updatePassword(oldPass: string, newPass: string) {
  return api.post('/api/system/users/updatePass', { oldPass, newPass });
}

export async function updateProfile(data: Partial<UserItem>) {
  return api.put('/api/system/users/center', data);
}

export async function getAllRole() {
  // /api/system/roles/all 直接返回 List<SysRole>，字段是 name
  const data = await api.get<any>('/api/system/roles/all');
  return data || [];
}

export async function getAllPost() {
  // /api/system/post/all 直接返回 List<SysPost>
  const data = await api.get<any>('/api/system/post/all');
  return data || [];
}

export async function exportUserExcel(params?: Record<string, unknown>) {
  return exportExcel({ api, url: '/api/system/users/download', filename: '用户数据', params });
}
