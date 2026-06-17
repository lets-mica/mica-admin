<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';
import type { NoticeItem } from '#/api/system/notice';
import { addNotice, deleteNotice, editNotice, exportNoticeExcel, getNoticeList } from '#/api/system/notice';

import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NPagination,
  NSelect,
  NSpace,
  NSwitch,
  NTag,
} from 'naive-ui';
import { useAccess } from '@vben/access';
import { useVbenModal } from '@vben-core/popup-ui';
import { h, onMounted, reactive, ref } from 'vue';
import { dialog, notification } from '#/adapter/naive';
import { formatDateTime } from '#/utils/format-date';

defineOptions({ name: 'NoticeManagement' });
const { hasAccessByCodes } = useAccess();
const canAccess = (codes: string | string[]) =>
  hasAccessByCodes(Array.isArray(codes) ? codes : [codes]);

const loading = ref(false);
const data = ref<NoticeItem[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, total: 0 });
const searchText = ref('');
const selectedRowKeys = ref<number[]>([]);

const dialogLoading = ref(false);
const isEdit = ref(false);

const [Modal, modalApi] = useVbenModal({
  onConfirm: handleSubmit,
});

const formData = ref({
  id: undefined as number | undefined,
  title: '',
  content: '',
  type: 1,
  enabled: true,
});

const typeOptions = [
  { label: '通知', value: 1 },
  { label: '公告', value: 2 },
];

const typeColors: Record<number, 'info' | 'success'> = {
  1: 'info',
  2: 'success',
};

const columns: DataTableColumns<NoticeItem> = [
  { type: 'selection', width: 50 },
  { title: '标题', key: 'title', width: 220, ellipsis: { tooltip: true } },
  {
    title: '类型',
    key: 'type',
    width: 80,
    align: 'center',
    render: (row) =>
      h(NTag, { type: typeColors[(row as any).type] || 'default', size: 'small', bordered: false }, () =>
        typeOptions.find((o) => o.value === (row as any).type)?.label || '-',
      ),
  },
  { title: '内容', key: 'content', ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'enabled',
    width: 80,
    align: 'center',
    render: (row) =>
      h(NTag, { type: (row as any).enabled !== false ? 'success' : 'default', size: 'small', bordered: false }, () =>
        (row as any).enabled !== false ? '发布' : '草稿',
      ),
  },
  {
      title: '创建时间',
      key: 'createdAt',
      width: 170,
      render: (row) => formatDateTime(row.createdAt),
    },
  {
    title: '操作',
    key: 'action',
    width: 140,
    fixed: 'right',
    align: 'center',
    render: (row) => {
      const actions: ReturnType<typeof h>[] = [];
      if (canAccess('system:notice:edit')) {
        actions.push(
          h(NButton, { size: 'small', type: 'primary', tertiary: true, onClick: () => handleEdit(row) }, () => '编辑'),
        );
      }
      if (canAccess('system:notice:del')) {
        actions.push(
          h(NButton, { size: 'small', type: 'error', tertiary: true, onClick: () => handleDelete(row.id) }, () => '删除'),
        );
      }
      return actions.length > 0 ? h(NSpace, { size: 'small' }, () => actions) : '-';
    },
  },
];

async function loadData() {
  loading.value = true;
  try {
    const params: any = { page: pagination.page, size: pagination.pageSize };
    if (searchText.value) params.blurry = searchText.value;

    const result = await getNoticeList(params);
    data.value = result.list;
    pagination.total = result.total;
  } catch (e: any) {
    console.error('Failed to load notices:', e);
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

function resetFormData() {
  formData.value = { id: undefined, title: '', content: '', type: 1, enabled: true };
}

function handleAdd() {
  isEdit.value = false;
  resetFormData();
  modalApi.setState({ title: '新增通知' }).open();
}

function handleEditSelected() {
  if (selectedRowKeys.value.length !== 1) {
    notification.warning({ content: '请选择一条记录进行编辑', duration: 2000 });
    return;
  }
  const row = data.value.find((item) => item.id === selectedRowKeys.value[0]);
  if (row) handleEdit(row);
}

function handleEdit(row: NoticeItem) {
  isEdit.value = true;
  formData.value = {
    id: row.id,
    title: (row as any).title || '',
    content: (row as any).content || '',
    type: (row as any).type || 1,
    enabled: (row as any).enabled !== false,
  };
  modalApi.setState({ title: '编辑通知' }).open();
}

async function handleSubmit() {
  if (!formData.value.title) {
    notification.warning({ content: '请输入标题', duration: 2000 });
    return;
  }
  if (!formData.value.content) {
    notification.warning({ content: '请输入内容', duration: 2000 });
    return;
  }
  dialogLoading.value = true;
  modalApi.lock();
  try {
    if (isEdit.value && formData.value.id) {
      await editNotice(formData.value as any);
    } else {
      await addNotice(formData.value as any);
    }
    notification.success({ content: isEdit.value ? '通知已更新' : '通知已创建', duration: 2000 });
    modalApi.close();
    loadData();
  } catch (e: any) {
    console.error('Failed to submit:', e);
  } finally {
    dialogLoading.value = false;
    modalApi.unlock();
  }
}

function handleDelete(id: number) {
  dialog.warning({
    title: '提示',
    content: '确定要删除该通知吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteNotice([id]);
        notification.success({ content: '通知已删除', duration: 2000 });
        loadData();
      } catch (e: any) {
        console.error('Failed to delete:', e);
      }
    },
  });
}

function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    notification.warning({ content: '请选择要删除的通知', duration: 2000 });
    return;
  }
  dialog.warning({
    title: '提示',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个通知吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteNotice(selectedRowKeys.value);
        notification.success({ content: `已删除 ${selectedRowKeys.value.length} 个通知`, duration: 2000 });
        selectedRowKeys.value = [];
        loadData();
      } catch (e: any) {
        console.error('Failed to batch delete:', e);
      }
    },
  });
}

function handleCheckedRowKeysChange(keys: (string | number)[]) {
  selectedRowKeys.value = keys.map(Number);
}

const exporting = ref(false);

async function handleExport() {
  exporting.value = true;
  try {
    const params: Record<string, unknown> = {};
    if (searchText.value) params.blurry = searchText.value;
    await exportNoticeExcel(params);
    notification.success({ content: '导出成功', duration: 2000 });
  } catch (e: any) {
    console.error('Failed to export:', e);
  } finally {
    exporting.value = false;
  }
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
          placeholder="搜索通知标题"
          clearable
          class="!w-64"
          @keyup.enter="handleSearch"
        />
        <NButton type="primary" size="small" @click="handleSearch">搜索</NButton>
        <NButton size="small" @click="handleReset">重置</NButton>
      </div>

      <!-- 工具栏 -->
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <NButton v-access:code="'system:notice:add'" type="primary" size="small" @click="handleAdd">新增</NButton>
        <NButton
          v-access:code="'system:notice:edit'"
          size="small"
          type="info"
          :disabled="selectedRowKeys.length !== 1"
          @click="handleEditSelected"
        >
          修改
        </NButton>
        <NButton
          v-access:code="'system:notice:del'"
          size="small"
          type="error"
          :disabled="selectedRowKeys.length === 0"
          @click="handleBatchDelete"
        >
          删除
        </NButton>
        <NButton
          v-access:code="'system:notice:export'"
          size="small"
          type="warning"
          :loading="exporting"
          @click="handleExport"
        >
          导出
        </NButton>
      </div>

      <NDataTable
        :loading="loading"
        :columns="columns"
        :data="data"
        :row-key="(row: NoticeItem) => row.id"
        :checked-row-keys="selectedRowKeys"
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

    <Modal class="w-[640px]">
      <NForm :model="formData" label-placement="left" :label-width="80">
        <NFormItem label="标题" required>
          <NInput v-model:value="formData.title" placeholder="请输入标题" />
        </NFormItem>
        <NFormItem label="类型">
          <NSelect v-model:value="formData.type" :options="typeOptions" />
        </NFormItem>
        <NFormItem label="内容" required>
          <NInput v-model:value="formData.content" type="textarea" :rows="6" placeholder="请输入内容" />
        </NFormItem>
        <NFormItem label="状态">
          <NSwitch v-model:value="formData.enabled">
            <template #checked>发布</template>
            <template #unchecked>草稿</template>
          </NSwitch>
        </NFormItem>
      </NForm>
    </Modal>
  </div>
</template>