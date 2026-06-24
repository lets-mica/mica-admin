/**
 * 网络请求封装(基于 uni.request)
 * 注意 mica-admin 成功响应 code = 0(非 200)
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

export function request<T = unknown>(options: RequestOptions): Promise<T> {
  const { url, method = 'GET', data, params, header = {}, hideError, hideAuth } = options
  return new Promise((resolve, reject) => {
    const fullUrl = url.startsWith('http')
      ? buildUrl(url, params)
      : buildUrl(`${env.apiUrl}${url}`, params)

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
        const body = res.data as ApiResponse<T>
        // HTTP 401 → 清 token 跳登录
        if (res.statusCode === 401) {
          clearToken()
          uni.reLaunch({ url: '/modules/auth/pages/login' })
          reject(new Error('登录已过期'))
          return
        }
        // mica-admin 业务 code
        if (body && body.code === 0) {
          resolve(body.data)
          return
        }
        const msg = body?.msg || `请求失败 (${res.statusCode})`
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