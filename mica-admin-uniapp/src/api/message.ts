/**
 * 系统消息 API
 */
import { http } from '@/utils/request'

export interface UserMessage {
  id: number
  title: string
  content: string
  category?: string
  bizId?: string
  bizType?: string
  read?: boolean
  createdAt: string
}

export function getUnreadMessages() {
  return http.get<UserMessage[]>('/api/system/user/message/unread')
}

export function getMessages(params: { current?: number; size?: number }) {
  return http.get<import('@/utils/request').PageResult<UserMessage>>('/api/system/user/message', params)
}

export function markRead(id: number) {
  return http.put<void>(`/api/system/user/message/read/${id}`)
}

export function markAllRead() {
  return http.put<void>('/api/system/user/message/read-all')
}