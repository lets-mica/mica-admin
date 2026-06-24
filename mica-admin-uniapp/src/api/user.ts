/**
 * 系统用户/个人中心 API
 */
import { http } from '@/utils/request'

/**
 * 部门(对应后端 SysDept)
 * @see net.dreamlu.mica.admin.project.system.entity.SysDept
 */
export interface SysDept {
  id: number
  parentId?: number
  name: string
  /** 显示顺序(后端字段 seq) */
  seq?: number
  leader?: string
  phone?: string
  email?: string
  enabled?: number | boolean
  children?: SysDept[]
}

/**
 * 用户 VO(对应后端 UserVo)
 * @see net.dreamlu.mica.admin.project.system.pojo.UserVo
 *
 * 注意:后端 UserVo 返回的是嵌套对象(dept)与数组(posts/roles),
 * 业务页面常取的"部门名 / 岗位名"在前端按需取首个或拼接。
 */
export interface UserVo {
  id: number
  deptId?: number
  dept?: { id: number; name: string }
  posts?: { id: number; name: string; code?: string }[]
  roles?: { id: number; name: string; level?: number; dataScope?: string | number }[]
  userName: string
  nickName: string
  email?: string
  phone?: string
  gender?: number
  avatar?: string
  isAdmin?: boolean
  enabled?: boolean
  locked?: boolean
  remark?: string
}

/**
 * 取用户岗位名称(取第一个岗位)
 */
export function getPostName(u: UserVo | null | undefined): string {
  if (!u?.posts?.length) return ''
  return u.posts[0]?.name || ''
}

/**
 * 取用户部门名称
 */
export function getDeptName(u: UserVo | null | undefined): string {
  return u?.dept?.name || ''
}

export function getDepts() {
  return http.get<SysDept[]>('/api/system/dept')
}

export function getUsers(params: {
  current?: number
  size?: number
  blurry?: string
  deptId?: number
  enabled?: boolean
}) {
  return http.get<import('@/utils/request').PageResult<UserVo>>('/api/system/users', params)
}

export function getUserDetail(id: number) {
  return http.get<UserVo>(`/api/system/users/${id}`)
}

export function getCurrentCenter() {
  return http.get<UserVo>('/api/system/users/center')
}

export function updatePass(form: { oldPassword: string; newPassword: string }) {
  return http.put<void>('/api/system/users/updatePass', form)
}

export function updateEmail(form: { email: string; code: string }) {
  return http.put<void>('/api/system/users/updateEmail', form)
}

export function updateAvatar(avatarUrl: string) {
  return http.put<void>('/api/system/users/avatar', { avatar: avatarUrl })
}

export function requestEmailCode(email: string) {
  return http.post<void>('/api/system/code/resetEmail', { email })
}
