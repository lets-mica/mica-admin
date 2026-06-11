<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';
import type { TokenItem } from '#/api/monitor/token';
import { getTokenList, delToken } from '#/api/monitor/token';

import { NButton, NCard, NDataTable, NInput, NPagination, NSpace } from 'naive-ui';
import { h, onMounted, reactive, ref } from 'vue';
import { dialog, notification } from '#/adapter/naive';
import { formatDateTime } from '#/utils/format-date';

defineOptions({ name: 'TokenMonitor' });

const loading = ref(false);
const data = ref<TokenItem[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, total: 0 });
const searchText = ref('');
const selectedRowKeys = ref<string[]>([]);

const columns: DataTableColumns<TokenItem> = [
  { type: 'selection', width: 50 },
  { title: 'Token', key: 'summary', ellipsis: { tooltip: true } },
  { title: '用户名', key: 'userName', width: 100 },
  { title: '昵称', key: 'nickName', width: 100 },
  { title: '部门', key: 'dept', width: 120 },
  { title: '登录IP', key: 'ip', width: 130 },
  { title: '登录地点', key: 'address', ellipsis: { tooltip: true } },
  { title: '浏览器', key: 'browser', width: 100 },
  {
      title: '创建时间',
      key: 'loginTime',
      width: 180,
      render: (row) => formatDateTime(row.loginTime),
    },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right',
    align: 'center',
    render: (row) =>
      h(NButton, { size: 'small', type: 'error', tertiary: true, onClick: () => handleDelete(row.key) }, () => '强退'),
  },
];

async function loadData() {
  loading.value = true;
  try {
    const result = await getTokenList({
      page: pagination.page,
      size: pagination.pageSize,
      filter: searchText.value || undefined,
    });
    data.value = result.list;
    pagination.total = result.total;
  } catch (e: any) {
    console.error('Failed to load tokens:', e);
    data.value = [];
    pagination.total = 0;
    notification.error({
      content: '加载在线用户失败',
      description: e?.response?.data?.msg || e?.message || '请检查后端服务',
      duration: 4000,
    });
  } finally {
    loading.value = false;
  }
}

function handleDelete(key: string) {
  dialog.warning({
    title: '提示',
    content: '确定要强退该用户吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await delToken([key]);
        notification.success({ content: '已强退该用户', duration: 2000 });
        loadData();
      } catch (e: any) {
        console.error('Failed to delete token:', e);
        notification.error({ content: '操作失败', description: e.message || '强退失败', duration: 3000 });
      }
    },
  });
}

function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    notification.warning({ content: '请选择要强退的用户', duration: 2000 });
    return;
  }
  dialog.warning({
    title: '提示',
    content: `确定要强退选中的 ${selectedRowKeys.value.length} 个用户吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await delToken(selectedRowKeys.value);
        notification.success({ content: `已强退 ${selectedRowKeys.value.length} 个用户`, duration: 2000 });
        selectedRowKeys.value = [];
        loadData();
      } catch (e: any) {
        console.error('Failed to batch delete tokens:', e);
        notification.error({ content: '操作失败', description: e.message || '强退失败', duration: 3000 });
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

function handleCheckedRowKeysChange(keys: (string | number)[]) {
  selectedRowKeys.value = keys.map(String);
}

onMounted(() => loadData());
</script>

<template>
  <div class="p-4">
    <NCard :bordered="false" content-class="!p-4">
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <NInput
          v-model:value="searchText"
          placeholder="全表模糊搜索"
          clearable
          size="small"
          class="!w-52"
          @keyup.enter="handleSearch"
        />
        <NButton type="primary" size="small" @click="handleSearch">搜索</NButton>
        <NButton
          size="small"
          type="error"
          :disabled="selectedRowKeys.length === 0"
          @click="handleBatchDelete"
        >
          强退
        </NButton>
      </div>

      <NDataTable
        :loading="loading"
        :columns="columns"
        :data="data"
        :row-key="(row: TokenItem) => row.key"
        :checked-row-keys="selectedRowKeys"
        :scroll-x="1200"
        striped
        @update:checked-row-keys="handleCheckedRowKeysChange"
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