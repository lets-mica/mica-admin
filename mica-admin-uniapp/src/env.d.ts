/// <reference types="@dcloudio/types" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<Record<string, unknown>, Record<string, unknown>, unknown>
  export default component
}

interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_APP_NAMESPACE: string
  readonly VITE_GLOB_API_URL: string
  readonly VITE_GLOB_MQTT_URL: string
  readonly VITE_PORT: string
  readonly VITE_ROUTER_HISTORY: string
  readonly VITE_APP_STORE_SECURE_KEY: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

declare const wx: {
  [key: string]: any
} | undefined

declare const plus: {
  [key: string]: any
} | undefined