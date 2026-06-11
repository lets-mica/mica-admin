<script setup lang="ts">
import type { RedisData } from '#/api/monitor/redis';
import { getRedisInfo } from '#/api/monitor/redis';

import { NCard, NGrid, NGi, NStatistic, NSpace, NProgress } from 'naive-ui';
import { onMounted, onUnmounted, ref } from 'vue';
import { notification } from '#/adapter/naive';
import * as echarts from 'echarts';

defineOptions({ name: 'RedisMonitor' });

const loading = ref(true);
const show = ref(false);
const data = ref<RedisData | null>(null);

let commandChart: echarts.ECharts | null = null;
let memoryChart: echarts.ECharts | null = null;
let monitorInterval: number | null = null;

const commandChartRef = ref<HTMLDivElement | null>(null);
const memoryChartRef = ref<HTMLDivElement | null>(null);

const loadError = ref('');

async function loadData() {
  try {
    const result = await getRedisInfo();
    data.value = result;
    show.value = true;
    loadError.value = '';
    loading.value = false;
    setTimeout(() => initCharts(), 100);
  } catch (e: any) {
    console.error('Failed to load Redis info:', e);
    data.value = null;
    show.value = false;
    loadError.value = e?.response?.data?.msg || e?.message || 'Redis 监控接口不可用';
    loading.value = false;
    notification.error({
      content: '加载 Redis 监控失败',
      description: loadError.value,
      duration: 4000,
    });
  }
}

function initCharts() {
  if (!data.value || !show.value) return;

  // 命令统计图表
  if (commandChartRef.value) {
    commandChart = echarts.init(commandChartRef.value);
    commandChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b} : {c} ({d}%)',
      },
      series: [
        {
          name: '命令',
          type: 'pie',
          roseType: 'radius',
          radius: [15, 95],
          center: ['50%', '38%'],
          data: data.value.commandStats || [],
          animationEasing: 'cubicInOut',
          animationDuration: 1000,
        },
      ],
    });
  }

  // 内存仪表盘图表
  if (memoryChartRef.value) {
    const memoryValue = parseFloat(data.value.info?.used_memory_human || '0');
    memoryChart = echarts.init(memoryChartRef.value);
    memoryChart.setOption({
      tooltip: {
        formatter: '{b} <br/>{a} : ' + (data.value?.info?.used_memory_human || ''),
      },
      series: [
        {
          name: '峰值',
          type: 'gauge',
          min: 0,
          max: 1000,
          detail: {
            formatter: data.value.info?.used_memory_human || '0',
          },
          data: [{ value: memoryValue, name: '内存消耗' }],
        },
      ],
    });
  }
}

function refreshData() {
  loadData();
}

onMounted(() => {
  loadData();
  // 每3.5秒刷新一次
  monitorInterval = window.setInterval(() => {
    loadData();
  }, 3500);

  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    commandChart?.resize();
    memoryChart?.resize();
  });
});

onUnmounted(() => {
  if (monitorInterval) {
    clearInterval(monitorInterval);
  }
  commandChart?.dispose();
  memoryChart?.dispose();
});
</script>

<template>
  <div v-if="loading" class="p-4">
    <NCard title="Redis监控">
      <div class="flex items-center justify-center h-64">
        <span class="text-muted-foreground">加载中...</span>
      </div>
    </NCard>
  </div>

  <div v-else-if="loadError" class="p-4">
    <NCard title="Redis监控">
      <div class="flex items-center justify-center h-64 text-muted-foreground">
        {{ loadError }}
      </div>
    </NCard>
  </div>

  <div v-else-if="show && data" class="p-4">
    <!-- 基本信息 -->
    <NCard title="基本信息" class="mb-4">
      <div class="grid grid-cols-4 gap-4">
        <div class="text-center p-2">
          <div class="text-sm text-gray-500">Redis版本</div>
          <div class="text-lg font-semibold">{{ data.info?.redis_version || '-' }}</div>
        </div>
        <div class="text-center p-2">
          <div class="text-sm text-gray-500">运行模式</div>
          <div class="text-lg font-semibold">{{ data.info?.redis_mode === 'standalone' ? '单机' : '集群' }}</div>
        </div>
        <div class="text-center p-2">
          <div class="text-sm text-gray-500">端口</div>
          <div class="text-lg font-semibold">{{ data.info?.tcp_port || '-' }}</div>
        </div>
        <div class="text-center p-2">
          <div class="text-sm text-gray-500">客户端数</div>
          <div class="text-lg font-semibold">{{ data.info?.connected_clients || '-' }}</div>
        </div>
        <div class="text-center p-2">
          <div class="text-sm text-gray-500">运行时间(天)</div>
          <div class="text-lg font-semibold">{{ data.info?.uptime_in_days || '-' }}</div>
        </div>
        <div class="text-center p-2">
          <div class="text-sm text-gray-500">使用内存</div>
          <div class="text-lg font-semibold">{{ data.info?.used_memory_human || '-' }}</div>
        </div>
        <div class="text-center p-2">
          <div class="text-sm text-gray-500">使用CPU</div>
          <div class="text-lg font-semibold">{{ data.info?.used_cpu_user_children || '-' }}</div>
        </div>
        <div class="text-center p-2">
          <div class="text-sm text-gray-500">Key数量</div>
          <div class="text-lg font-semibold">{{ data.dbSize || '-' }}</div>
        </div>
      </div>
    </NCard>

    <!-- 图表区域 -->
    <NGrid :cols="2" :x-gap="16">
      <NGi>
        <NCard title="命令统计">
          <div ref="commandChartRef" style="height: 420px"></div>
        </NCard>
      </NGi>
      <NGi>
        <NCard title="内存信息">
          <div ref="memoryChartRef" style="height: 420px"></div>
        </NCard>
      </NGi>
    </NGrid>
  </div>
</template>

<style scoped>
:deep(.n-card) {
  border-radius: 8px;
}
</style>