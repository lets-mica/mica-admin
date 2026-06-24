/**
 * MQTT 客户端配置(mica-admin IM 模块使用)
 * 协议详见 docs/im/api-design.md
 */
import { env } from '@/config/env'
import { getToken } from './request'
import { storage } from './storage'

export const MQTT_CONFIG = {
  // 开发: Vite proxy /mqtt -> ws://localhost:8083
  // 生产: 与后端 8083 同源或 nginx 反代
  url: env.mqttUrl.startsWith('ws') ? env.mqttUrl : `${env.mqttUrl}`,
  get fullUrl(): string {
    // H5 通过 Vite proxy
    if (typeof window !== 'undefined') {
      return `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}${env.mqttUrl}`
    }
    // App / 小程序 → 直接连后端 8083
    return `ws://localhost:8083/mqtt`
  },
  options: {
    clean: true,
    reconnectPeriod: 3000,
    connectTimeout: 30 * 1000,
    keepalive: 60
  }
}

/**
 * 构建 clientId: app-{userId}-{uuid}
 */
export function buildClientId(userId: number): string {
  let uuid = storage.get<string>('mqtt-client-uuid')
  if (!uuid) {
    uuid = Math.random().toString(36).slice(2) + Date.now().toString(36)
    storage.set('mqtt-client-uuid', uuid)
  }
  return `app-${userId}-${uuid}`
}

/**
 * 构建当前用户的订阅 topic 集合
 */
export function buildSubscribeTopics(userId: number, myGroupIds: number[]): string[] {
  return [
    `im/p2p/${userId}/inbox`,
    `im/sys/${userId}/system`,
    `im/status/${userId}/state`,
    ...myGroupIds.map((id) => `im/group/${id}/inbox`)
  ]
}

/**
 * 单聊发送 topic
 */
export function p2pSendTopic(fromId: number, toId: number): string {
  return `im/p2p/${fromId}/to/${toId}`
}

/**
 * 获取当前连接 username(JWT)
 */
export function getMqttUsername(): string {
  return getToken()
}