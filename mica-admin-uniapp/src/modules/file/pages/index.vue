<script setup lang="ts">
import { ref } from 'vue'
import { uploadFile, previewFile } from '@/api/file'

const fileList = ref<{ name: string; url: string; size: number }[]>([])
const uploading = ref(false)

function onChoose() {
  uni.chooseImage({
    count: 1,
    success: async (res) => {
      const path = res.tempFilePaths[0]
      uploading.value = true
      try {
        const f = await uploadFile(path)
        fileList.value.unshift(f)
        uni.showToast({ title: '上传成功', icon: 'success' })
      } finally {
        uploading.value = false
      }
    }
  })
}

function onPreview(f: { url: string; name: string }) {
  if (/\.(jpg|jpeg|png|gif|webp)$/i.test(f.name)) {
    uni.previewImage({ urls: [f.url] })
  } else {
    previewFile(f.url)
  }
}
</script>

<template>
  <view class="page">
    <button class="upload-btn" :loading="uploading" @tap="onChoose">选择文件上传</button>

    <view v-if="fileList.length === 0" class="empty">
      <text>暂无文件,点击上方按钮上传</text>
    </view>

    <view class="list">
      <view v-for="f in fileList" :key="f.url" class="row" @tap="onPreview(f)">
        <text class="name text-ellipsis">📄 {{ f.name }}</text>
        <text class="arrow">›</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 24rpx;
}
.upload-btn {
  background: $uni-color-primary;
  color: #fff;
  border-radius: 50rpx;
  height: 80rpx;
  line-height: 80rpx;
  margin-bottom: 24rpx;
}
.empty {
  text-align: center;
  color: #8f959e;
  padding: 100rpx 0;
}
.row {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  .arrow {
    color: #c0c4cc;
  }
}
</style>