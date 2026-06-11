import { api } from '#/api/request';
import { exportExcel } from '#/utils/export-excel';

export interface DeptItem {
  id: number;
  name: string;
  parentId?: number | null;
  parentName?: string;
  leader?: string;
  phone?: string;
  email?: string;
  seq?: number;
  enabled?: number | boolean;
  hasChildren?: boolean;
  createdAt?: string;
  children?: DeptItem[];
}

export function isRootDept(parentId?: number | null) {
  return parentId === undefined || parentId === null || parentId === 0;
}

export function buildDeptTree(items: DeptItem[], parentId?: number | null): DeptItem[] {
  return items
    .filter((item) =>
      isRootDept(parentId) ? isRootDept(item.parentId) : item.parentId === parentId,
    )
    .map((item) => {
      const children = buildDeptTree(items, item.id);
      return {
        ...item,
        children: children.length > 0 ? children : undefined,
      };
    });
}

function parseDeptList(data: unknown): DeptItem[] {
  if (!data || typeof data !== 'object') return [];
  const page = data as Record<string, unknown>;
  if (Array.isArray(page.records)) return page.records as DeptItem[];
  if (Array.isArray(page.content)) return page.content as DeptItem[];
  if (Array.isArray(page.data)) return page.data as DeptItem[];
  if (Array.isArray(data)) return data as DeptItem[];
  return [];
}

export async function getDeptList(params?: {
  name?: string;
  enabled?: number;
  pid?: number;
  createTime?: string[];
}) {
  const data = await api.get<any>('/api/system/dept', { params });
  const list = parseDeptList(data);
  return { list, total: list.length };
}

export async function getDeptTree(params?: { enabled?: number; name?: string }) {
  const { list } = await getDeptList(params);
  return buildDeptTree(list);
}

export async function getDepts(params?: Record<string, unknown>) {
  const { list } = await getDeptList(params);
  return list;
}

export async function getDeptSuperior(ids: number | number[]) {
  const idList = Array.isArray(ids) ? ids : [ids];
  const data = await api.post<any>('/api/system/dept/superior', idList);
  return (data || []) as DeptItem[];
}

export async function addDept(data: Partial<DeptItem>) {
  return api.post('/api/system/dept', data);
}

export async function editDept(data: Partial<DeptItem>) {
  return api.put('/api/system/dept', data);
}

export async function deleteDept(ids: number | number[]) {
  const idList = Array.isArray(ids) ? ids : [ids];
  return api.delete('/api/system/dept', { data: idList });
}

export async function exportDeptExcel(params?: Record<string, unknown>) {
  return exportExcel({ api, url: '/api/system/dept/download', filename: '部门数据', params });
}
