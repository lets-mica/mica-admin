import type { Router } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';
import { useAccessStore, useUserStore } from '@vben/stores';
import { startProgress, stopProgress } from '@vben/utils';

import { accessRoutes, coreRouteNames } from '#/router/routes';
import { useAuthStore } from '#/store';

import { generateAccess } from './access';

/**
 * 通用守卫配置
 * @param router
 */
function setupCommonGuard(router: Router) {
  // 记录已经加载的页面
  const loadedPaths = new Set<string>();

  router.beforeEach((to) => {
    to.meta.loaded = loadedPaths.has(to.path);

    // 页面加载进度条
    if (!to.meta.loaded && preferences.transition.progress) {
      startProgress();
    }
    return true;
  });

  router.afterEach((to) => {
    // 记录页面是否加载,如果已经加载，后续的页面切换动画等效果不在重复执行

    loadedPaths.add(to.path);

    // 关闭页面加载进度条
    if (preferences.transition.progress) {
      stopProgress();
    }
  });
}

/**
 * 权限访问守卫配置
 * @param router
 */
function setupAccessGuard(router: Router) {
  router.beforeEach(async (to, from) => {
    const accessStore = useAccessStore();
    const userStore = useUserStore();
    const authStore = useAuthStore();

    // 1. accessToken 检查
    if (!accessStore.accessToken) {
      // 明确声明忽略权限访问权限，则可以访问
      if (to.meta.ignoreAccess) {
        return true;
      }

      // 没有访问权限，跳转登录页面
      if (to.fullPath !== LOGIN_PATH) {
        return {
          path: LOGIN_PATH,
          // 如不需要，直接删除 query
          query:
            to.fullPath === preferences.app.defaultHomePath
              ? {}
              : { redirect: encodeURIComponent(to.fullPath) },
          // 携带当前跳转的页面，登录后重新跳转该页面
          replace: true,
        };
      }
      return to;
    }

    // 2. 已登录但还没生成动态路由 → 必先生成（不区分目标是 coreRouteNames 还是后端菜单）。
    //    coreRouteNames 现在包含 Profile / UserMessage 这些挂在 Root 下的内建页面，
    //    它们走 BasicLayout + 侧栏，必须保证 accessMenus / dynamic routes 已就位，
    //    否则直访 /user/profile 或 /user/message 刷新后侧栏会消失。
    if (!accessStore.isAccessChecked) {
      const userInfo = userStore.userInfo || (await authStore.fetchUserInfo());
      const userRoles = userInfo.roles ?? [];
      const { accessibleMenus, accessibleRoutes } = await generateAccess({
        roles: userRoles,
        router,
        routes: accessRoutes,
      });
      accessStore.setAccessMenus(accessibleMenus);
      accessStore.setAccessRoutes(accessibleRoutes);
      accessStore.setIsAccessChecked(true);
    }

    // 3. 基本路由（Login / Fallback* / Profile / UserMessage 等）放行
    if (coreRouteNames.includes(to.name as string)) {
      // 已登录用户访问 /login 时直接跳到首页（或 redirect query）
      if (to.path === LOGIN_PATH && accessStore.accessToken) {
        return decodeURIComponent(
          (to.query?.redirect as string) ||
            preferences.app.defaultHomePath,
        );
      }
      return true;
    }

    // 4. 后端动态路由：用 router.resolve 强制重匹配（addRoute 后必须重新解析路径，
    //    否则 vue-router 会拿着未注册前的 to.matched 继续导航，最终落到 404）。
    //    动态路由注册前，全局 404 兜底 `/:path(.*)*` 会先吃掉 /dashboard 等路径，
    //    to.matched.length > 0 但 name 为 FallbackNotFound，必须重新 resolve。
    //    真正不存在的路径 resolve 后仍是 FallbackNotFound，此时直接放行展示 404。
    const redirectPath = (from.query.redirect ??
      (to.path === preferences.app.defaultHomePath
        ? preferences.app.defaultHomePath
        : to.fullPath)) as string;

    const resolved = router.resolve(decodeURIComponent(redirectPath));

    if (
      to.matched.length > 0 &&
      to.name !== 'FallbackNotFound' &&
      resolved.fullPath === to.fullPath
    ) {
      return true;
    }

    if (resolved.name === 'FallbackNotFound' && to.name === 'FallbackNotFound') {
      return true;
    }

    return {
      ...resolved,
      replace: true,
    };
  });
}

/**
 * 项目守卫配置
 * @param router
 */
function createRouterGuard(router: Router) {
  /** 通用 */
  setupCommonGuard(router);
  /** 权限访问 */
  setupAccessGuard(router);
}

export { createRouterGuard };
