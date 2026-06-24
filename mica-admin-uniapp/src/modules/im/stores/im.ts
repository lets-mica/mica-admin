/**
 * IM 全局 store
 * - 会话列表
 * - 未读总数
 * - MQTT 连接管理
 * - 收到消息分发到对应 chat store
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { mqttClient } from '../mqtt-client'
import {
  getConversations,
  getUnreadTotal,
  markAllRead as apiMarkAllRead
} from '@/api/im/conversation'
import { getMyGroups } from '@/api/im/group'
import type { ConversationVo, GroupVo, MessageVo } from '@/types/im'
import { useAuthStore } from '@/stores/auth'

export const useImStore = defineStore('im', () => {
  const conversations = ref<ConversationVo[]>([])
  const myGroups = ref<GroupVo[]>([])
  const unreadTotal = ref(0)
  const connected = ref(false)
  const loading = ref(false)

  const sortedConversations = computed(() =>
    [...conversations.value].sort(
      (a, b) => +new Date(b.updatedAt) - +new Date(a.updatedAt)
    )
  )

  const hasUnread = computed(() => unreadTotal.value > 0)

  async function loadConversations() {
    loading.value = true
    try {
      // 后端返回裸数组(不分页)
      const list = await getConversations()
      conversations.value = list || []
    } finally {
      loading.value = false
    }
  }

  async function loadMyGroups() {
    const groups = await getMyGroups()
    myGroups.value = groups || []
    return myGroups.value
  }

  async function refreshUnread() {
    try {
      // API 客户端已解构 {total} 为 number
      unreadTotal.value = (await getUnreadTotal()) ?? 0
    } catch {
      // ignore
    }
  }

  async function connectMqtt() {
    const auth = useAuthStore()
    if (!auth.user) return
    await loadMyGroups()
    mqttClient.onMessage(handleIncoming)
    mqttClient.connect(auth.user.userId, myGroups.value.map((g) => g.id))
    connected.value = true
  }

  function disconnectMqtt() {
    mqttClient.disconnect()
    connected.value = false
  }

  function handleIncoming(topic: string, payload: MessageVo) {
    // 收到新消息 → 更新对应会话 + 未读
    if (topic.includes('/inbox')) {
      upsertConversationFromMessage(payload)
      if (payload.senderId !== useAuthStore().user?.userId) {
        unreadTotal.value += 1
      }
      // 通知当前打开的聊天窗口(由 chat store 监听)
      uni.$emit('im:message', payload)
    } else if (topic.includes('/system')) {
      uni.$emit('im:system-message', payload)
    }
  }

  function upsertConversationFromMessage(msg: MessageVo) {
    const idx = conversations.value.findIndex((c) => c.id === msg.conversationId)
    if (idx >= 0) {
      conversations.value[idx].lastMessage = msg
      conversations.value[idx].updatedAt = msg.serverReceivedAt || msg.createdAt
      if (msg.senderId !== useAuthStore().user?.userId) {
        conversations.value[idx].unreadCount += 1
      }
    }
  }

  async function markAllRead() {
    await apiMarkAllRead()
    conversations.value.forEach((c) => (c.unreadCount = 0))
    unreadTotal.value = 0
  }

  return {
    conversations,
    myGroups,
    unreadTotal,
    connected,
    loading,
    sortedConversations,
    hasUnread,
    loadConversations,
    loadMyGroups,
    refreshUnread,
    connectMqtt,
    disconnectMqtt,
    markAllRead
  }
})