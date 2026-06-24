<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPostName, getUsers } from '@/api/user'
import type { UserVo } from '@/api/user'

const users = ref<UserVo[]>([])

onLoad(async (q) => {
  if (q?.deptId) {
    const p = await getUsers({ current: 1, size: 500, deptId: Number(q.deptId) })
    users.value = p.records || []
  }
})

function onUserTap(u: UserVo) {
  uni.navigateTo({ url: `/modules/contacts/pages/user-detail?id=${u.id}` })
}
</script>

<template>
  <view class="page">
    <view v-if="users.length === 0" class="empty">
      <text>暂无成员</text>
    </view>
    <view v-for="u in users" :key="u.id" class="row" @tap="onUserTap(u)">
      <image class="avatar" :src="u.avatar || '/static/default-avatar.png'" />
      <view class="info">
        <text class="name">{{ u.nickName }}</text>
        <text class="post">{{ getPostName(u) || '' }}</text>
      </view>
      <text class="arrow">›</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 24rpx;
}
.empty {
  text-align: center;
  color: #8f959e;
  padding: 80rpx 0;
}
.row {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  display: flex;
  align-items: center;
  .avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: #e8e8e8;
  }
  .info {
    flex: 1;
    margin-left: 20rpx;
  }
  .name {
    display: block;
    font-size: 28rpx;
  }
  .post {
    display: block;
    color: #8f959e;
    font-size: 24rpx;
    margin-top: 4rpx;
  }
  .arrow {
    color: #c0c4cc;
  }
}
</style>