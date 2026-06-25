<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getRegisteredCards } from '@/modules/extension/workbench/registry'
import type { WorkbenchCard } from '@/modules/extension/workbench/registry'

const auth = useAuthStore()

const greeting = ref('')

function updateGreeting() {
  const h = new Date().getHours()
  if (h < 6) greeting.value = '夜深了'
  else if (h < 12) greeting.value = '早上好'
  else if (h < 14) greeting.value = '中午好'
  else if (h < 18) greeting.value = '下午好'
  else greeting.value = '晚上好'
}

const cards = computed<WorkbenchCard[]>(() => getRegisteredCards())
const hasCards = computed(() => cards.value.length > 0)

onMounted(() => {
  if (!auth.isLoggedIn) {
    uni.reLaunch({ url: '/modules/auth/pages/login' })
    return
  }
  updateGreeting()
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

    <view class="cards">
      <component
        :is="card.component"
        v-for="card in cards"
        :key="card.id"
      />
    </view>

    <view v-if="!hasCards" class="empty">
      <text class="empty-title">暂无业务卡片</text>
      <text class="empty-desc">前往 src/modules/extension/workbench/index.ts 注册卡片</text>
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
.cards {
  display: block;
}
.empty {
  margin-top: 80rpx;
  text-align: center;
  .empty-title {
    display: block;
    color: #8f959e;
    font-size: 30rpx;
  }
  .empty-desc {
    display: block;
    color: #c0c4cc;
    font-size: 24rpx;
    margin-top: 12rpx;
  }
}
</style>
