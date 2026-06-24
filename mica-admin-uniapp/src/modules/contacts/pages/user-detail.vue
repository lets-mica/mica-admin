<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getUserDetail } from '@/api/user'
import { getPostName } from '@/api/user'
import type { UserVo } from '@/api/user'

const user = ref<UserVo | null>(null)

onLoad(async (q) => {
  if (q?.id) user.value = await getUserDetail(Number(q.id))
})

function callPhone() {
  if (!user.value?.phone) {
    uni.showToast({ title: '该用户未留电话', icon: 'none' })
    return
  }
  uni.makePhoneCall({
    phoneNumber: user.value.phone,
    fail: () => uni.showToast({ title: '拨号失败', icon: 'none' })
  })
}
</script>

<template>
  <view class="page">
    <view class="card head">
      <image class="avatar" :src="user?.avatar || '/static/default-avatar.png'" />
      <text class="name">{{ user?.nickName }}</text>
      <text class="username">@{{ user?.userName }}</text>
    </view>

    <view class="group">
      <view class="row">
        <text class="lbl">部门</text>
        <text class="val">{{ user?.dept?.name || '-' }}</text>
      </view>
      <view class="row">
        <text class="lbl">岗位</text>
        <text class="val">{{ getPostName(user) || '-' }}</text>
      </view>
      <view class="row">
        <text class="lbl">手机</text>
        <text class="val">{{ user?.phone || '-' }}</text>
      </view>
      <view class="row">
        <text class="lbl">邮箱</text>
        <text class="val">{{ user?.email || '-' }}</text>
      </view>
    </view>

    <view class="actions">
      <view class="btn" @tap="callPhone">
        <text class="ico">📞</text>
        <text>拨号</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 24rpx;
}
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 30rpx;
  text-align: center;
}
.head {
  margin-bottom: 24rpx;
  .avatar {
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    background: #e8e8e8;
  }
  .name {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
    margin-top: 20rpx;
  }
  .username {
    display: block;
    color: #8f959e;
    font-size: 24rpx;
    margin-top: 6rpx;
  }
}
.group {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
  .row {
    padding: 28rpx;
    display: flex;
    justify-content: space-between;
    border-bottom: 1rpx solid #f0f0f0;
    .lbl {
      color: #8f959e;
    }
    .val {
      color: #1f2329;
    }
    &:last-child {
      border-bottom: none;
    }
  }
}
.actions {
  display: flex;
  gap: 20rpx;
  .btn {
    flex: 1;
    background: #fff;
    border-radius: 16rpx;
    padding: 40rpx 0;
    text-align: center;
    .ico {
      display: block;
      font-size: 48rpx;
    }
    text {
      display: block;
      font-size: 26rpx;
      margin-top: 8rpx;
    }
  }
}
</style>