<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getNoticeFeed } from '@/api/notice'
import { getUnreadMessages } from '@/api/message'
import { formatDateTime } from '@/utils/format'
import type { NoticeVo } from '@/api/notice'
import type { UserMessage } from '@/api/message'

const auth = useAuthStore()

const greeting = ref('')
const sysUnread = ref<UserMessage[]>([])
const notices = ref<NoticeVo[]>([])

function updateGreeting() {
  const h = new Date().getHours()
  if (h < 6) greeting.value = '夜深了'
  else if (h < 12) greeting.value = '早上好'
  else if (h < 14) greeting.value = '中午好'
  else if (h < 18) greeting.value = '下午好'
  else greeting.value = '晚上好'
}

const quickEntries = [
  { icon: '💬', name: '消息', path: '/pages/message/index' },
  { icon: '👥', name: '通讯录', path: '/modules/contacts/pages/index' },
  { icon: '📁', name: '文件', path: '/modules/file/pages/index' }
]

async function load() {
  updateGreeting()
  await Promise.all([
    getUnreadMessages().then((r) => (sysUnread.value = r || [])).catch(() => {}),
    getNoticeFeed({ current: 1, size: 3 }).then((p) => (notices.value = p.records || [])).catch(() => {})
  ])
}

function go(path: string) {
  uni.navigateTo({ url: path })
}

onMounted(() => {
  if (!auth.isLoggedIn) {
    uni.reLaunch({ url: '/modules/auth/pages/login' })
    return
  }
  load()
})
</script>

<template>
  <view class="workbench">
    <view class="header">
      <view class="user">
        <image class="avatar" :src="auth.user?.avatar || '/static/default-avatar.png'" />
        <view class="info">
          <text class="name">{{ greeting }}, {{ auth.user?.nickname || auth.user?.username }}</text>
          <text class="dept">{{ auth.user?.deptName || '未分配部门' }}</text>
        </view>
      </view>
    </view>

    <view class="card stat">
      <view class="stat-item">
        <text class="num">{{ sysUnread.length }}</text>
        <text class="label">系统未读</text>
      </view>
      <view class="stat-item">
        <text class="num">{{ notices.length }}</text>
        <text class="label">最新公告</text>
      </view>
    </view>

    <view class="card notices" v-if="notices.length">
      <view class="card-header">
        <text class="title">最新公告</text>
        <text class="more" @tap="go('/pages/message/index')">更多 ›</text>
      </view>
      <view v-for="n in notices" :key="n.id" class="notice-row" @tap="go(`/modules/notice/pages/detail?id=${n.id}`)">
        <text class="notice-title text-ellipsis">{{ n.title }}</text>
        <text class="notice-time">{{ formatDateTime(n.createdAt, 'MM-DD') }}</text>
      </view>
    </view>

    <view class="card">
      <view class="card-header">
        <text class="title">快捷入口</text>
      </view>
      <view class="quick-grid">
        <view v-for="q in quickEntries" :key="q.name" class="quick-item" @tap="go(q.path)">
          <text class="quick-icon">{{ q.icon }}</text>
          <text class="quick-name">{{ q.name }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.workbench {
  padding: 20rpx 24rpx 40rpx;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 10rpx 30rpx;
  .user {
    display: flex;
    align-items: center;
  }
  .avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    background: #e8e8e8;
  }
  .info {
    margin-left: 24rpx;
  }
  .name {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
  }
  .dept {
    display: block;
    color: #8f959e;
    font-size: 24rpx;
    margin-top: 4rpx;
  }
}
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  .card-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20rpx;
    .title {
      font-size: 30rpx;
      font-weight: 600;
    }
    .more {
      color: #8f959e;
      font-size: 24rpx;
    }
  }
}
.stat {
  display: flex;
  .stat-item {
    flex: 1;
    text-align: center;
    .num {
      display: block;
      font-size: 44rpx;
      font-weight: 600;
      color: $uni-color-primary;
    }
    .label {
      display: block;
      color: #8f959e;
      font-size: 24rpx;
      margin-top: 4rpx;
    }
  }
}
.notices {
  .notice-row {
    display: flex;
    justify-content: space-between;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
    &:last-child {
      border-bottom: none;
    }
    .notice-title {
      flex: 1;
      font-size: 28rpx;
    }
    .notice-time {
      color: #8f959e;
      font-size: 24rpx;
      margin-left: 20rpx;
    }
  }
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30rpx 0;
  .quick-item {
    text-align: center;
    position: relative;
    .quick-icon {
      display: block;
      font-size: 48rpx;
    }
    .quick-name {
      display: block;
      font-size: 24rpx;
      margin-top: 8rpx;
      color: #1f2329;
    }
    .quick-badge {
      position: absolute;
      top: -8rpx;
      right: 16rpx;
    }
  }
}
</style>