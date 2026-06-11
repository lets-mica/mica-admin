<script setup lang="ts">
import { NCard, NSelect, NInput, NInputNumber, NSpace, NGrid, NGi } from 'naive-ui';
import { onMounted, onUnmounted, ref, watch } from 'vue';
import * as echarts from 'echarts';

defineOptions({ name: 'EChartsDemo' });

const chartRef = ref<HTMLDivElement | null>(null);
let chart: echarts.ECharts | null = null;

const chartType = ref('line');
const chartTitle = ref('销售数据');

const chartTypeOptions = [
  { label: '折线图', value: 'line' },
  { label: '柱状图', value: 'bar' },
  { label: '饼图', value: 'pie' },
  { label: '散点图', value: 'scatter' },
  { label: '雷达图', value: 'radar' },
  { label: '仪表盘', value: 'gauge' },
];

// 模拟数据
const lineData = {
  xAxis: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
  series1: [820, 932, 901, 934, 1290, 1330, 1320, 1800, 1900, 2000, 2100, 2300],
  series2: [620, 732, 701, 834, 1090, 1130, 1120, 1500, 1600, 1700, 1800, 1900],
};

const barData = {
  xAxis: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
  series: [120, 200, 150, 80, 70, 110, 130],
};

const pieData = [
  { value: 1048, name: '搜索引擎' },
  { value: 735, name: '直接访问' },
  { value: 580, name: '邮件营销' },
  { value: 484, name: '联盟广告' },
  { value: 300, name: '视频广告' },
];

const scatterData = Array.from({ length: 50 }, () => [
  Math.random() * 100,
  Math.random() * 100,
  Math.random() * 40 + 10,
]);

function getChartOption(type: string) {
  switch (type) {
    case 'line':
      return {
        title: { text: chartTitle.value, left: 'center' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['今年', '去年'], top: 30 },
        xAxis: { type: 'category', data: lineData.xAxis },
        yAxis: { type: 'value' },
        series: [
          { name: '今年', data: lineData.series1, type: 'line', smooth: true, areaStyle: { opacity: 0.3 } },
          { name: '去年', data: lineData.series2, type: 'line', smooth: true, areaStyle: { opacity: 0.3 } },
        ],
      };
    case 'bar':
      return {
        title: { text: '周访问量', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: barData.xAxis },
        yAxis: { type: 'value' },
        series: [{ data: barData.series, type: 'bar', itemStyle: { color: '#3b82f6' } }],
      };
    case 'pie':
      return {
        title: { text: '访问来源', left: 'center' },
        tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
        series: [
          {
            name: '访问来源',
            type: 'pie',
            radius: '50%',
            data: pieData,
            emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } },
          },
        ],
      };
    case 'scatter':
      return {
        title: { text: '散点图', left: 'center' },
        tooltip: { trigger: 'item' },
        xAxis: { type: 'value', name: 'X' },
        yAxis: { type: 'value', name: 'Y' },
        series: [
          {
            type: 'scatter',
            data: scatterData,
            symbolSize: (data: number[]) => data[2],
            itemStyle: { color: '#10b981' },
          },
        ],
      };
    case 'radar':
      return {
        title: { text: '能力评估', left: 'center' },
        tooltip: {},
        radar: {
          indicator: [
            { name: '销售', max: 100 },
            { name: '管理', max: 100 },
            { name: '技术', max: 100 },
            { name: '客服', max: 100 },
            { name: '研发', max: 100 },
            { name: '市场', max: 100 },
          ],
        },
        series: [
          {
            type: 'radar',
            data: [
              { value: [85, 70, 90, 80, 75, 88], name: '能力分布' },
            ],
            areaStyle: { opacity: 0.3 },
          },
        ],
      };
    case 'gauge':
      return {
        title: { text: '完成率', left: 'center' },
        series: [
          {
            type: 'gauge',
            progress: { show: true, width: 18 },
            axisLine: { lineStyle: { width: 18 } },
            pointer: { width: 5 },
            axisTick: { show: true },
            splitLine: { length: 12, lineStyle: { width: 2, color: '#999' } },
            axisLabel: { distance: 25, color: '#999', fontSize: 12 },
            anchor: { show: true, showAbove: true, size: 18, itemStyle: { borderWidth: 4, borderColor: '#3b82f6', color: '#fff' } },
            title: { show: true, offsetCenter: [0, '70%'] },
            detail: { valueAnimation: true, fontSize: 30, offsetCenter: [0, '90%'] },
            data: [{ value: 78, name: '完成率(%)' }],
          },
        ],
      };
    default:
      return {};
  }
}

function initChart() {
  if (!chartRef.value) return;
  if (!chart) {
    chart = echarts.init(chartRef.value);
  }
  chart.setOption(getChartOption(chartType.value), true);
}

function resize() {
  chart?.resize();
}

watch(chartType, () => {
  initChart();
});

watch(chartTitle, () => {
  if (chart && chartType.value === 'line') {
    chart.setOption({ title: { text: chartTitle.value } });
  }
});

onMounted(() => {
  initChart();
  window.addEventListener('resize', resize);
});

onUnmounted(() => {
  window.removeEventListener('resize', resize);
  chart?.dispose();
});
</script>

<template>
  <div class="p-4">
    <NCard title="ECharts 图表组件演示">
      <template #header-extra>
        <NSpace>
          <NSelect v-model:value="chartType" :options="chartTypeOptions" placeholder="选择图表类型" style="width: 150px" size="small" />
          <NInput v-model:value="chartTitle" placeholder="图表标题" size="small" style="width: 200px" v-if="chartType === 'line'" />
        </NSpace>
      </template>

      <div ref="chartRef" style="width: 100%; height: 500px;"></div>
    </NCard>
  </div>
</template>

<style scoped>
:deep(.n-card) {
  border-radius: 8px;
}
</style>
