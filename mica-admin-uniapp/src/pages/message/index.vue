<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getNoticeFeed } from '@/api/notice'
import { getMessages, markAllRead, markRead } from '@/api/message'
import { formatRelative } from '@/utils/format'
import type { NoticeVo } from '@/api/notice'
import type { UserMessage } from '@/api/message'

const currentTab = ref<number>(0)
const loading = ref(false)

const notices = ref<NoticeVo[]>([])
const sysMessages = ref<UserMessage[]>([])
const unreadMsgCount = computed(() => sysMessages.value.filter(m => !m.read).length)

async function loadNotices() {
  try {
    const p = await getNoticeFeed({ current: 1, size: 50 })
    notices.value = p.records || []
  } catch {
    notices.value = []
  }
}

async function loadMessages() {
  try {
    const p = await getMessages({ current: 1, size: 50 })
    sysMessages.value = p.records || []
  } catch {
    sysMessages.value = []
  }
}

async function load() {
  loading.value = true
  try {
    await Promise.all([loadNotices(), loadMessages()])
  } finally {
    loading.value = false
  }
}

async function onTapMessage(m: UserMessage) {
  if (!m.read) {
    await markRead(m.id)
    m.read = true
  }
}

async function onMarkAll() {
  await markAllRead()
  sysMessages.value.forEach((m) => (m.read = true))
}

function onTapNotice(n: NoticeVo) {
  uni.navigateTo({ url: `/modules/notice/pages/detail?id=${n.id}` })
}

function noticeTypeLabel(type?: number) {
  return type === 2 ? '公告' : '通知'
}

onMounted(() => {
  load()
})
</script>

<template>
  <view class="page">
    <view class="tabs">
      <view class="tab" :class="{ active: currentTab === 0 }" @tap="currentTab = 0">
        <text>公告</text>
      </view>
      <view class="tab" :class="{ active: currentTab === 1 }" @tap="currentTab = 1">
        <text>系统消息</text>
        <view v-if="unreadMsgCount > 0" class="badge">
          <text>{{ unreadMsgCount > 99 ? '99+' : unreadMsgCount }}</text>
        </view>
      </view>
    </view>

    <!-- 公告 Tab -->
    <view v-if="currentTab === 0" class="list">
      <view v-if="notices.length === 0" class="empty">
        <text>暂无公告</text>
      </view>
      <view
        v-for="n in notices"
        :key="n.id"
        class="notice-row"
        @tap="onTapNotice(n)"
      >
        <view class="notice-head">
          <view class="tag" :class="{ announce: n.type === 2 }">
            <text>{{ noticeTypeLabel(n.type) }}</text>
          </view>
          <text class="title text-ellipsis">{{ n.title }}</text>
          <text class="time">{{ formatRelative(n.createdAt) }}</text>
        </view>
        <text class="content text-ellipsis">{{ n.content }}</text>
      </view>
    </view>

    <!-- 系统消息 Tab -->
    <view v-else class="list">
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
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8rpx;
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
    .badge {
      min-width: 32rpx;
      height: 32rpx;
      padding: 0 8rpx;
      border-radius: 16rpx;
      background: #f53f3f;
      color: #fff;
      font-size: 20rpx;
      line-height: 32rpx;
      text-align: center;
    }
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
.notice-row {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  .notice-head {
    display: flex;
    align-items: center;
    gap: 12rpx;
    .tag {
      flex-shrink: 0;
      padding: 4rpx 12rpx;
      border-radius: 6rpx;
      background: #e8f4ff;
      color: $uni-color-primary;
      font-size: 22rpx;
      line-height: 1.4;
      &.announce {
        background: #fff4e6;
        color: #fa8c16;
      }
      text {
        white-space: nowrap;
      }
    }
    .title {
      flex: 1;
      font-size: 30rpx;
      font-weight: 500;
      color: #1f2329;
    }
    .time {
      flex-shrink: 0;
      color: #8f959e;
      font-size: 22rpx;
    }
  }
  .content {
    display: block;
    color: #555;
    font-size: 26rpx;
    margin-top: 8rpx;
  }
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
</style>