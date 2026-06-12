/**
 * 动态路由生成器 —— 后端菜单驱动模式
 *
 * 复用 mica-admin 本地化的 @vben/access#generateAccessible('backend', ...)：
 *   1. fetchMenuListAsync() 拉取后端菜单（GET /api/auth/menus）
 *   2. convertRoutes: component === 'Layout' → layoutMap；其他 → pageMap
 *   3. accessible.ts 自动给顶级目录设 redirect、keep-alive 名字包装、404 fallback
 *   4. generateMenus 把路由转为菜单（菜单名读 meta.title，active 路径从
 *      router.getRoutes() 取规范化后的 path）
 *
 * 重要：fetchMenuListAsync 必须返回**树**结构（每个节点含 children），
 * vben 内部用 mapTree 对 children 递归；对扁平数组只做单层 transform，
 * 会导致菜单拍平、目录无法展开。
 *
 * 后端约定（与 vben RouteRecordStringComponent 对齐）：
 *   - 顶级菜单 component = 'Layout'（MenuVoUtil 自动填）
 *   - 叶子菜单 component = 'system/user/index' 等相对 views 路径
 *   - 外链 path 以 http(s):// 开头，component 为 null，转 meta.link 走新窗口
 *   - meta.noCache = true ↔ 关闭 keep-alive
 *   - meta.title / meta.icon 直接作为菜单显示名 / 图标
 *
 * path 处理：mica-admin 后端 MenuVoUtil 只对顶级菜单 path 加前导 '/',
 *   子菜单 path 仍是相对的 'user'、'role' 等。vben 在 accessible.ts 里
 *   对顶级目录设 redirect 的逻辑要求 firstChild.path.startsWith('/')，
 *   否则顶级目录访问时会渲染空白。所以这里把所有 path 拼成完整绝对路径
 *   后再交给 vben 处理。
 */
import type {
  GenerateMenuAndRoutesOptions,
  RouteRecordStringComponent,
} from '@vben/types';

import { generateAccessible } from '@vben/access';

import type { MenuVo } from '#/api/core/menu';

import { getAllMenusApi } from '#/api/core/menu';
import { BasicLayout } from '#/layouts';

const layoutMap = {
  Layout: BasicLayout,
};

const pageMap: Record<string, () => Promise<unknown>> = import.meta.glob(
  '../views/**/*.vue',
);

const forbiddenComponent = () =>
  import('#/views/_core/fallback/forbidden.vue');

const EXTERNAL_PATH_PREFIX = '/__external__';

function isExternalUrl(path: string | undefined): boolean {
  return !!path && /^https?:\/\//.test(path);
}

/** 将相对 path 拼接到父级完整 path 上，得到规范化的绝对 path */
function joinPath(parentFullPath: string, childPath: string): string {
  const cleanChild = childPath.replace(/^\/+/, '');
  if (!parentFullPath || parentFullPath === '/') {
    return `/${cleanChild}`;
  }
  return `${parentFullPath.replace(/\/+$/, '')}/${cleanChild}`;
}

interface TreeNode extends MenuVo {
  children: TreeNode[];
}

/** 后端 MenuVo 列表（扁平） -> 父子树 */
function buildTree(menus: MenuVo[]): TreeNode[] {
  const map = new Map<number, TreeNode>();
  for (const m of menus) {
    map.set(m.id, { ...m, children: [] });
  }
  const roots: TreeNode[] = [];
  for (const n of map.values()) {
    if (n.parentId == null || !map.has(n.parentId)) {
      roots.push(n);
    } else {
      map.get(n.parentId)!.children.push(n);
    }
  }
  return roots;
}

/**
 * 把 MenuVo 树转为 vben 期望的 RouteRecordStringComponent 树。
 * 在原地上升级每个节点的 path 为完整绝对路径，**保留 children 嵌套关系**。
 */
function upgradeTreePaths(
  roots: TreeNode[],
  parentFullPath = '',
): RouteRecordStringComponent[] {
  return roots.map((node) => {
    const title = node.meta?.title ?? node.name;
    const icon = node.meta?.icon ?? undefined;
    const noCache = node.meta?.noCache ?? false;
    const external = isExternalUrl(node.path);

    const finalPath = external
      ? `${EXTERNAL_PATH_PREFIX}/${node.id}`
      : joinPath(parentFullPath, node.path ?? '');

    const routeChildren =
      node.children.length > 0
        ? upgradeTreePaths(node.children, finalPath)
        : undefined;

    return {
      children: routeChildren,
      component: external ? undefined : node.component,
      meta: {
        hideInMenu: node.hidden,
        icon,
        keepAlive: !noCache,
        link: external ? node.path : undefined,
        title,
      },
      name: node.name,
      path: finalPath,
      redirect: node.redirect,
    } as unknown as RouteRecordStringComponent;
  });
}

async function fetchMenuListAsync(): Promise<RouteRecordStringComponent[]> {
  const menus = (await getAllMenusApi()) ?? [];
  const tree = buildTree(menus);
  return upgradeTreePaths(tree);
}

async function generateAccess(options: GenerateMenuAndRoutesOptions) {
  return generateAccessible('backend', {
    ...options,
    fetchMenuListAsync,
    forbiddenComponent,
    layoutMap,
    pageMap,
  });
}

export { generateAccess };
