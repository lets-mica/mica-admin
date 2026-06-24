/**
 * IM 会话 + 消息 API
 */
import { http } from '@/utils/request'
import type {
  ConversationVo,
  MessageVo,
  P2pConversationForm,
  MarkReadForm
} from '@/types/im'

export function createP2pConversation(form: P2pConversationForm) {
  return http.post<{ conversation: ConversationVo }>('/api/im/conversations/p2p', form)
}

export function getConversations(params: { current?: number; size?: number } = {}) {
  return http.get<import('@/utils/request').PageResult<ConversationVo>>(
    '/api/im/conversations',
    params
  )
}

export function getMessages(convId: number, params: { current?: number; size?: number } = {}) {
  return http.get<import('@/utils/request').PageResult<MessageVo>>(
    `/api/im/conversations/${convId}/messages`,
    params
  )
}

export function markRead(convId: number, form: MarkReadForm) {
  return http.post<void>(`/api/im/conversations/${convId}/mark-read`, form)
}

export function getUnreadTotal() {
  return http.get<number>('/api/im/conversations/unread-total')
}

export function markAllRead() {
  return http.post<void>('/api/im/conversations/mark-all-read')
}

export function recallMessage(messageId: number) {
  return http.delete<void>(`/api/im/conversations/messages/${messageId}`)
}