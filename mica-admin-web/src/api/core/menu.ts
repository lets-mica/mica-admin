/**
 * 菜单 API
 * 适配 mica-admin 后端 /api/auth/menus
 *
 * 后端返回的是扁平列表（按 parent_id/seq 排序），前端需要再 buildRoutes 时
 * 拼成一棵树。字段说明：
 *   id         : 菜单主键
 *   parentId   : 父菜单主键（顶层为 null 或 0）
 *   name       : 路由 name，前端用作 vue-router 的 name（必须唯一）
 *   path       : 路由 path，顶层已由后端自动加前缀 "/"
 *   component  : 组件路径字符串。"Layout" → BasicLayout；"IFrame" → IFrameView；
 *                其他值则映射到 views/ 下 .vue 文件
 *   icon       : 菜单图标，格式 "lucide:xxx"（或旧图标名，但前端建议统一 lucide）
 *   hidden     : true 时不在侧边栏显示，但路由仍可访问
 *   redirect   : 可空，指定子路由默认跳转
 *   meta       : { title, icon, noCache } —— 其中 noCache 对应前端 keepAlive=false
 *   isFrame    : 是否外链（true 时 path 必须是 http(s):// 开头，组件走 IFrameView）
 */
import { api } from '#/api/request';

export interface MenuMetaVo {
  title: string;
  icon?: string;
  noCache?: boolean;
}

export interface MenuVo {
  id: number;
  parentId?: number | null;
  name: string;
  path: string;
  component?: string;
  icon?: string;
  hidden?: boolean;
  redirect?: string;
  alwaysShow?: boolean;
  isFrame?: boolean;
  meta?: MenuMetaVo;
}

/**
 * 获取当前登录用户的菜单
 */
export async function getAllMenusApi() {
  return api.get<MenuVo[]>('/api/auth/menus');
}
