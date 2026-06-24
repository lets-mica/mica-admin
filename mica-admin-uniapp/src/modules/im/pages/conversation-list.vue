<script setup lang="ts">
import { onMounted } from 'vue'
import { useImStore } from '../stores/im'

const im = useImStore()

onMounted(() => {
  im.loadConversations()
})

function open(c: any) {
  const url =
    c.type === 'P2P'
      ? `/modules/im/pages/chat-window?type=p2p&peerId=${c.targetId}&convId=${c.id}`
      : `/modules/im/pages/chat-window?type=group&groupId=${c.targetId}&convId=${c.id}`
  uni.navigateTo({ url })
}

function startChat() {
  uni.navigateTo({ url: '/modules/im/pages/start-chat' })
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">会话</text>
      <text class="add" @tap="startChat">+ 发起聊天</text>
    </view>

    <view v-if="im.conversations.length === 0" class="empty">
      <text>暂无会话</text>
    </view>

    <view
      v-for="c in im.sortedConversations"
      :key="c.id"
      class="row"
      @tap="open(c)"
    >
      <image class="avatar" :src="c.avatar || '/static/default-avatar.png'" />
      <view class="body">
        <view class="head">
          <text class="name">{{ c.title }}</text>
          <text class="time">{{ c.updatedAt.slice(11, 16) }}</text>
        </view>
        <view class="msg">
          <text class="preview text-ellipsis">{{ c.lastMessage?.content || ' ' }}</text>
          <view v-if="c.unreadCount > 0" class="badge">
            <uni-badge :text="c.unreadCount > 99 ? '99+' : String(c.unreadCount)" />
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
.header {
  display: flex;
  justify-content: space-between;
  padding: 30rpx;
  background: #fff;
  .title {
    font-size: 32rpx;
    font-weight: 600;
  }
  .add {
    color: $uni-color-primary;
    font-size: 28rpx;
  }
}
.empty {
  text-align: center;
  color: #8f959e;
  padding: 100rpx 0;
}
.row {
  background: #fff;
  padding: 24rpx;
  margin: 16rpx 24rpx;
  border-radius: 16rpx;
  display: flex;
  .avatar {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    background: #e8e8e8;
  }
  .body {
    flex: 1;
    margin-left: 20rpx;
    overflow: hidden;
  }
  .head {
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
  .msg {
    display: flex;
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