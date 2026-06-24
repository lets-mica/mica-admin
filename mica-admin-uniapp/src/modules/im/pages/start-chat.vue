<script setup lang="ts">
import { ref } from 'vue'
import UserPicker from '../components/UserPicker.vue'
import { createP2pConversation } from '@/api/im/conversation'
import { useGroupStore } from '../stores/group'
import type { ImUserVo } from '@/types/im'

const showSinglePicker = ref(false)
const showMultiPicker = ref(false)

async function onP2pConfirm(users: ImUserVo[]) {
  if (users.length === 0) return
  const peer = users[0]
  const { conversation } = await createP2pConversation({ peerUserId: peer.userId })
  uni.redirectTo({
    url: `/modules/im/pages/chat-window?type=p2p&peerId=${peer.userId}&convId=${conversation.id}`
  })
}

function onMultiConfirm() {
  // 多选 → 跳转创建群(传递 picked 不便,这里走 picker 已选状态)
  uni.redirectTo({ url: '/modules/im/pages/group-create' })
}
</script>

<template>
  <view class="page">
    <view class="grid">
      <view class="entry" @tap="showSinglePicker = true">
        <text class="ico">👤</text>
        <text class="t">单聊</text>
      </view>
      <view class="entry" @tap="showMultiPicker = true">
        <text class="ico">👥</text>
        <text class="t">群聊</text>
      </view>
    </view>

    <view class="tip">
      <text>单聊:选择 1 人发起私聊</text>
      <text>群聊:选择多人自动创建新群</text>
    </view>

    <UserPicker
      v-model="showSinglePicker"
      title="选择联系人"
      :multiple="false"
      @confirm="onP2pConfirm"
    />
    <UserPicker
      v-model="showMultiPicker"
      title="选择成员(进入创建群)"
      :multiple="true"
      @confirm="onMultiConfirm"
    />
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 40rpx 24rpx;
}
.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}
.entry {
  background: #fff;
  border-radius: 16rpx;
  padding: 60rpx 0;
  text-align: center;
  .ico {
    display: block;
    font-size: 80rpx;
  }
  .t {
    display: block;
    margin-top: 12rpx;
    font-size: 28rpx;
  }
}
.tip {
  margin-top: 40rpx;
  text-align: center;
  color: #8f959e;
  font-size: 24rpx;
  text {
    display: block;
    margin-top: 8rpx;
  }
}
</style>