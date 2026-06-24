<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getNotices } from '@/api/notice'
import type { NoticeVo } from '@/api/notice'
import { formatDateTime } from '@/utils/format'

const notices = ref<NoticeVo[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const p = await getNotices({ current: 1, size: 50 })
    notices.value = p.records || []
  } finally {
    loading.value = false
  }
}

function open(n: NoticeVo) {
  uni.navigateTo({ url: `/modules/notice/pages/detail?id=${n.id}` })
}

onMounted(() => {
  load()
})
</script>

<template>
  <view class="page">
    <view v-if="notices.length === 0 && !loading" class="empty">
      <text>暂无公告</text>
    </view>
    <view v-for="n in notices" :key="n.id" class="row" @tap="open(n)">
      <view class="row-head">
        <text class="title text-ellipsis">{{ n.title }}</text>
        <text class="time">{{ formatDateTime(n.publishTime || n.createdAt, 'YYYY-MM-DD') }}</text>
      </view>
      <text class="content text-ellipsis">{{ n.content }}</text>
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
.row {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  .row-head {
    display: flex;
    justify-content: space-between;
    .title {
      flex: 1;
      font-size: 30rpx;
      font-weight: 500;
    }
    .time {
      color: #8f959e;
      font-size: 22rpx;
      margin-left: 20rpx;
    }
  }
  .content {
    display: block;
    color: #555;
    font-size: 26rpx;
    margin-top: 8rpx;
  }
}
</style>