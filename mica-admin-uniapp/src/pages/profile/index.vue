<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { useImStore } from '@/modules/im/stores/im'

const auth = useAuthStore()
const im = useImStore()

interface MenuItem {
  icon: string
  label: string
  path?: string
  action?: 'checkUpdate' | 'settings' | 'help'
  color?: string
}

const groups: { items: MenuItem[] }[] = [
  {
    items: [
      { icon: '👤', label: '个人信息', path: '/pages/profile/edit', color: '#3B82F6' },
      { icon: '🔑', label: '修改密码', path: '/modules/profile/pages/update-pass', color: '#10B981' },
      { icon: '❓', label: '帮助中心', action: 'help', color: '#F59E0B' }
    ]
  },
  {
    items: [
      { icon: '💙', label: '关于我们', path: '/modules/profile/pages/about', color: '#3B82F6' },
      { icon: '⏰', label: '检查更新', action: 'checkUpdate', color: '#F97316' }
    ]
  },
  {
    items: [
      { icon: '⚙️', label: '系统设置', action: 'settings', color: '#3B82F6' }
    ]
  }
]

function onLogout() {
  uni.showModal({
    title: '提示',
    content: '确定退出登录?',
    success: async (res) => {
      if (res.confirm) {
        await auth.doLogout()
        im.disconnectMqtt()
        uni.reLaunch({ url: '/modules/auth/pages/login' })
      }
    }
  })
}

function onItemTap(item: MenuItem) {
  if (item.path) {
    uni.navigateTo({ url: item.path })
    return
  }
  switch (item.action) {
    case 'checkUpdate':
      uni.showToast({ title: '已是最新版本', icon: 'none' })
      break
    case 'help':
      uni.showToast({ title: '帮助中心建设中', icon: 'none' })
      break
    case 'settings':
      uni.showToast({ title: '系统设置建设中', icon: 'none' })
      break
  }
}
</script>

<template>
  <view class="page">
    <view class="header">
      <view class="avatar-wrap">
        <image
          class="avatar"
          :src="auth.user?.avatar || '/static/default-avatar.png'"
          mode="aspectFill"
        />
      </view>
      <view class="info">
        <text class="name">
          {{ auth.user?.nickname || auth.user?.username }}
          <text v-if="auth.user?.username" class="username">（{{ auth.user.username }}）</text>
        </text>
        <text v-if="auth.user?.deptName || auth.user?.postName" class="dept">
          {{ auth.user?.deptName || '' }}
          <text v-if="auth.user?.deptName && auth.user?.postName"> / </text>
          {{ auth.user?.postName || '' }}
        </text>
      </view>
    </view>

    <view class="menu-list">
      <view v-for="(group, gi) in groups" :key="gi" class="menu-group">
        <view
          v-for="(item, ii) in group.items"
          :key="ii"
          class="menu-row"
          hover-class="menu-row--hover"
          :hover-stay-time="50"
          @tap="onItemTap(item)"
        >
          <view class="menu-left">
            <view class="menu-icon" :style="{ color: item.color }">
              <text>{{ item.icon }}</text>
            </view>
            <text class="menu-label">{{ item.label }}</text>
          </view>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>

    <view class="logout-wrap">
      <view class="logout-btn" hover-class="logout-btn--hover" :hover-stay-time="50" @tap="onLogout">
        <text>退出登录</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f6f8;
  padding-bottom: 60rpx;
}

.header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 40rpx 60rpx;
}

.avatar-wrap {
  padding: 6rpx;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.06);
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #e8e8e8;
  display: block;
}

.info {
  margin-top: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 100%;
}

.name {
  font-size: 36rpx;
  color: #1f2329;
  font-weight: 600;
  line-height: 1.4;
  text-align: center;
}

.username {
  font-size: 26rpx;
  color: #8f959e;
  font-weight: 400;
}

.dept {
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #8f959e;
  line-height: 1.4;
  text-align: center;
}

.menu-list {
  margin: 0 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.menu-group {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.menu-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
  transition: background 0.15s;

  &:last-child {
    border-bottom: none;
  }

  &--hover {
    background: #f5f6f8;
  }
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.menu-icon {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  line-height: 1;
}

.menu-label {
  font-size: 30rpx;
  color: #1f2329;
}

.menu-arrow {
  color: #c0c4cc;
  font-size: 36rpx;
  line-height: 1;
}

.logout-wrap {
  margin: 40rpx 24rpx 0;
}

.logout-btn {
  background: #fff;
  border-radius: 16rpx;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  color: #f53f3f;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: background 0.15s;

  &--hover {
    background: #f5f6f8;
  }
}
</style>