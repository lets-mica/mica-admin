/**
 * 系统用户/个人中心 API
 */
import { http } from '@/utils/request'

export interface SysDept {
  id: number
  parentId: number
  name: string
  sort: number
  children?: SysDept[]
}

export interface UserVo {
  userId: number
  username: string
  nickname: string
  avatar?: string
  email?: string
  phone?: string
  deptId?: number
  deptName?: string
  postName?: string
}

export function getDepts() {
  return http.get<SysDept[]>('/api/system/dept')
}

export function getUsers(params: { current?: number; size?: number; blurry?: string; deptId?: number }) {
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