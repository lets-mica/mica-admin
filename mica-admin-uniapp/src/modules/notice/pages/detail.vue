<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getNoticeDetail } from '@/api/notice'
import type { NoticeVo } from '@/api/notice'
import { formatDateTime } from '@/utils/format'

const notice = ref<NoticeVo | null>(null)

onLoad(async (q) => {
  if (q?.id) notice.value = await getNoticeDetail(Number(q.id))
})
</script>

<template>
  <view class="page" v-if="notice">
    <view class="title">{{ notice.title }}</view>
    <view class="meta">
      <text v-if="notice.publisher">{{ notice.publisher }}</text>
      <text class="time">{{ formatDateTime(notice.publishTime || notice.createdAt) }}</text>
    </view>
    <view class="content">{{ notice.content }}</view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 30rpx;
  background: #fff;
  min-height: 100vh;
}
.title {
  font-size: 40rpx;
  font-weight: 600;
  line-height: 1.5;
}
.meta {
  display: flex;
  justify-content: space-between;
  color: #8f959e;
  font-size: 24rpx;
  margin: 20rpx 0 40rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}
.content {
  font-size: 30rpx;
  line-height: 1.8;
  color: #1f2329;
  white-space: pre-wrap;
}
</style>