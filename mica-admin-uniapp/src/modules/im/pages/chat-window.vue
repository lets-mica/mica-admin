<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useChatStore } from '../stores/chat'
import { useAuthStore } from '@/stores/auth'

const chat = useChatStore()
const auth = useAuthStore()

const chatType = ref<'p2p' | 'group'>('p2p')
const peerId = ref(0)
const convId = ref<string>('') // 后端雪花 id 为 String
const input = ref('')
const scrollTop = ref(0)

onLoad(async (q) => {
  chatType.value = (q?.type as any) || 'p2p'
  peerId.value = Number(q?.peerId || q?.groupId || 0)
  convId.value = q?.convId ? String(q.convId) : ''
  if (peerId.value) {
    await chat.open(chatType.value === 'p2p' ? 'P2P' : 'GROUP', peerId.value, convId.value)
    uni.setNavigationBarTitle({ title: chatType.value === 'p2p' ? '聊天' : '群聊' })
  }
})

function onIncoming(payload: any) {
  if (chatType.value === 'p2p') {
    if (payload.senderId === peerId.value) {
      chat.messages.push(payload)
      nextTick(scrollToBottom)
    }
  } else {
    chat.messages.push(payload)
    nextTick(scrollToBottom)
  }
}

function scrollToBottom() {
  // 用 pageScrollTo 触底
  setTimeout(() => {
    uni.pageScrollTo({ scrollTop: 99999, duration: 0 })
  }, 50)
}

async function send() {
  const text = input.value.trim()
  if (!text) return
  input.value = ''
  chat.sendText(text)
  nextTick(scrollToBottom)
}

function onRecall(m: any) {
  if (m.senderId !== auth.user?.userId) return
  uni.showActionSheet({
    itemList: ['撤回消息'],
    success: async (res) => {
      if (res.tapIndex === 0) {
        try {
          await chat.recall(m.id)
        } catch (e: any) {
          uni.showToast({ title: e.message || '超过 2 分钟无法撤回', icon: 'none' })
        }
      }
    }
  })
}

function onPeer() {
  // 单聊 → 跳用户详情
  if (chatType.value === 'p2p') {
    uni.navigateTo({ url: `/modules/contacts/pages/user-detail?id=${peerId.value}` })
  } else {
    uni.navigateTo({ url: `/modules/im/pages/group-detail?id=${peerId.value}` })
  }
}

onMounted(() => {
  uni.$on('im:message', onIncoming)
  nextTick(scrollToBottom)
})
onUnmounted(() => {
  uni.$off('im:message', onIncoming)
  chat.reset()
})
</script>

<template>
  <view class="page">
    <view class="header" @tap="onPeer">
      <text class="title">{{ chatType === 'p2p' ? '聊天' : '群聊' }}</text>
      <text class="arrow">⋮</text>
    </view>

    <scroll-view scroll-y class="messages" :scroll-top="scrollTop">
      <view
        v-for="m in chat.allMessages"
        :key="m.id || (m as any).clientMsgId || `${m.senderId}-${m.serverReceivedAt}`"
        class="msg-row"
        :class="{ self: m.senderId === auth.user?.userId }"
      >
        <view v-if="m.recalled" class="recall">[消息已撤回]</view>
        <view v-else class="bubble" @longpress="onRecall(m)">
          <text>{{ m.content }}</text>
          <text v-if="(m as any)._pending && (m as any).status === 'sending'" class="status">发送中…</text>
          <text v-else-if="(m as any)._pending && (m as any).status === 'failed'" class="status danger">发送失败</text>
        </view>
      </view>
    </scroll-view>

    <view class="composer">
      <input v-model="input" class="ipt" placeholder="输入消息" confirm-type="send" @confirm="send" />
      <button size="mini" class="send-btn" @tap="send">发送</button>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f6f8;
}
.header {
  background: #fff;
  padding: 24rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #f0f0f0;
  .title {
    font-size: 30rpx;
    font-weight: 500;
  }
  .arrow {
    color: #8f959e;
    font-size: 36rpx;
  }
}
.messages {
  flex: 1;
  padding: 20rpx;
}
.msg-row {
  display: flex;
  margin-bottom: 20rpx;
  &.self {
    justify-content: flex-end;
    .bubble {
      background: $uni-color-primary;
      color: #fff;
    }
  }
}
.bubble {
  max-width: 70%;
  background: #fff;
  border-radius: 16rpx;
  padding: 16rpx 24rpx;
  font-size: 28rpx;
  word-break: break-all;
  .status {
    display: block;
    color: #8f959e;
    font-size: 22rpx;
    margin-top: 6rpx;
    &.danger {
      color: #f53f3f;
    }
  }
}
.recall {
  color: #8f959e;
  font-size: 24rpx;
  text-align: center;
  width: 100%;
}
.composer {
  background: #fff;
  padding: 16rpx 24rpx;
  display: flex;
  align-items: center;
  border-top: 1rpx solid #f0f0f0;
  .ipt {
    flex: 1;
    background: #f5f6f8;
    border-radius: 8rpx;
    padding: 16rpx 20rpx;
    font-size: 28rpx;
  }
  .send-btn {
    margin-left: 16rpx;
    background: $uni-color-primary;
    color: #fff;
  }
}
</style>