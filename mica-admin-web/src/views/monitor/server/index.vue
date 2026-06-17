<script setup lang="ts">
import type { ServerInfo } from '#/api/monitor/server';
import { getServerInfo } from '#/api/monitor/server';

import { NCard, NSpace, NProgress } from 'naive-ui';
import { onMounted, onUnmounted, ref } from 'vue';
import { notification } from '#/adapter/naive';

const loading = ref(true);
const show = ref(false);
const data = ref<ServerInfo>({
  time: '',
  sys: { os: '', ip: '', day: '' },
  cpu: { name: '', package: '', core: '', logic: '', used: '0', coreNumber: 0 },
  memory: { total: '', used: '', available: '', usageRate: '0' },
  swap: { total: '', used: '', available: '', usageRate: '0' },
  disk: { total: '', used: '', available: '', usageRate: '0' },
});

let monitorInterval: number | null = null;

const loadError = ref('');

async function loadData() {
  try {
    const result = await getServerInfo();
    data.value = result;
    show.value = true;
    loadError.value = '';
  } catch (e: any) {
    console.error('Failed to load server info:', e);
    show.value = false;
    loadError.value = e?.response?.data?.msg || e?.message || '服务器监控接口不可用';
  } finally {
    loading.value = false;
  }
}

function refreshData() {
  loadData();
}

onMounted(() => {
  loadData();
  monitorInterval = window.setInterval(() => {
    loadData();
  }, 3500);
});

onUnmounted(() => {
  if (monitorInterval) {
    clearInterval(monitorInterval);
  }
});
</script>

<template>
  <div class="p-4">
    <NCard v-if="loading" title="服务器监控">
      <div class="flex items-center justify-center h-64">
        <span class="text-muted-foreground">加载中...</span>
      </div>
    </NCard>

    <NCard v-else-if="loadError" title="服务器监控">
      <div class="flex items-center justify-center h-64 text-muted-foreground">
        {{ loadError }}
      </div>
    </NCard>

    <template v-else-if="show">
      <!-- 系统信息 -->
      <NCard class="mb-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4 text-sm text-muted-foreground">
            <span>系统：{{ data.sys.os }}</span>
            <span>IP：{{ data.sys.ip }}</span>
            <span>运行时间：{{ data.sys.day }}</span>
          </div>
          <button
            class="p-2 rounded hover:bg-muted transition-colors"
            @click="refreshData"
            title="刷新"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10"></polyline>
              <polyline points="1 20 1 14 7 14"></polyline>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
            </svg>
          </button>
        </div>
      </NCard>

      <!-- 状态概览 -->
      <NCard title="状态概览" class="mb-4">
        <NSpace wrap class="w-full">
          <!-- CPU -->
          <div class="flex-1 min-w-[200px]">
            <div class="text-center text-sm text-muted-foreground mb-2">CPU使用率</div>
            <div class="flex justify-center">
              <NProgress
                type="circle"
                :percentage="parseFloat(data.cpu.used)"
                :stroke-width="8"
                :show-indicator="true"
                :size="100"
              />
            </div>
            <div class="text-center text-xs text-muted-foreground mt-2">
              {{ data.cpu.core }}
            </div>
          </div>

          <!-- 内存 -->
          <div class="flex-1 min-w-[200px]">
            <div class="text-center text-sm text-muted-foreground mb-2">内存使用率</div>
            <div class="flex justify-center">
              <NProgress
                type="circle"
                :percentage="parseFloat(data.memory.usageRate)"
                :stroke-width="8"
                :show-indicator="true"
                :size="100"
              />
            </div>
            <div class="text-center text-xs text-muted-foreground mt-2">
              {{ data.memory.used }} / {{ data.memory.total }}
            </div>
          </div>

          <!-- 交换区 -->
          <div class="flex-1 min-w-[200px]">
            <div class="text-center text-sm text-muted-foreground mb-2">交换区使用率</div>
            <div class="flex justify-center">
              <NProgress
                type="circle"
                :percentage="parseFloat(data.swap.usageRate)"
                :stroke-width="8"
                :show-indicator="true"
                :size="100"
              />
            </div>
            <div class="text-center text-xs text-muted-foreground mt-2">
              {{ data.swap.used }} / {{ data.swap.total }}
            </div>
          </div>

          <!-- 磁盘 -->
          <div class="flex-1 min-w-[200px]">
            <div class="text-center text-sm text-muted-foreground mb-2">磁盘使用率</div>
            <div class="flex justify-center">
              <NProgress
                type="circle"
                :percentage="parseFloat(data.disk.usageRate)"
                :stroke-width="8"
                :show-indicator="true"
                :size="100"
              />
            </div>
            <div class="text-center text-xs text-muted-foreground mt-2">
              {{ data.disk.used }} / {{ data.disk.total }}
            </div>
          </div>
        </NSpace>
      </NCard>

      <!-- CPU信息 -->
      <NCard title="CPU信息" class="mb-4">
        <div class="grid grid-cols-2 gap-4">
          <div class="flex justify-between">
            <span class="text-muted-foreground">CPU名称</span>
            <span>{{ data.cpu.name }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-muted-foreground">封装</span>
            <span>{{ data.cpu.package }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-muted-foreground">核心数</span>
            <span>{{ data.cpu.core }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-muted-foreground">线程数</span>
            <span>{{ data.cpu.logic }}</span>
          </div>
        </div>
      </NCard>

      <!-- 内存信息 -->
      <NCard title="内存信息">
        <div class="grid grid-cols-3 gap-4">
          <div class="flex justify-between">
            <span class="text-muted-foreground">总量</span>
            <span>{{ data.memory.total }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-muted-foreground">已使用</span>
            <span>{{ data.memory.used }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-muted-foreground">可用</span>
            <span>{{ data.memory.available }}</span>
          </div>
        </div>
      </NCard>
    </template>
  </div>
</template>