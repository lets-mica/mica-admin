/**
 * IM 会话 + 消息 API
 *
 * 后端真实返回结构(无 R 包装):
 *   - 列表: 裸数组(不分页)
 *   - 对象: Map<String,Object> 包字段
 *   - 操作: Map<String,Object> 或 void
 *
 * 前端 VO 与后端 VO 字段差异较大(类型枚举/字段名),在 API 客户端做一次映射,
 * 避免污染 stores / pages / types。
 */
import { http } from '@/utils/request'
import type {
  ConversationVo,
  MessageVo,
  P2pConversationForm,
  MessageType
} from '@/types/im'

/** 消息历史分页查询参数(对齐后端 beforeId/size) */
export interface MessageQuery {
  beforeId?: number
  size?: number
}

/* ========== 后端原始 VO(仅用于映射,不对外暴露) ========== */
interface ServerConversationVo {
  id: string
  type: 'p2p' | 'group'
  peerUserId?: number
  peerUserName?: string
  peerAvatar?: string
  lastMsgId?: number
  lastMsgTime?: string
  lastMsgPreview?: string
  unreadCount: number
  top?: boolean
  mute?: boolean
}

interface ServerMessageVo {
  id: number
  conversationId: string
  senderId: number
  receiverId?: number
  msgType?: string
  content: string
  extra?: string
  status?: number
  serverReceivedAt?: string
  recallBy?: number
  recallAt?: string
}

/* ========== Server → Client 映射 ========== */
function toClientConversation(s: ServerConversationVo): ConversationVo {
  const type: 'P2P' | 'GROUP' = s.type === 'p2p' ? 'P2P' : 'GROUP'
  return {
    id: s.id,
    type,
    title: s.peerUserName,
    avatar: s.peerAvatar,
    targetId: type === 'P2P' ? s.peerUserId : undefined,
    lastMessage: s.lastMsgPreview
      ? ({
          id: 0,
          conversationId: s.id,
          senderId: 0,
          type: 'TEXT' as MessageType,
          content: s.lastMsgPreview,
          createdAt: s.lastMsgTime || '',
          serverReceivedAt: s.lastMsgTime || ''
        } as MessageVo)
      : undefined,
    unreadCount: s.unreadCount ?? 0,
    updatedAt: s.lastMsgTime || '',
    pinned: s.top,
    muted: s.mute
  }
}

function toClientMessage(s: ServerMessageVo): MessageVo {
  const msgType = (s.msgType || 'TEXT').toUpperCase() as MessageType
  let extra: Record<string, unknown> | undefined
  if (s.extra) {
    try {
      extra = typeof s.extra === 'string' ? JSON.parse(s.extra) : (s.extra as any)
    } catch {
      extra = undefined
    }
  }
  return {
    id: s.id,
    conversationId: s.conversationId,
    senderId: s.senderId,
    type: msgType,
    content: s.content,
    extra,
    recalled: s.recallBy != null,
    createdAt: s.serverReceivedAt || '',
    serverReceivedAt: s.serverReceivedAt || ''
  }
}

/* ========== 对外 API ========== */
export function createP2pConversation(form: P2pConversationForm) {
  // 后端 @PostMapping + @RequestBody,JSON body
  return http
    .post<{ conversation: ServerConversationVo }>('/api/im/conversations/p2p', form)
    .then((r) => ({ conversation: toClientConversation(r.conversation) }))
}

export function getConversations() {
  // 后端 GET /api/im/conversations 返回裸数组(不分页)
  return http
    .get<ServerConversationVo[]>('/api/im/conversations')
    .then((arr) => (arr || []).map(toClientConversation))
}

export function getMessages(convId: number | string, query: MessageQuery = {}) {
  return http
    .get<ServerMessageVo[]>(`/api/im/conversations/${convId}/messages`, query as Record<string, unknown>)
    .then((arr) => (arr || []).map(toClientMessage))
}

export function markRead(convId: number | string, toMsgId?: number) {
  // 后端是 @RequestParam Long toMsgId,要走 query
  return http.post<{ conversationId: string; unreadCount: number }>(
    `/api/im/conversations/${convId}/mark-read`,
    undefined,
    { params: toMsgId != null ? { toMsgId } : undefined }
  )
}

export function getUnreadTotal() {
  // 后端返回 Map{total: number},在 API 客户端解构为 number
  return http
    .get<{ total: number }>('/api/im/conversations/unread-total')
    .then((r) => r?.total ?? 0)
}

export function markAllRead() {
  return http.post<unknown>('/api/im/conversations/mark-all-read')
}

export function recallMessage(messageId: number) {
  return http.delete<unknown>(`/api/im/conversations/messages/${messageId}`)
}
