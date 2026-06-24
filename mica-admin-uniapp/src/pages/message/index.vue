<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useImStore } from '@/modules/im/stores/im'
import { getMessages, markAllRead, markRead } from '@/api/message'
import { formatRelative } from '@/utils/format'
import type { UserMessage } from '@/api/message'

const im = useImStore()
const sysMessages = ref<UserMessage[]>([])
const currentTab = ref<number>(0) // 0 = 消息, 1 = 会话
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    await Promise.all([
      im.loadConversations(),
      getMessages({ current: 1, size: 50 }).then((p) => (sysMessages.value = p.records || []))
    ])
  } finally {
    loading.value = false
  }
}

async function onTapMessage(m: UserMessage) {
  if (!m.read) {
    await markRead(m.id)
    m.read = true
    await im.refreshUnread()
  }
  // 跳转业务单据(1.0 仅示例)
  if (m.bizType && m.bizId) {
    uni.showToast({ title: `${m.bizType}#${m.bizId} 跳转待开发`, icon: 'none' })
  }
}

async function onMarkAll() {
  await markAllRead()
  sysMessages.value.forEach((m) => (m.read = true))
  await im.refreshUnread()
}

function onTabChange(i: number) {
  currentTab.value = i
}

function onStartChat() {
  uni.navigateTo({ url: '/modules/im/pages/start-chat' })
}

function onOpenConv(c: any) {
  const url =
    c.type === 'P2P'
      ? `/modules/im/pages/chat-window?type=p2p&peerId=${c.targetId}&convId=${c.id}`
      : `/modules/im/pages/chat-window?type=group&groupId=${c.targetId}&convId=${c.id}`
  uni.navigateTo({ url })
}

function onMessageIncoming() {
  load()
}

onMounted(() => {
  uni.$on('im:message', onMessageIncoming)
  uni.$on('im:system-message', onMessageIncoming)
  load()
})
onUnmounted(() => {
  uni.$off('im:message', onMessageIncoming)
  uni.$off('im:system-message', onMessageIncoming)
})
</script>

<template>
  <view class="page">
    <view class="tabs">
      <view class="tab" :class="{ active: currentTab === 0 }" @tap="onTabChange(0)">
        <text>消息</text>
        <view v-if="sysMessages.filter(m => !m.read).length > 0" class="dot" />
      </view>
      <view class="tab" :class="{ active: currentTab === 1 }" @tap="onTabChange(1)">
        <text>会话</text>
        <view v-if="im.unreadTotal > 0" class="dot" />
      </view>
      <view class="plus" @tap="onStartChat">+</view>
    </view>

    <!-- 消息 Tab -->
    <view v-if="currentTab === 0" class="list">
      <view v-if="sysMessages.length === 0" class="empty">
        <text>暂无消息</text>
      </view>
      <view
        v-for="m in sysMessages"
        :key="m.id"
        class="msg-row"
        :class="{ unread: !m.read }"
        @tap="onTapMessage(m)"
      >
        <view class="dot-unread" v-if="!m.read" />
        <view class="msg-body">
          <view class="msg-title">
            <text class="t">{{ m.title }}</text>
            <text class="time">{{ formatRelative(m.createdAt) }}</text>
          </view>
          <text class="msg-content text-ellipsis">{{ m.content }}</text>
        </view>
      </view>
      <view v-if="sysMessages.some(m => !m.read)" class="footer-btn">
        <button size="mini" @tap="onMarkAll">全部已读</button>
      </view>
    </view>

    <!-- 会话 Tab -->
    <view v-else class="list">
      <view v-if="im.conversations.length === 0" class="empty">
        <text>暂无会话,点击右上角 + 发起聊天</text>
      </view>
      <view
        v-for="c in im.sortedConversations"
        :key="c.id"
        class="conv-row"
        @tap="onOpenConv(c)"
      >
        <image class="avatar" :src="c.avatar || '/static/default-avatar.png'" />
        <view class="conv-body">
          <view class="conv-head">
            <text class="name">{{ c.title }}</text>
            <text class="time">{{ formatRelative(c.updatedAt) }}</text>
          </view>
          <view class="conv-msg">
            <text class="preview text-ellipsis">{{ c.lastMessage?.content || ' ' }}</text>
            <view v-if="c.unreadCount > 0" class="badge">
              <uni-badge :text="c.unreadCount > 99 ? '99+' : String(c.unreadCount)" />
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  background: #f5f6f8;
  min-height: 100vh;
}
.tabs {
  background: #fff;
  display: flex;
  padding: 0 30rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  .tab {
    flex: 1;
    text-align: center;
    padding: 24rpx 0;
    font-size: 30rpx;
    color: #8f959e;
    position: relative;
    &.active {
      color: $uni-color-primary;
      font-weight: 600;
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60rpx;
        height: 4rpx;
        background: $uni-color-primary;
        border-radius: 2rpx;
      }
    }
    .dot {
      position: absolute;
      top: 16rpx;
      right: 50%;
      margin-right: -36rpx;
      width: 14rpx;
      height: 14rpx;
      border-radius: 50%;
      background: #f53f3f;
    }
  }
  .plus {
    width: 80rpx;
    text-align: center;
    line-height: 80rpx;
    color: $uni-color-primary;
    font-size: 44rpx;
  }
}
.list {
  padding: 20rpx 24rpx;
}
.empty {
  text-align: center;
  color: #8f959e;
  padding: 100rpx 0;
}
.msg-row {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  display: flex;
  position: relative;
  .dot-unread {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;
    background: #f53f3f;
    margin-right: 16rpx;
    margin-top: 12rpx;
    flex-shrink: 0;
  }
  .msg-body {
    flex: 1;
  }
  .msg-title {
    display: flex;
    justify-content: space-between;
    .t {
      font-size: 30rpx;
      font-weight: 500;
    }
    .time {
      color: #8f959e;
      font-size: 22rpx;
    }
  }
  .msg-content {
    color: #555;
    font-size: 26rpx;
    margin-top: 6rpx;
    display: block;
  }
}
.footer-btn {
  text-align: center;
  padding: 20rpx 0;
}
.conv-row {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  display: flex;
  .avatar {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    background: #e8e8e8;
  }
  .conv-body {
    flex: 1;
    margin-left: 20rpx;
    overflow: hidden;
  }
  .conv-head {
    display: flex;
    justify-content: space-between;
    .name {
      font-size: 30rpx;
      font-weight: 500;
    }
    .time {
      color: #8f959e;
      font-size: 22rpx;
    }
  }
  .conv-msg {
    display: flex;
    align-items: center;
    margin-top: 6rpx;
    .preview {
      flex: 1;
      color: #555;
      font-size: 26rpx;
    }
    .badge {
      margin-left: 10rpx;
    }
  }
}
</style>