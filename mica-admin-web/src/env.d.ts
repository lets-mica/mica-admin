/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<object, object, unknown>
  export default component
}

interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_APP_NAMESPACE: string
  readonly VITE_APP_STORE_SECURE_KEY: string
  readonly VITE_PORT: string
  readonly VITE_BASE: string
  readonly VITE_GLOB_API_URL: string
  readonly VITE_ROUTER_HISTORY: string
  readonly VITE_INJECT_APP_LOADING: string
  readonly VITE_DEVTOOLS: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
