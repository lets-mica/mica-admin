/**
 * 监控 + Token 管理 API
 */
import { http } from '@/utils/request'

export interface ServerMonitor {
  cpu: { usage: number; cores: number }
  memory: { total: number; used: number; free: number }
  jvm: { heapUsed: number; heapMax: number; uptime: number }
  disk: { total: number; used: number }
  system: { os: string; hostname: string }
}

export function getServerMonitor() {
  return http.get<ServerMonitor>('/api/system/monitor/server')
}

export interface ImStats {
  onlineCount: number
}

export function getImOnlineStats() {
  return http.get<ImStats>('/admin/im/stats/online')
}

export interface TokenVo {
  id: number
  username: string
  nickname: string
  clientId: string
  issuedAt: string
  expiresAt: string
  ip?: string
  location?: string
  browser?: string
  os?: string
}

export function getTokens(params: { current?: number; size?: number }) {
  return http.get<import('@/utils/request').PageResult<TokenVo>>('/api/auth/token', params)
}

export function deleteToken(id: number) {
  return http.delete<void>(`/api/auth/token/${id}`)
}