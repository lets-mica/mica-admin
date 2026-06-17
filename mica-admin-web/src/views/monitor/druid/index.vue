<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';
import type { SqlRecord } from '#/api/monitor/druid';
import { getSqlList } from '#/api/monitor/druid';

import { NCard, NDataTable, NTag } from 'naive-ui';
import { h, onMounted, ref } from 'vue';
import { notification } from '#/adapter/naive';

defineOptions({ name: 'DruidSqlMonitor' });

const loading = ref(true);
const show = ref(false);
const data = ref<SqlRecord[]>([]);

const columns: DataTableColumns<SqlRecord> = [
  {
    type: 'expand',
    renderExpand: (row) =>
      h(
        'div',
        { class: 'p-3 text-xs font-mono whitespace-pre-wrap' },
        row.SQL,
      ),
  },
  { title: 'ID', key: 'ID', width: 60, sorter: 'default' },
  { title: 'SQL', key: 'SQL', ellipsis: { tooltip: true } },
  { title: '执行数', key: 'ExecuteCount', width: 80, sorter: 'default', align: 'center' },
  { title: '耗时(ms)', key: 'TotalTime', width: 90, sorter: 'default', align: 'center' },
  { title: '最慢', key: 'MaxTimespan', width: 80, sorter: 'default', align: 'center' },
  { title: '事务', key: 'InTransactionCount', width: 80, sorter: 'default', align: 'center' },
  {
    title: '错误',
    key: 'ErrorCount',
    width: 80,
    sorter: 'default',
    align: 'center',
    render: (row) =>
      row.ErrorCount > 0
        ? h(NTag, { type: 'error', size: 'small', bordered: false }, () => row.ErrorCount)
        : (row.ErrorCount as any),
  },
  { title: '更新行', key: 'EffectedRowCount', width: 80, sorter: 'default', align: 'center' },
  { title: '读取行', key: 'FetchRowCount', width: 80, sorter: 'default', align: 'center' },
  { title: '执行中', key: 'RunningCount', width: 80, sorter: 'default', align: 'center' },
  { title: '最大并发', key: 'ConcurrentMax', width: 90, sorter: 'default', align: 'center' },
];

async function loadData() {
  loading.value = true;
  try {
    const result = await getSqlList();
    data.value = result ?? [];
    show.value = true;
  } catch (e: any) {
    console.error('Failed to load SQL list:', e);
    data.value = [];
    show.value = true;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="p-4">
    <NCard :bordered="false" content-class="!p-4">
      <NDataTable
        :loading="loading"
        :columns="columns"
        :data="data"
        :row-key="(row: SqlRecord) => row.ID"
        :scroll-x="1300"
        striped
      />
    </NCard>
  </div>
</template>