/**
 * mica-admin IM 模块类型定义
 */
export interface ConversationVo {
  id: number
  type: 'P2P' | 'GROUP'
  title?: string
  avatar?: string
  targetId?: number
  lastMessage?: MessageVo
  unreadCount: number
  updatedAt: string
  pinned?: boolean
  muted?: boolean
}

export type MessageType = 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM'

export interface MessageVo {
  id: number
  conversationId: number
  senderId: number
  senderName?: string
  type: MessageType
  content: string
  extra?: Record<string, unknown>
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

export interface MarkReadForm {
  lastReadMessageId: number
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