<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getImOnlineStats, getServerMonitor } from '@/api/monitor'
import type { ServerMonitor } from '@/api/monitor'

const data = ref<ServerMonitor | null>(null)
const imOnline = ref(0)
const timer = ref<number | null>(null)

/**
 * 解析后端 FormatUtil 输出的字节字符串(已带单位,如 "12.34 GB"),直接原样展示
 */
function showSize(s: string | undefined): string {
  return s || '-'
}

/** 把 "12.34" 的使用率字符串转成数字 */
function pct(s: string | undefined): number {
  const n = Number(s)
  return Number.isFinite(n) ? n : 0
}

/** 把 CPU used 字符串(如 "5.20")作为百分比 */
function cpuPct(s: string | undefined): number {
  const n = Number(s)
  return Number.isFinite(n) ? n : 0
}

async function load() {
  const [m, o] = await Promise.all([
    getServerMonitor(),
    getImOnlineStats().catch(() => ({ totalOnline: 0 }))
  ])
  data.value = m
  imOnline.value = o.totalOnline ?? 0
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
        <text class="val">{{ data.cpu.used }}% ({{ data.cpu.coreNumber }} 核)</text>
        <view class="bar">
          <view
            class="fill"
            :style="{ width: cpuPct(data.cpu.used) + '%' }"
            :class="{ danger: cpuPct(data.cpu.used) > 80 }"
          />
        </view>
      </view>
      <view class="row">
        <text class="lbl">内存</text>
        <text class="val">{{ showSize(data.memory.used) }} / {{ showSize(data.memory.total) }}</text>
        <view class="bar">
          <view class="fill" :style="{ width: pct(data.memory.usageRate) + '%' }" />
        </view>
      </view>
      <view class="row">
        <text class="lbl">交换区</text>
        <text class="val">{{ showSize(data.swap.used) }} / {{ showSize(data.swap.total) }}</text>
        <view class="bar">
          <view class="fill" :style="{ width: pct(data.swap.usageRate) + '%' }" />
        </view>
      </view>
      <view class="row">
        <text class="lbl">磁盘</text>
        <text class="val">{{ showSize(data.disk.used) }} / {{ showSize(data.disk.total) }}</text>
        <view class="bar">
          <view class="fill" :style="{ width: pct(data.disk.usageRate) + '%' }" />
        </view>
      </view>
      <view class="row">
        <text class="lbl">运行时长</text>
        <text class="val">{{ data.sys.day }}</text>
      </view>
      <view class="row">
        <text class="lbl">系统</text>
        <text class="val sys-text">{{ data.sys.os }} · {{ data.sys.ip }}</text>
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
  .sys-text {
    word-break: break-all;
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
