/**
 * 监控 + Token 管理 API
 */
import { http } from '@/utils/request'

/**
 * 服务器监控响应(后端 Map<String,Object>,字段对齐 MonitorServiceImpl#getServers)
 * @see net.dreamlu.mica.admin.project.system.service.impl.MonitorServiceImpl#getServers
 */
export interface ServerMonitor {
  /** 当前时间 HH:mm:ss */
  time: string
  /** 系统信息 */
  sys: {
    os: string
    ip: string
    day: string
  }
  /** CPU(used/idle 是 DecimalFormat 字符串) */
  cpu: {
    name: string
    package: string
    core: string
    coreNumber: number
    logic: string
    used: string
    idle: string
  }
  /** 内存(总量/已用/可用为 FormatUtil 字符串,使用率是 % ) */
  memory: {
    total: string
    used: string
    available: string
    usageRate: string
  }
  /** 交换区 */
  swap: {
    total: string
    used: string
    available: string
    usageRate: string
  }
  /** 磁盘 */
  disk: {
    total: string
    used: string
    available: string
    usageRate: string
  }
}

export function getServerMonitor() {
  return http.get<ServerMonitor>('/api/system/monitor/server')
}

/**
 * IM 在线人数
 * @see ImStatsController#online -> { totalOnline: number }
 */
export interface ImStats {
  totalOnline: number
}

export function getImOnlineStats() {
  return http.get<ImStats>('/admin/im/stats/online')
}

/**
 * 在线 Token
 * @see net.dreamlu.mica.admin.framework.vo.TokenVo
 */
export interface TokenVo {
  /** token key(用于踢人) */
  key: string
  /** token 摘要 */
  summary?: string
  userName: string
  nickName: string
  dept?: string
  browser?: string
  ip?: string
  address?: string
  loginTime: string
}

export function getTokens(params: { current?: number; size?: number; filter?: string }) {
  return http.get<import('@/utils/request').PageResult<TokenVo>>('/api/auth/token', params)
}

/**
 * 踢出指定 token(后端:DELETE /api/auth/token + body keys: string[])
 */
export function deleteToken(key: string) {
  return http.delete<void>('/api/auth/token', [key])
}
