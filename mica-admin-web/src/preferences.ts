import { defineOverridesPreferences } from '@vben/preferences';

/**
 * @description 项目配置文件
 * 只需要覆盖项目中的一部分配置，不需要的配置不用覆盖，会自动使用默认配置
 * !!! 更改配置后请清空缓存，否则可能不生效
 */
export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    accessMode: 'backend',
    // 后端菜单仪表盘叶子路由为 /dashboard/analytics，非目录 /dashboard
    defaultHomePath: '/dashboard/analytics',
    name: import.meta.env.VITE_APP_TITLE,
  },
  logo: {
    // 浅色主题 logo（黑色），使用 public 目录下的本地 SVG
    source: '/logo.svg',
    // 暗色主题 logo（白色），使用 public 目录下的本地 SVG
    sourceDark: '/logo-dark.svg',
  },
});
