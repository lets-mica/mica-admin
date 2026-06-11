/**
 * 菜单 API
 * 适配 mica-admin 后端
 */
import { api } from '#/api/request';

/**
 * 获取用户所有菜单
 * mica-admin 后端: GET /api/auth/menus
 */
export async function getAllMenusApi() {
  return api.get<any[]>('/api/auth/menus');
}
