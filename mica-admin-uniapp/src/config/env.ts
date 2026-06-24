/**
 * 全局环境配置(基于 import.meta.env 的类型化封装)
 */
export interface AppEnv {
  title: string
  namespace: string
  apiUrl: string
  port: string
  routerHistory: string
  storeSecureKey: string
}

export const env: AppEnv = {
  title: import.meta.env.VITE_APP_TITLE || 'MICA Work',
  namespace: import.meta.env.VITE_APP_NAMESPACE || 'mica-admin-app',
  apiUrl: import.meta.env.VITE_GLOB_API_URL || '/api',
  port: import.meta.env.VITE_PORT || '5889',
  routerHistory: import.meta.env.VITE_ROUTER_HISTORY || 'hash',
  storeSecureKey: import.meta.env.VITE_APP_STORE_SECURE_KEY || 'mica-admin-app-secure-key'
}

export const isH5 = typeof window !== 'undefined'
export const isMp = typeof wx !== 'undefined' && typeof uni !== 'undefined' && !isH5
export const isApp = !isH5 && !isMp && typeof plus !== 'undefined'