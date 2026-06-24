<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { deleteToken, getTokens } from '@/api/monitor'
import type { TokenVo } from '@/api/monitor'
import { formatDateTime } from '@/utils/format'

const tokens = ref<TokenVo[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const p = await getTokens({ current: 1, size: 50 })
    tokens.value = p.records || []
  } finally {
    loading.value = false
  }
}

function onKick(t: TokenVo) {
  uni.showModal({
    title: '提示',
    content: `强制下线 ${t.nickName || t.userName}?`,
    success: async (res) => {
      if (res.confirm) {
        await deleteToken(t.key)
        tokens.value = tokens.value.filter((x) => x.key !== t.key)
      }
    }
  })
}

onMounted(load)
</script>

<template>
  <view class="page">
    <view v-if="tokens.length === 0" class="empty">
      <text>无活跃 Token</text>
    </view>
    <view v-for="t in tokens" :key="t.key" class="card">
      <view class="row">
        <text class="name">{{ t.nickName || t.userName }}</text>
        <text class="username">@{{ t.userName }}</text>
      </view>
      <view class="meta">
        <text>部门: {{ t.dept || '-' }}</text>
        <text>浏览器: {{ t.browser || '-' }}</text>
        <text>IP: {{ t.ip || '-' }} · {{ t.address || '-' }}</text>
        <text>登录: {{ formatDateTime(t.loginTime) }}</text>
      </view>
      <button size="mini" class="kick" @tap="onKick(t)">强制下线</button>
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
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  .row {
    display: flex;
    align-items: baseline;
    .name {
      font-size: 30rpx;
      font-weight: 600;
    }
    .username {
      color: #8f959e;
      font-size: 24rpx;
      margin-left: 12rpx;
    }
  }
  .meta {
    color: #555;
    font-size: 24rpx;
    margin: 12rpx 0;
    text {
      display: block;
      margin-top: 4rpx;
    }
  }
  .kick {
    background: #fff;
    color: #f53f3f;
    border: 1rpx solid #f53f3f;
  }
}
</style>
