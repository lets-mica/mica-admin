/**
 * 系统消息 API
 *
 * 后端 mica-admin-server: java 字段 `Boolean readFlag` → JSON `readFlag: boolean`
 * (改造前为 `'0' | '1'` 字符串,本层做兼容)
 */
import { http, type PageResult } from '@/utils/request'

/** 后端原始结构(兼容新旧两套值类型) */
interface UserMessageRaw {
  id: number
  messageId?: number
  title: string
  content: string
  category?: string
  /**
   * 已读状态
   * - 新版后端:boolean(true/false)
   * - 旧版后端:字符串 '0' / '1'(兼容期)
   */
  readFlag?: boolean | '0' | '1' | string
  createdAt: string
}

/** 前端消费结构 */
export interface UserMessage {
  id: number
  title: string
  content: string
  category?: string
  read: boolean
  createdAt: string
}

/** 把后端值归一为 boolean */
function computeRead(raw: UserMessageRaw['readFlag']): boolean {
  if (typeof raw === 'boolean') return raw
  if (typeof raw === 'string') return raw === '1'
  if (typeof raw === 'number') return raw === 1
  return false
}

function normalize(r: UserMessageRaw): UserMessage {
  return {
    id: r.id,
    title: r.title,
    content: r.content,
    category: r.category,
    read: computeRead(r.readFlag),
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
