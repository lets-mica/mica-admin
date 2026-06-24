/**
 * 聊天窗口 store(单会话)
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { markRead as apiMarkRead, getMessages as apiGetMessages, recallMessage as apiRecall } from '@/api/im/conversation'
import { mqttClient } from '../mqtt-client'
import { MqttTopic, type MessageVo, type MessageType } from '@/types/im'
import { uuid } from '@/utils/uuid'
import { useAuthStore } from '@/stores/auth'
import type { ImUserVo } from '@/types/im'

export type ChatType = 'P2P' | 'GROUP'

export interface PendingMessage {
  clientMsgId: string
  type: MessageType
  content: string
  status: 'sending' | 'sent' | 'failed'
  ts: number
  id: number
  conversationId: number
  senderId: number
  senderName?: string
  recalled?: boolean
  createdAt: string
  serverReceivedAt: string
  _pending: true
}

export const useChatStore = defineStore('im-chat', () => {
  const auth = useAuthStore()
  // 后端雪花 id 为 String,前端保持 string
  const convId = ref<string>('')
  const chatType = ref<ChatType>('P2P')
  /** 单聊为对端 userId;群聊为 groupId(都是 number/Long) */
  const peerId = ref<number>(0)
  const groupMembers = ref<ImUserVo[]>([])

  const messages = ref<MessageVo[]>([])
  const pending = ref<PendingMessage[]>([])
  const loading = ref(false)
  const hasMore = ref(true)

  const allMessages = computed<MessageVo[]>(() => {
    // 合并正式消息 + 发送中消息,按 serverReceivedAt 升序
    const merged: MessageVo[] = [...messages.value]
    pending.value.forEach((p) => merged.push(p))
    return merged.sort(
      (a, b) => +new Date(a.serverReceivedAt) - +new Date(b.serverReceivedAt)
    )
  })

  async function open(type: ChatType, target: number, cId?: string | number) {
    chatType.value = type
    peerId.value = target
    convId.value = cId ? String(cId) : ''
    messages.value = []
    pending.value = []
    hasMore.value = true
    await Promise.all([loadHistory(), onEnter()])
  }

  async function loadHistory() {
    if (!convId.value || loading.value) return
    loading.value = true
    try {
      // 后端返回裸数组(不分页),分页用 beforeId/size
      const list = await apiGetMessages(convId.value, { size: 30 })
      messages.value = list || []
      hasMore.value = (list?.length ?? 0) >= 30
      // 进入即标记已读
      if (messages.value.length) {
        await apiMarkRead(convId.value, messages.value[0].id)
      }
    } finally {
      loading.value = false
    }
  }

  async function onEnter() {
    // 订阅收件箱
    const userId = auth.user?.userId
    if (!userId) return
    if (chatType.value === 'P2P') {
      mqttClient.publish = mqttClient.publish.bind(mqttClient)
    }
  }

  function sendText(text: string) {
    if (!text.trim() || !peerId.value || !auth.user) return
    const clientMsgId = uuid()
    const nowIso = new Date().toISOString()
    const pendingMsg: PendingMessage = {
      clientMsgId,
      type: 'TEXT',
      content: text,
      status: 'sending',
      ts: Date.now(),
      id: 0,
      conversationId: convId.value,
      senderId: auth.user.userId,
      senderName: auth.user.nickname,
      createdAt: nowIso,
      serverReceivedAt: nowIso,
      _pending: true
    }
    pending.value.push(pendingMsg)
    const payload = {
      type: 'TEXT' as MessageType,
      content: text,
      clientMsgId,
      sentAt: nowIso,
      conversationId: convId.value
    }
    try {
      if (chatType.value === 'P2P') {
        mqttClient.publish(
          MqttTopic.p2pSend(auth.user.userId, peerId.value),
          payload
        )
      } else {
        mqttClient.publish(`im/group/${peerId.value}/inbox`, payload)
      }
      // 标记为 sent(实际等待对端 ack;1.0 简化:发完即视为成功)
      const p = pending.value.find((x) => x.clientMsgId === clientMsgId)
      if (p) p.status = 'sent'
    } catch (e) {
      const p = pending.value.find((x) => x.clientMsgId === clientMsgId)
      if (p) p.status = 'failed'
      throw e
    }
  }

  async function recall(messageId: number) {
    await apiRecall(messageId)
    const m = messages.value.find((x) => x.id === messageId)
    if (m) m.recalled = true
  }

  function reset() {
    convId.value = ''
    peerId.value = 0
    chatType.value = 'P2P'
    messages.value = []
    pending.value = []
  }

  return {
    convId,
    chatType,
    peerId,
    groupMembers,
    messages,
    pending,
    loading,
    hasMore,
    allMessages,
    open,
    loadHistory,
    sendText,
    recall,
    reset
  }
})