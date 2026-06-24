/**
 * mica-admin IM 模块类型定义
 */
export interface ConversationVo {
  // 后端雪花 id 为 String,避免 JS number 精度丢失
  id: string
  type: 'P2P' | 'GROUP'
  title?: string
  avatar?: string
  /** P2P 时为对端 userId;GROUP 时为 groupId(后端目前未返回 groupId,Group 会话跳详情页需后续补) */
  targetId?: number
  lastMessage?: MessageVo
  unreadCount: number
  /** ISO 字符串,后端 lastMsgTime 序列化 */
  updatedAt: string
  pinned?: boolean
  muted?: boolean
}

export type MessageType = 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM'

export interface MessageVo {
  /** 后端雪花 id 在 JS 侧用 number 表示仍有 2^53 精度风险,本项目 ID <= 1e16 时勉强 OK;严格场景请改 string */
  id: number
  /** 后端返回 String(雪花/UUID),前端保持 string */
  conversationId: string
  senderId: number
  senderName?: string
  type: MessageType
  content: string
  extra?: Record<string, unknown>
  /** 后端 recallBy != null → recalled: true(API 客户端映射) */
  recalled?: boolean
  createdAt: string
  serverReceivedAt: string
}

export interface GroupVo {
  id: number
  name: string
  avatar?: string
  ownerId: number
  announcement?: string
  memberCount: number
  myRole: 'OWNER' | 'ADMIN' | 'MEMBER'
  createdAt: string
}

export interface GroupMemberVo {
  userId: number
  username: string
  nickname: string
  avatar?: string
  role: 'OWNER' | 'ADMIN' | 'MEMBER'
  joinedAt: string
  muted?: boolean
}

export interface ImUserVo {
  userId: number
  username: string
  nickname: string
  avatar?: string
  deptName?: string
}

export interface P2pConversationForm {
  peerUserId: number
}

export interface GroupCreateForm {
  name: string
  memberIds: number[]
}

export interface GroupUpdateForm {
  name?: string
  announcement?: string
}

/** 客户端发送的消息负载 */
export interface MqttMessagePayload {
  type: MessageType
  content: string
  clientMsgId: string
  sentAt: string
  extra?: Record<string, unknown>
}

/** MQTT topic 集合 */
export const MqttTopic = {
  p2pInbox: (userId: number) => `im/p2p/${userId}/inbox`,
  p2pSend: (fromId: number, toId: number) => `im/p2p/${fromId}/to/${toId}`,
  groupInbox: (groupId: number) => `im/group/${groupId}/inbox`,
  sys: (userId: number) => `im/sys/${userId}/system`,
  status: (userId: number) => `im/status/${userId}/state`
} as const