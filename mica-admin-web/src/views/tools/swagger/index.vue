<script setup lang="ts">
import { NCard, NInput, NSpace, NButton, NIcon } from 'naive-ui';
import { computed, ref, onMounted } from 'vue';
import { ExternalLink as LinkIcon, RotateCw as ReloadIcon } from '@vben/icons';

defineOptions({ name: 'SwaggerIndex' });

const swaggerUrl = ref('http://localhost:8080/doc.html');
const loading = ref(false);

const iframeUrl = computed(() => swaggerUrl.value);

function reload() {
  const iframe = document.getElementById('swagger-iframe') as HTMLIFrameElement;
  if (iframe) {
    loading.value = true;
    iframe.src = iframeUrl.value;
    setTimeout(() => {
      loading.value = false;
    }, 1000);
  }
}

function openInNew() {
  window.open(swaggerUrl.value, '_blank');
}

onMounted(() => {
  loading.value = true;
  setTimeout(() => {
    loading.value = false;
  }, 1000);
});
</script>

<template>
  <div class="p-4">
    <NCard title="接口文档">
      <template #header-extra>
        <NSpace>
          <NInput
            v-model:value="swaggerUrl"
            placeholder="请输入 Swagger 地址"
            style="width: 400px"
            size="small"
          />
          <NButton size="small" @click="reload">
            <template #icon><NIcon><ReloadIcon /></NIcon></template>
            刷新
          </NButton>
          <NButton size="small" type="primary" @click="openInNew">
            <template #icon><NIcon><LinkIcon /></NIcon></template>
            新窗口打开
          </NButton>
        </NSpace>
      </template>

      <div v-if="loading" class="flex items-center justify-center h-[600px]">
        <span class="text-gray-500">加载中...</span>
      </div>
      <iframe
        v-else
        id="swagger-iframe"
        :src="iframeUrl"
        frameborder="0"
        width="100%"
        height="600"
        style="border-radius: 6px;"
      />
    </NCard>
  </div>
</template>

<style scoped>
:deep(.n-card) {
  border-radius: 8px;
}
</style>
