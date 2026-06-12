import { initPreferences } from '@vben/preferences';
import { unmountGlobalLoading } from '@vben/utils';

import './styles/global.css';
import { loadServerPreferences, overridesPreferences } from './preferences';

/**
 * 应用初始化完成之后再进行页面加载渲染
 */
async function initApplication() {
  const env = import.meta.env.PROD ? 'prod' : 'dev';
  const appVersion = import.meta.env.VITE_APP_VERSION;
  const namespace = `${import.meta.env.VITE_APP_NAMESPACE}-${appVersion}-${env}`;

  // app偏好设置初始化（localStorage + 默认值）
  await initPreferences({
    namespace,
    overrides: overridesPreferences,
  });

  // 启动应用并挂载（不等服务端，fire-and-forget）
  const { bootstrap } = await import('./bootstrap');
  bootstrap(namespace);

  // 异步合并服务端系统默认偏好（失败静默降级）
  loadServerPreferences();

  // 移除并销毁loading
  unmountGlobalLoading();
}

initApplication();
