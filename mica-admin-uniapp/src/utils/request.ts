/**
 * 网络请求封装(基于 uni.request)
 * mica-admin 后端响应约定:
 *   - 成功: 直接返回数据体,无 R 包装
 *     · 分页: { records: [...], total: 0 }  (Mybatis-Plus IPage)
 *     · 对象: { ... }
 *     · 列表: [...]
 *   - 失败: R 包装 { code: 非0, msg: '...', data: null }
 *     或标准 Spring 错误 { status, error, message, path }
 */
import { env } from '@/config/env'
import { storage } from './storage'

type AnyObject = Record<string, unknown>

export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data: T
}

export interface PageRequest {
  current?: number
  size?: number
  [key: string]: unknown
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

export interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: unknown
  params?: Record<string, unknown>
  header?: Record<string, string>
  hideError?: boolean
  hideAuth?: boolean
}

const TOKEN_KEY = 'mica-admin-token'

export function getToken(): string {
  return storage.get<string>(TOKEN_KEY) || ''
}

export function setToken(token: string): void {
  storage.set(TOKEN_KEY, token)
}

export function clearToken(): void {
  storage.remove(TOKEN_KEY)
}

function buildUrl(url: string, params?: Record<string, unknown>): string {
  if (!params) return url
  const query = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
    .join('&')
  if (!query) return url
  return url.includes('?') ? `${url}&${query}` : `${url}?${query}`
}

function joinBaseUrl(base: string, path: string): string {
  const cleanBase = base.replace(/\/+$/, '')
  let p = path.startsWith('/') ? path : `/${path}`
  if (cleanBase && p.startsWith(`${cleanBase}/`)) {
    p = p.slice(cleanBase.length)
  }
  return `${cleanBase}${p}`
}

/**
 * 判断是否为 mica-admin 的 R 包装失败响应。
 * R 包装特征: { code: number, msg: string, data?: unknown } 且 code !== 0(默认 0 表示成功)
 */
function isRFailResponse(body: unknown): body is { code: number; msg: string; data?: unknown } {
  if (!body || typeof body !== 'object') return false
  const obj = body as Record<string, unknown>
  // 必须是单一对象(非数组),且包含 code 字段
  if (Array.isArray(obj)) return false
  if (!('code' in obj) || typeof obj.code !== 'number') return false
  return obj.code !== 0
}

function extractErrorMessage(body: unknown): string {
  if (!body || typeof body !== 'object') return ''
  const obj = body as Record<string, unknown>
  return (obj.msg as string) || (obj.message as string) || (obj.error as string) || ''
}

export function request<T = unknown>(options: RequestOptions): Promise<T> {
  const { url, method = 'GET', data, params, header = {}, hideError, hideAuth } = options
  return new Promise((resolve, reject) => {
    const fullUrl = url.startsWith('http')
      ? buildUrl(url, params)
      : buildUrl(joinBaseUrl(env.apiUrl, url), params)

    const reqHeader: Record<string, string> = { ...header }
    if (!hideAuth) {
      const token = getToken()
      if (token) reqHeader['Authorization'] = `Bearer ${token}`
    }

    uni.request({
      url: fullUrl,
      method,
      data: data as AnyObject | undefined,
      header: reqHeader,
      timeout: 30000,
      success: (res) => {
        const body = res.data
        // HTTP 401 → 清 token 跳登录
        if (res.statusCode === 401) {
          clearToken()
          uni.reLaunch({ url: '/modules/auth/pages/login' })
          reject(new Error('登录已过期'))
          return
        }
        // 1) R 包装的失败响应
        if (isRFailResponse(body)) {
          const msg = extractErrorMessage(body) || `请求失败 (${res.statusCode})`
          if (!hideError) {
            uni.showToast({ title: msg, icon: 'none', duration: 2500 })
          }
          reject(new Error(msg))
          return
        }
        // 2) 成功响应(裸数据体,无 R 包装)
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(body as T)
          return
        }
        // 3) 其他 HTTP 失败
        const msg = extractErrorMessage(body) || `请求失败 (${res.statusCode})`
        if (!hideError) {
          uni.showToast({ title: msg, icon: 'none', duration: 2500 })
        }
        reject(new Error(msg))
      },
      fail: (err) => {
        if (!hideError) {
          uni.showToast({ title: err.errMsg || '网络异常', icon: 'none' })
        }
        reject(err)
      }
    })
  })
}

export const http = {
  get<T = unknown>(url: string, params?: Record<string, unknown>, options?: Partial<RequestOptions>) {
    return request<T>({ ...options, url, method: 'GET', params })
  },
  post<T = unknown>(url: string, data?: unknown, options?: Partial<RequestOptions>) {
    return request<T>({ ...options, url, method: 'POST', data })
  },
  put<T = unknown>(url: string, data?: unknown, options?: Partial<RequestOptions>) {
    return request<T>({ ...options, url, method: 'PUT', data })
  },
  delete<T = unknown>(url: string, data?: unknown, options?: Partial<RequestOptions>) {
    return request<T>({ ...options, url, method: 'DELETE', data })
  }
}