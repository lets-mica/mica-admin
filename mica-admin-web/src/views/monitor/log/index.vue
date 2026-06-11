<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';
import type { LogItem } from '#/api/monitor/log';
import { getOperationLogList, clearOperationLogs, exportInfoLogExcel } from '#/api/monitor/log';

import { NButton, NCard, NDataTable, NInput, NPagination, NTag } from 'naive-ui';
import { h, onMounted, reactive, ref } from 'vue';
import { dialog, notification } from '#/adapter/naive';
import { formatDateTime } from '#/utils/format-date';

defineOptions({ name: 'OperationLog' });

const loading = ref(false);
const data = ref<LogItem[]>([]);
const searchText = ref('');
const pagination = reactive({ page: 1, pageSize: 10, total: 0 });

const columns: DataTableColumns<LogItem> = [
  { title: '用户名', key: 'userName', width: 100 },
  { title: 'IP', key: 'requestIp', width: 130 },
  { title: 'IP来源', key: 'address', width: 180, ellipsis: { tooltip: true } },
  { title: '描述', key: 'description', ellipsis: { tooltip: true } },
  { title: '浏览器', key: 'browser', width: 110 },
  {
    title: '请求耗时',
    key: 'requestTime',
    width: 110,
    align: 'center',
    render: (row) => {
      const t = row.requestTime || 0;
      const type = t <= 300 ? 'success' : t <= 1000 ? 'warning' : 'error';
      return h(NTag, { type, size: 'small', bordered: false }, () => `${t}ms`);
    },
  },
  {
      title: '创建时间',
      key: 'createdAt',
      width: 180,
      render: (row) => formatDateTime(row.createdAt),
    },
];

async function loadData() {
  loading.value = true;
  try {
    const params: any = { page: pagination.page, size: pagination.pageSize };
    if (searchText.value) params.blurry = searchText.value;
    const result = await getOperationLogList(params);
    data.value = result.list;
    pagination.total = result.total;
  } catch (e: any) {
    console.error('Failed to load operation logs:', e);
    data.value = [];
    pagination.total = 0;
    notification.error({
      content: '加载操作日志失败',
      description: e?.response?.data?.msg || e?.message || '请检查后端服务',
      duration: 4000,
    });
  } finally {
    loading.value = false;
  }
}

function handleClear() {
  dialog.warning({
    title: '提示',
    content: '确定要清空所有操作日志吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await clearOperationLogs();
        notification.success({ content: '已清空所有操作日志', duration: 2000 });
        loadData();
      } catch (e: any) {
        console.error('Failed to clear logs:', e);
        notification.error({ content: '操作失败', description: e.message || '无法清空日志', duration: 3000 });
      }
    },
  });
}

function handleSearch() {
  pagination.page = 1;
  loadData();
}

function handlePageChange(page: number) {
  pagination.page = page;
  loadData();
}

function handlePageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize;
  pagination.page = 1;
  loadData();
}

const exporting = ref(false);

async function handleExport() {
  exporting.value = true;
  try {
    const params: Record<string, unknown> = {};
    if (searchText.value) params.blurry = searchText.value;
    await exportInfoLogExcel(params);
    notification.success({ content: '导出成功', duration: 2000 });
  } catch (e: any) {
    notification.error({
      content: '导出失败',
      description: e?.response?.data?.msg || e?.message || '导出失败',
      duration: 3000,
    });
  } finally {
    exporting.value = false;
  }
}

onMounted(() => loadData());
</script>

<template>
  <div class="p-4">
    <NCard :bordered="false" content-class="!p-4">
      <div class="mb-3 flex items-center gap-2">
        <NInput
          v-model:value="searchText"
          placeholder="请输入你要搜索的内容"
          clearable
          class="!w-64"
          @keyup.enter="handleSearch"
        />
        <NButton type="primary" @click="handleSearch">搜索</NButton>
        <NButton v-access:code="'system:logs:info:del'" type="error" @click="handleClear">清空日志</NButton>
        <NButton v-access:code="'system:logs:info:export'" type="warning" :loading="exporting" @click="handleExport">导出</NButton>
      </div>

      <NDataTable
        :loading="loading"
        :columns="columns"
        :data="data"
        :row-key="(row: LogItem) => row.id"
        striped
      />

      <div class="mt-4 flex justify-end">
        <NPagination
          v-model:page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :item-count="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          show-size-picker
          show-quick-jumper
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </NCard>
  </div>
</template>
