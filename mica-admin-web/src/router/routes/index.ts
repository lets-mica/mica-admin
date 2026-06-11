import type { RouteRecordRaw } from 'vue-router';

import { coreRoutes, fallbackNotFoundRoute } from './core';

/**
 * 后端动态路由模式下，不再扫描 ./modules/**.ts，
 * 所有业务路由由 /api/auth/menus 返回后由 access.ts 拼装。
 */
const staticRoutes: RouteRecordRaw[] = [];
const externalRoutes: RouteRecordRaw[] = [];

/** 根路由列表：核心路由（登录/404/root）+ 外部路由 + 404 兜底 */
const routes: RouteRecordRaw[] = [
  ...coreRoutes,
  ...externalRoutes,
  fallbackNotFoundRoute,
];

/** 递归收集中文路由树中所有节点的 name */
function collectNames(routes: RouteRecordRaw[], out: (string | symbol | undefined)[] = []) {
  for (const r of routes) {
    out.push(r.name);
    if (r.children && r.children.length > 0) {
      collectNames(r.children, out);
    }
  }
  return out.filter((n) => n != null) as (string | symbol)[];
}

/** 核心路由 name 集合：这些路由永远不需要走权限拦截 */
const coreRouteNames = collectNames(coreRoutes);

/** 权限路由（后端模式下留空） */
const accessRoutes: RouteRecordRaw[] = [...staticRoutes];

export { accessRoutes, coreRouteNames, routes };
