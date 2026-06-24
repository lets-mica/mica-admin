<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useGroupStore } from '../stores/group'
import { useAuthStore } from '@/stores/auth'
import UserPicker from '../components/UserPicker.vue'

const group = useGroupStore()
const auth = useAuthStore()

const groupId = ref(0)
const showPicker = ref(false)

onLoad(async (q) => {
  if (q?.id) {
    groupId.value = Number(q.id)
    await group.loadDetail(groupId.value)
    uni.setNavigationBarTitle({ title: group.current?.name || '群信息' })
  }
})

async function onInviteConfirm(users: any[]) {
  if (users.length === 0) return
  await group.invite(groupId.value, users.map((u) => u.userId))
  await group.loadDetail(groupId.value)
  uni.showToast({ title: '已邀请', icon: 'success' })
}

function onKick(userId: number) {
  uni.showModal({
    title: '提示',
    content: '踢出该成员?',
    success: async (res) => {
      if (res.confirm) {
        await group.kick(groupId.value, userId)
      }
    }
  })
}

function onDissolve() {
  uni.showModal({
    title: '解散群聊',
    content: '解散后无法恢复,确定?',
    success: async (res) => {
      if (res.confirm) {
        await group.dissolve(groupId.value)
        uni.navigateBack()
      }
    }
  })
}

const canManage = () => {
  const role = group.current?.myRole
  return role === 'OWNER' || role === 'ADMIN'
}
</script>

<template>
  <view class="page" v-if="group.current">
    <view class="head">
      <text class="avatar">👥</text>
      <text class="name">{{ group.current.name }} ({{ group.current.memberCount }})</text>
    </view>

    <view class="group">
      <view class="row">
        <text class="lbl">群公告</text>
        <text class="val">{{ group.current.announcement || '暂无' }}</text>
      </view>
    </view>

    <view class="group">
      <view class="row-title">
        <text>成员 ({{ group.members.length }})</text>
        <text v-if="canManage()" class="add" @tap="showPicker = true">+ 邀请</text>
      </view>
      <view v-for="m in group.members" :key="m.userId" class="member">
        <image class="avatar" :src="m.avatar || '/static/default-avatar.png'" />
        <view class="info">
          <text class="name">
            {{ m.nickname }}
            <text v-if="m.role === 'OWNER'" class="badge">👑</text>
            <text v-else-if="m.role === 'ADMIN'" class="badge">★</text>
          </text>
        </view>
        <text
          v-if="canManage() && m.userId !== auth.user?.userId && m.role !== 'OWNER'"
          class="kick"
          @tap="onKick(m.userId)"
        >
          踢出
        </text>
      </view>
    </view>

    <button v-if="group.current.myRole === 'OWNER'" class="dissolve" @tap="onDissolve">
      解散群聊
    </button>

    <UserPicker
      v-model="showPicker"
      title="邀请成员"
      :excludeIds="group.members.map(m => m.userId)"
      @confirm="onInviteConfirm"
    />
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 24rpx;
}
.head {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  text-align: center;
  margin-bottom: 16rpx;
  .avatar {
    font-size: 80rpx;
  }
  .name {
    display: block;
    margin-top: 16rpx;
    font-size: 32rpx;
    font-weight: 600;
  }
}
.group {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  overflow: hidden;
  .row-title {
    display: flex;
    justify-content: space-between;
    padding: 24rpx;
    font-weight: 600;
    border-bottom: 1rpx solid #f0f0f0;
    .add {
      color: $uni-color-primary;
      font-weight: 400;
    }
  }
  .row {
    padding: 24rpx;
    display: flex;
    justify-content: space-between;
    border-bottom: 1rpx solid #f0f0f0;
    .lbl {
      color: #8f959e;
    }
  }
}
.member {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
  .avatar {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    background: #e8e8e8;
  }
  .info {
    flex: 1;
    margin-left: 20rpx;
  }
  .name {
    font-size: 28rpx;
    .badge {
      margin-left: 6rpx;
      color: $uni-color-primary;
    }
  }
  .kick {
    color: #f53f3f;
    font-size: 26rpx;
  }
}
.dissolve {
  margin-top: 40rpx;
  background: #fff;
  color: #f53f3f;
  border-radius: 16rpx;
  height: 88rpx;
  line-height: 88rpx;
}
</style>