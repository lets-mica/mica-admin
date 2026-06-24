<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getImOnlineStats, getServerMonitor } from '@/api/monitor'
import type { ServerMonitor } from '@/api/monitor'

const data = ref<ServerMonitor | null>(null)
const imOnline = ref(0)
const timer = ref<number | null>(null)

function fmtBytes(n: number) {
  if (n < 1024) return `${n}B`
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)}KB`
  if (n < 1024 * 1024 * 1024) return `${(n / 1024 / 1024).toFixed(1)}MB`
  return `${(n / 1024 / 1024 / 1024).toFixed(1)}GB`
}

function fmtDuration(ms: number) {
  const s = Math.floor(ms / 1000)
  const d = Math.floor(s / 86400)
  const h = Math.floor((s % 86400) / 3600)
  const m = Math.floor((s % 3600) / 60)
  return `${d}天 ${h}时 ${m}分`
}

async function load() {
  const [m, o] = await Promise.all([
    getServerMonitor(),
    getImOnlineStats().catch(() => ({ onlineCount: 0 }))
  ])
  data.value = m
  imOnline.value = o.onlineCount
}

onMounted(() => {
  load()
  timer.value = setInterval(load, 30000) as unknown as number
})
onUnmounted(() => {
  if (timer.value) clearInterval(timer.value)
})
</script>

<template>
  <view class="page">
    <view class="card" v-if="data">
      <view class="row">
        <text class="lbl">CPU</text>
        <text class="val">{{ data.cpu.usage }}% ({{ data.cpu.cores }} 核)</text>
        <view class="bar">
          <view class="fill" :style="{ width: data.cpu.usage + '%' }" :class="{ danger: data.cpu.usage > 80 }" />
        </view>
      </view>
      <view class="row">
        <text class="lbl">内存</text>
        <text class="val">{{ fmtBytes(data.memory.used) }} / {{ fmtBytes(data.memory.total) }}</text>
        <view class="bar">
          <view class="fill" :style="{ width: (data.memory.used / data.memory.total * 100) + '%' }" />
        </view>
      </view>
      <view class="row">
        <text class="lbl">JVM</text>
        <text class="val">{{ fmtBytes(data.jvm.heapUsed) }} / {{ fmtBytes(data.jvm.heapMax) }}</text>
        <view class="bar">
          <view class="fill" :style="{ width: (data.jvm.heapUsed / data.jvm.heapMax * 100) + '%' }" />
        </view>
      </view>
      <view class="row">
        <text class="lbl">磁盘</text>
        <text class="val">{{ data.disk.used }}GB / {{ data.disk.total }}GB</text>
        <view class="bar">
          <view class="fill" :style="{ width: (data.disk.used / data.disk.total * 100) + '%' }" />
        </view>
      </view>
      <view class="row">
        <text class="lbl">运行时长</text>
        <text class="val">{{ fmtDuration(data.jvm.uptime) }}</text>
      </view>
      <view class="row">
        <text class="lbl">系统</text>
        <text class="val">{{ data.system.os }} · {{ data.system.hostname }}</text>
      </view>
    </view>

    <view class="card">
      <view class="card-title">IM 在线用户</view>
      <text class="big">{{ imOnline }}</text>
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
  padding: 24rpx;
  margin-bottom: 16rpx;
  .card-title {
    font-size: 28rpx;
    font-weight: 600;
    margin-bottom: 20rpx;
  }
  .row {
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
    &:last-child {
      border-bottom: none;
    }
  }
  .lbl {
    color: #8f959e;
    font-size: 24rpx;
  }
  .val {
    color: #1f2329;
    font-size: 28rpx;
    margin-left: 20rpx;
  }
  .bar {
    margin-top: 10rpx;
    height: 8rpx;
    background: #f0f0f0;
    border-radius: 4rpx;
    overflow: hidden;
  }
  .fill {
    height: 100%;
    background: $uni-color-primary;
    transition: width 0.3s;
    &.danger {
      background: #f53f3f;
    }
  }
  .big {
    font-size: 80rpx;
    font-weight: 600;
    color: $uni-color-primary;
  }
}
</style>