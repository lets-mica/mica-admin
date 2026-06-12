import { defineOverridesPreferences, preferences, updatePreferences } from '@vben/preferences';
import { useAccessStore } from '@vben/stores';

import { getPreferenceDefaultsApi } from '#/api/core';
import { mergePreferences, unflattenPreferences } from '#/utils/preference-codec';

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

/**
 * 拉取服务端系统默认偏好（sys_config）并合并到本地 state。
 * 失败时静默忽略，避免阻塞首屏。
 *
 * 调用前置条件：必须在 pinia 已安装且 token 已写入之后调用
 * （建议在 bootstrap() 完成后再调）。
 */
export async function loadServerPreferences(): Promise<void> {
  // 未登录时不请求，避免 401
  try {
    const accessStore = useAccessStore();
    if (!accessStore.accessToken) return;
  } catch {
    return;
  }
  try {
    const kv = await getPreferenceDefaultsApi();
    if (!kv || Object.keys(kv).length === 0) return;
    const serverObj = unflattenPreferences(kv);
    const merged = mergePreferences(preferences, serverObj);
    updatePreferences(merged);
  } catch (e) {
    // 接口失败时静默降级（仍走 localStorage + 默认值）
    console.warn('[preferences] load server defaults failed:', e);
  }
}
