<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';
import type { UserMessageItem } from '#/api/system/user-message';
import { getMyMessages, markAllAsRead, markAsRead } from '#/api/system/user-message';

import {
  NButton,
  NCard,
  NDataTable,
  NDatePicker,
  NInput,
  NPagination,
  NSpace,
  NTag,
} from 'naive-ui';
import { useVbenModal } from '@vben-core/popup-ui';
import { h, onMounted, reactive, ref } from 'vue';
import { notification } from '#/adapter/naive';
import { dayjs, formatDateTime } from '#/utils/format-date';

defineOptions({ name: 'UserMessageList' });

const loading = ref(false);
const data = ref<UserMessageItem[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, total: 0 });
const searchText = ref('');
const dateRange = ref<[number, number] | null>(null);

const [DetailModal, detailModalApi] = useVbenModal({
  onConfirm: () => detailModalApi.close(),
  showConfirmButton: false,
});

const detailData = ref<UserMessageItem | null>(null);

const columns: DataTableColumns<UserMessageItem> = [
  {
    title: '分类',
    key: 'category',
    width: 120,
    render: (row) =>
      h(NTag, { size: 'small', bordered: false }, () => (row as any).category || '-'),
  },
  { title: '标题', key: 'title', ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'readFlag',
    width: 80,
    align: 'center',
    render: (row) =>
      h(NTag, { type: (row as any).readFlag === '1' ? 'success' : 'warning', size: 'small', bordered: false }, () =>
        (row as any).readFlag === '1' ? '已读' : '未读',
      ),
  },
  {
    title: '时间',
    key: 'createdAt',
    width: 170,
    render: (row) => formatDateTime(row.createdAt),
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right',
    align: 'center',
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(NButton, { size: 'small', type: 'primary', tertiary: true, onClick: () => handleViewDetail(row) }, () => '查看'),
      ]),
  },
];

async function loadData() {
  loading.value = true;
  try {
    const params: Record<string, unknown> = { page: pagination.page, size: pagination.pageSize };
    if (searchText.value) params.blurry = searchText.value;
    if (dateRange.value) {
      const [start, end] = dateRange.value;
      params.createTime = [
        dayjs(start).startOf('day').format('YYYY-MM-DD HH:mm:ss'),
        dayjs(end).endOf('day').format('YYYY-MM-DD HH:mm:ss'),
      ];
    }
    const result = await getMyMessages(params);
    data.value = result.list;
    pagination.total = result.total;
  } catch (e: any) {
    console.error('Failed to load messages:', e);
    data.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.page = 1;
  loadData();
}

function handleReset() {
  searchText.value = '';
  dateRange.value = null;
  pagination.page = 1;
  loadData();
}

async function handleViewDetail(row: UserMessageItem) {
  detailData.value = row;
  detailModalApi.setState({ title: row.title }).open();
  // 如果是未读，标记为已读
  if (row.readFlag === '0') {
    try {
      await markAsRead(row.id);
      row.readFlag = '1';
    } catch (e: any) {
      console.error('Failed to mark read:', e);
    }
  }
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead();
    notification.success({ content: '已全部标记为已读', duration: 2000 });
    loadData();
  } catch (e: any) {
    notification.error({ content: '操作失败', description: e.message || '', duration: 3000 });
  }
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

onMounted(() => loadData());
</script>

<template>
  <div class="p-4">
    <NCard :bordered="false" content-class="!p-4">
      <!-- 搜索区 -->
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <NInput
          v-model:value="searchText"
          placeholder="搜索消息标题"
          clearable
          size="small"
          class="!w-52"
          @keyup.enter="handleSearch"
        />
        <NDatePicker
          v-model:value="dateRange"
          type="daterange"
          clearable
          size="small"
          class="!w-72"
        />
        <NButton type="primary" size="small" @click="handleSearch">搜索</NButton>
        <NButton size="small" @click="handleReset">重置</NButton>
      </div>

      <!-- 工具栏 -->
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <NButton size="small" type="primary" @click="handleMarkAllRead">全部已读</NButton>
      </div>

      <NDataTable
        :loading="loading"
        :columns="columns"
        :data="data"
        :row-key="(row: UserMessageItem) => row.id"
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

    <DetailModal class="w-[640px]">
      <div v-if="detailData" class="space-y-4 p-2">
        <div class="flex items-center gap-3 text-sm text-gray-500">
          <NTag size="small" :bordered="false">{{ detailData.category || '-' }}</NTag>
          <span>{{ formatDateTime(detailData.createdAt) }}</span>
          <NTag :type="detailData.readFlag === '1' ? 'success' : 'warning'" size="small" :bordered="false">
            {{ detailData.readFlag === '1' ? '已读' : '未读' }}
          </NTag>
        </div>
        <div class="whitespace-pre-wrap text-sm leading-6">{{ detailData.content || '暂无内容' }}</div>
      </div>
    </DetailModal>
  </div>
</template>