/**
 * 认证 API
 * 与后端 mica-captcha 库 CaptchaVo 字段对齐:uuid -> captchaId, base64 -> captchaImage
 * 登录表单字段名与后端 SecWebAuthenticationDetails 对齐:validateCodeId / validateCode
 */
import { http } from '@/utils/request'
import type { CurrentUser, MenuItem } from '@/types/common'

/** 后端 JwtUser 原始字段(对应 mica-admin JwtUser.java) */
interface JwtUserRaw {
  id?: number
  userName?: string
  nickName?: string
  avatar?: string
  email?: string
  phone?: string
  isAdmin?: boolean
  dept?: { id?: number; name?: string }
  roleList?: string[]
  permissions?: string[]
}

/** 后端 /api/auth/info 响应:{ userInfo, publicKey } */
interface AuthInfoResp {
  userInfo?: JwtUserRaw
  publicKey?: string
}

/** 后端 MenuVo 原始字段(对应 mica-admin MenuVo.java) */
interface MenuVoRaw {
  id: number
  parentId?: number
  name?: string
  path?: string
  hidden?: boolean
  redirect?: string
  component?: string
  alwaysShow?: boolean
  meta?: { title?: string; icon?: string; noCache?: boolean }
}

export interface CaptchaVo {
  captchaId: string
  captchaImage: string
}

export interface LoginForm {
  username: string
  password: string
  validateCodeId: string
  validateCode: string
}

export function getCaptcha() {
  return http.get<{ uuid?: string; base64?: string }>('/api/auth/captcha').then((res) => {
    // 后端 mica-captcha 库 CaptchaVo 字段为 uuid / base64,映射为前端友好的 captchaId / captchaImage
    return {
      captchaId: res?.uuid || '',
      captchaImage: res?.base64 || ''
    } as CaptchaVo
  })
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
  return http
    .get<AuthInfoResp>('/api/auth/info')
    .then((res) => {
      // 后端 /api/auth/info 返回 { userInfo, publicKey }, 需解出 userInfo 并映射字段
      const raw = res?.userInfo || (res as unknown as JwtUserRaw) || {}
      return {
        userId: raw.id ?? 0,
        username: raw.userName || '',
        nickname: raw.nickName || '',
        avatar: raw.avatar || '',
        email: raw.email || '',
        phone: raw.phone || '',
        deptId: raw.dept?.id,
        deptName: raw.dept?.name || '',
        postName: '',
        roles: raw.roleList || [],
        permissions: raw.permissions || [],
        isAdmin: !!raw.isAdmin
      } as CurrentUser
    })
}

export function getMenus() {
  return http.get<MenuVoRaw[]>('/api/auth/menus').then((list) => {
    if (!Array.isArray(list)) return [] as MenuItem[]
    return list.map((m) => ({
      id: m.id,
      parentId: m.parentId ?? 0,
      path: m.path || '',
      name: m.name || '',
      title: m.meta?.title || m.name || '',
      icon: m.meta?.icon,
      component: m.component,
      redirect: m.redirect,
      hidden: m.hidden,
      // 后端未返回 type 字段, 按是否有 children 猜:实际由前端动态路由处理
      type: 'MENU' as const,
      keepAlive: !(m.meta?.noCache ?? false)
    })) as MenuItem[]
  })
}