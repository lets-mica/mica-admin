<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

interface IconMap {
  [key: string]: string
}
const iconMap: IconMap = {
  workbench: '🏠',
  contacts: '👥',
  file: '📁',
  monitor: '📊',
  dict: '📖',
  token: '🔑',
  log: '📝',
  notice: '📢',
  message: '💬'
}

function go(menu: any) {
  if (!menu.path) return
  // WebView 兑底:用 webview 页面打开后端路径
  uni.navigateTo({
    url: `/modules/menu/pages/webview?path=${encodeURIComponent(menu.path)}&title=${encodeURIComponent(menu.title || '')}`
  })
}

onMounted(() => {
  if (auth.menus.length === 0) auth.fetchProfile()
})
</script>

<template>
  <view class="page">
    <view v-if="auth.menus.length === 0" class="empty">
      <text>暂无应用</text>
    </view>
    <view v-for="cat in auth.menus" :key="cat.id" class="cat" v-show="!cat.hidden">
      <view class="cat-title">{{ cat.title }}</view>
      <view class="grid">
        <view
          v-for="m in (cat.children || []).filter(c => !c.hidden && c.type === 'MENU')"
          :key="m.id"
          class="item"
          @tap="go(m)"
        >
          <text class="ico">{{ iconMap[m.name] || '🧩' }}</text>
          <text class="t">{{ m.title }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 24rpx;
}
.cat {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  .cat-title {
    font-size: 28rpx;
    color: #1f2329;
    font-weight: 600;
    margin-bottom: 20rpx;
  }
  .grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 30rpx 0;
  }
  .item {
    text-align: center;
    .ico {
      display: block;
      font-size: 48rpx;
    }
    .t {
      display: block;
      font-size: 24rpx;
      margin-top: 8rpx;
      color: #1f2329;
    }
  }
}
.empty {
  text-align: center;
  color: #8f959e;
  padding: 100rpx 0;
}
</style>