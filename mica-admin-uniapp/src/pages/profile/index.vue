<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { useImStore } from '@/modules/im/stores/im'

const auth = useAuthStore()
const im = useImStore()

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

function onItemTap(path: string) {
  uni.navigateTo({ url: path })
}
</script>

<template>
  <view class="page">
    <view class="header">
      <image class="avatar" :src="auth.user?.avatar || '/static/default-avatar.png'" />
      <view class="info">
        <text class="name">{{ auth.user?.nickname || auth.user?.username }}</text>
        <text class="dept">{{ auth.user?.deptName }} · {{ auth.user?.postName || '员工' }}</text>
      </view>
    </view>

    <view class="group">
      <view class="row" @tap="onItemTap('/modules/profile/pages/update-pass')">
        <text class="lbl">修改密码</text>
        <text class="arrow">›</text>
      </view>
      <view class="row" @tap="onItemTap('/modules/profile/pages/update-email')">
        <text class="lbl">修改邮箱</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <view class="group">
      <view class="row" @tap="onItemTap('/modules/contacts/pages/index')">
        <text class="lbl">通讯录</text>
        <text class="arrow">›</text>
      </view>
      <view class="row" @tap="onItemTap('/modules/file/pages/index')">
        <text class="lbl">文件中心</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <view class="group">
      <view class="row" @tap="onItemTap('/modules/profile/pages/about')">
        <text class="lbl">关于</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <button class="logout" @tap="onLogout">退出登录</button>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 24rpx;
}
.header {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
  .avatar {
    width: 128rpx;
    height: 128rpx;
    border-radius: 50%;
    background: #e8e8e8;
  }
  .info {
    margin-left: 30rpx;
  }
  .name {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
  }
  .dept {
    display: block;
    color: #8f959e;
    font-size: 26rpx;
    margin-top: 6rpx;
  }
}
.group {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
  .row {
    padding: 30rpx;
    display: flex;
    justify-content: space-between;
    border-bottom: 1rpx solid #f0f0f0;
    &:last-child {
      border-bottom: none;
    }
    .arrow {
      color: #c0c4cc;
    }
  }
}
.logout {
  margin-top: 40rpx;
  background: #fff;
  color: #f53f3f;
  border-radius: 16rpx;
  height: 88rpx;
  line-height: 88rpx;
}
</style>