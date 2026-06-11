import { api, parsePage } from '#/api/request';
import { exportExcel } from '#/utils/export-excel';

export interface MenuItem {
  id: number;
  parentId?: number;
  /** 菜单标题（给用户看的） */
  title?: string;
  /** 菜单/路由名称（英文标识） */
  name: string;
  /** 菜单类型：0=目录 1=菜单 2=按钮 */
  type: number;
  path?: string;
  component?: string;
  icon?: string;
  permission?: string;
  /** 显示顺序 */
  seq?: number;
  /** 排序（部分版本字段名） */
  sort?: number;
  /** 是否外链 0否 1是 */
  isFrame?: boolean;
  /** 是否缓存 0否 1是 */
  cache?: boolean;
  /** 显示状态 0显示 1隐藏 */
  hidden?: boolean;
  /** 菜单状态 0正常 1停用 */
  status?: number;
  remark?: string;
  createdAt?: string;
  children?: MenuItem[];
}

export async function getMenuList(params?: {
  page?: number;
  size?: number;
  blurry?: string;
}) {
  const data = await api.get<any>('/api/system/menus', { params });
  return parsePage<MenuItem>(data);
}

export async function getMenuTree() {
  return api.get<any>('/api/system/menus/all');
}

export async function addMenu(data: Partial<MenuItem>) {
  return api.post('/api/system/menus', data);
}

export async function editMenu(data: Partial<MenuItem>) {
  return api.put('/api/system/menus', data);
}

export async function deleteMenu(ids: number | number[]) {
  return api.delete('/api/system/menus', { data: Array.isArray(ids) ? ids : [ids] });
}

export async function exportMenuExcel(params?: Record<string, unknown>) {
  return exportExcel({ api, url: '/api/system/menus/download', filename: '菜单数据', params });
}
