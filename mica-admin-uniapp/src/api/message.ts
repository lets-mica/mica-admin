/**
 * 系统消息 API
 *
 * 后端 mica-admin-server 返回的字段是 `readFlag: '0' | '1'`(字符串),
 * 本层在 API 边界统一映射为 `read: boolean`,消费方不再关心字段名。
 */
import { http, type PageResult } from '@/utils/request'

/** 后端原始结构 */
interface UserMessageRaw {
  id: number
  messageId?: number
  title: string
  content: string
  category?: string
  readFlag: '0' | '1'
  createdAt: string
}

/** 前端消费结构(已标准化) */
export interface UserMessage {
  id: number
  title: string
  content: string
  category?: string
  /** 已标准化为布尔 */
  read: boolean
  createdAt: string
}

function normalize(r: UserMessageRaw): UserMessage {
  return {
    id: r.id,
    title: r.title,
    content: r.content,
    category: r.category,
    read: r.readFlag === '1',
    createdAt: r.createdAt
  }
}

function normalizePage(page: PageResult<UserMessageRaw>): PageResult<UserMessage> {
  return {
    ...page,
    records: page.records.map(normalize)
  }
}

export function getUnreadMessages() {
  return http.get<UserMessageRaw[]>('/api/system/user/message/unread').then((list) => list.map(normalize))
}

export function getMessages(params: { current?: number; size?: number }) {
  return http.get<PageResult<UserMessageRaw>>('/api/system/user/message', params).then(normalizePage)
}

export function markRead(id: number) {
  return http.put<void>(`/api/system/user/message/read/${id}`)
}

export function markAllRead() {
  return http.put<void>('/api/system/user/message/read-all')
}
