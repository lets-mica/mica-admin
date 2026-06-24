<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { env } from '@/config/env'

const url = ref('')
const title = ref('')

onLoad((q) => {
  if (q?.path) {
    // WebView 兑底:把后端路径拼接为完整 URL
    const backend = (env.apiUrl || '').replace(/\/api$/, '')
    url.value = `${backend}${q.path}`
  }
  title.value = q?.title || ''
  uni.setNavigationBarTitle({ title: title.value })
})
</script>

<template>
  <web-view :src="url" />
</template>