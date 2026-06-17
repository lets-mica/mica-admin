<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';
import type { LogItem } from '#/api/monitor/log';
import { getErrorLogList, clearErrorLogs, getErrorDetail, exportErrorLogExcel } from '#/api/monitor/log';

import { h, onMounted, reactive, ref } from 'vue';
import { NButton, NCard, NDataTable, NInput, NModal, NPagination, NSpace } from 'naive-ui';
import { dialog, notification } from '#/adapter/naive';
import { formatDateTime } from '#/utils/format-date';

defineOptions({ name: 'ErrorLogMonitor' });

const loading = ref(false);
const data = ref<LogItem[]>([]);
const searchText = ref('');
const pagination = reactive({ page: 1, pageSize: 10, total: 0 });
const showDialog = ref(false);
const errorInfo = ref('');

// 渲染展开行内容
function renderExpand(row: LogItem) {
  return h('div', { class: 'p-3' }, [
    h('div', { class: 'mb-2' }, [
      h('span', { class: 'inline-block w-24 text-gray-500' }, '请求地址：'),
      h('span', null, (row as any).params || '-'),
    ]),
    (row as any).classMethod
      ? h('div', { class: 'mb-2' }, [
          h('span', { class: 'inline-block w-24 text-gray-500' }, '类和方法：'),
          h('span', null, (row as any).classMethod),
        ])
      : null,
    (row as any).data
      ? h('div', null, [
          h('span', { class: 'inline-block w-24 text-gray-500' }, '请求数据：'),
          h('span', null, (row as any).data),
        ])
      : null,
  ]);
}

const columns: DataTableColumns<LogItem> = [
  { type: 'expand', renderExpand: renderExpand },
  { title: '用户名', key: 'userName', width: 100 },
  { title: 'IP', key: 'requestIp', width: 130 },
  { title: 'IP来源', key: 'address', width: 180, ellipsis: { tooltip: true } },
  { title: '描述', key: 'description', ellipsis: { tooltip: true } },
  { title: '操作系统', key: 'os', width: 160 },
  { title: '浏览器', key: 'browser', width: 110 },
  {
      title: '创建时间',
      key: 'createdAt',
      width: 180,
      render: (row) => formatDateTime(row.createdAt),
    },
  {
    title: '异常详情',
    key: 'action',
    width: 110,
    align: 'center',
    render: (row) =>
      h(NButton, { size: 'small', type: 'primary', tertiary: true, onClick: () => viewDetail(row.id) }, () => '查看详情'),
  },
];

async function loadData() {
  loading.value = true;
  try {
    const params: any = { page: pagination.page, size: pagination.pageSize };
    if (searchText.value) params.blurry = searchText.value;
    const result = await getErrorLogList(params);
    data.value = result.list;
    pagination.total = result.total;
  } catch (e: any) {
    console.error('Failed to load error logs:', e);
    data.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
}

async function viewDetail(id: number) {
  showDialog.value = true;
  errorInfo.value = '加载中...';
  try {
    const result = await getErrorDetail(id);
    errorInfo.value = result.exceptionDetail || result.data?.exceptionDetail || '暂无详细信息';
  } catch (e: any) {
    console.error('Failed to get error detail:', e);
    const item = data.value.find((it) => it.id === id);
    errorInfo.value = (item as any)?.exceptionDetail || '获取详情失败';
  }
}

function closeDialog() {
  showDialog.value = false;
  errorInfo.value = '';
}

function handleClear() {
  dialog.warning({
    title: '提示',
    content: '确定要清空所有异常日志吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      loading.value = true;
      try {
        await clearErrorLogs();
        notification.success({ content: '已清空所有异常日志', duration: 2000 });
        pagination.page = 1;
        loadData();
      } catch (e: any) {
        console.error('Failed to clear error logs:', e);
      } finally {
        loading.value = false;
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
    await exportErrorLogExcel(params);
    notification.success({ content: '导出成功', duration: 2000 });
  } catch (e: any) {
    console.error('Failed to export error logs:', e);
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
        <NButton v-access:code="'system:logs:error:del'" type="error" :loading="loading" @click="handleClear">清空日志</NButton>
        <NButton v-access:code="'system:logs:error:export'" type="warning" :loading="exporting" @click="handleExport">导出</NButton>
      </div>

      <NDataTable
        :columns="columns"
        :data="data"
        :loading="loading"
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

    <NModal
      v-model:show="showDialog"
      preset="card"
      title="异常详情"
      style="width: 85%; max-width: 1200px;"
      :mask-closable="false"
    >
      <div class="error-detail max-h-[70vh] overflow-auto rounded-lg p-4">
        <pre class="whitespace-pre-wrap font-mono text-sm leading-relaxed"><code>{{ errorInfo }}</code></pre>
      </div>
      <template #footer>
        <NSpace justify="end">
          <NButton type="primary" @click="closeDialog">关闭</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.error-detail {
  background-color: var(--n-color-modal, #f5f5f5);
}

:deep(pre code) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}
</style>
