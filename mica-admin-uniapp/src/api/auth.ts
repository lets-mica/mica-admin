/**
 * 认证 API
 */
import { http } from '@/utils/request'

export interface CaptchaVo {
  captchaId: string
  expression: string
}

export interface LoginForm {
  username: string
  password: string
  captchaId: string
  captchaCode: string
}

export function getCaptcha() {
  return http.get<CaptchaVo>('/api/auth/captcha')
}

export function getPublicKey() {
  return http.get<string>('/api/auth/public-key')
}

export function loginByPassword(form: LoginForm) {
  // form-urlencoded
  return http.post<{ token: string }>('/api/session', form, {
    header: { 'content-type': 'application/x-www-form-urlencoded' }
  })
}

export function logout() {
  return http.get<void>('/api/logout')
}

export function getCurrentUser() {
  return http.get<import('@/types/common').CurrentUser>('/api/auth/info')
}

export function getMenus() {
  return http.get<import('@/types/common').MenuItem[]>('/api/auth/menus')
}