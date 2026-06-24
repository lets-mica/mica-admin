<script setup lang="ts">
import { env } from '@/config/env'
</script>

<template>
  <view class="page">
    <view class="logo">
      <text class="emoji">💼</text>
      <text class="title">MICA Work</text>
      <text class="version">v1.0.0</text>
    </view>
    <view class="info">
      <text>基于 mica-admin + uniapp + Vue 3 + Vite 构建</text>
      <text>API: {{ env.apiUrl }}</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 80rpx 40rpx;
  text-align: center;
}
.logo {
  .emoji {
    font-size: 100rpx;
  }
  .title {
    display: block;
    font-size: 40rpx;
    font-weight: 600;
    color: $uni-color-primary;
    margin-top: 20rpx;
  }
  .version {
    display: block;
    color: #8f959e;
    font-size: 24rpx;
    margin-top: 6rpx;
  }
}
.info {
  margin-top: 60rpx;
  color: #555;
  font-size: 26rpx;
  text {
    display: block;
    margin-top: 16rpx;
  }
}
</style>