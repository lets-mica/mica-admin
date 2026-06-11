<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';
import type { PostItem } from '#/api/system/post';
import { addPost, deletePost, editPost, exportPostExcel, getPostList } from '#/api/system/post';

import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NPagination,
  NSpace,
  NSwitch,
  NTag,
} from 'naive-ui';
import { useAccess } from '@vben/access';
import { useVbenModal } from '@vben-core/popup-ui';
import { h, onMounted, reactive, ref } from 'vue';
import { dialog, notification } from '#/adapter/naive';
import { formatDateTime } from '#/utils/format-date';

defineOptions({ name: 'PostManagement' });
const { hasAccessByCodes } = useAccess();
const canAccess = (codes: string | string[]) =>
  hasAccessByCodes(Array.isArray(codes) ? codes : [codes]);

const loading = ref(false);
const data = ref<PostItem[]>([]);
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
  name: '',
  code: '',
  sort: 0,
  enabled: true,
  remark: '',
});

const columns: DataTableColumns<PostItem> = [
  { type: 'selection', width: 50 },
  { title: '岗位名称', key: 'name', width: 200, ellipsis: { tooltip: true } },
  { title: '岗位编码', key: 'code', width: 200, ellipsis: { tooltip: true } },
  { title: '排序', key: 'sort', width: 80, align: 'center' },
  {
    title: '状态',
    key: 'enabled',
    width: 80,
    align: 'center',
    render: (row) =>
      h(NTag, { type: row.enabled !== false ? 'success' : 'error', size: 'small', bordered: false }, () =>
        row.enabled !== false ? '启用' : '禁用',
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
      if (canAccess('system:post:edit')) {
        actions.push(
          h(NButton, { size: 'small', type: 'primary', tertiary: true, onClick: () => handleEdit(row) }, () => '编辑'),
        );
      }
      if (canAccess('system:post:del')) {
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

    const result = await getPostList(params);
    data.value = result.list;
    pagination.total = result.total;
  } catch (e: any) {
    console.error('Failed to load posts:', e);
    data.value = [];
    pagination.total = 0;
    notification.error({
      content: '加载岗位失败',
      description: e?.response?.data?.msg || e?.message || '请检查后端服务',
      duration: 4000,
    });
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
  formData.value = { id: undefined, name: '', code: '', sort: 0, enabled: true, remark: '' };
}

function handleAdd() {
  isEdit.value = false;
  resetFormData();
  modalApi.setState({ title: '新增岗位' }).open();
}

function handleEditSelected() {
  if (selectedRowKeys.value.length !== 1) {
    notification.warning({ content: '请选择一条记录进行编辑', duration: 2000 });
    return;
  }
  const row = data.value.find((item) => item.id === selectedRowKeys.value[0]);
  if (row) handleEdit(row);
}

function handleEdit(row: PostItem) {
  isEdit.value = true;
  formData.value = {
    id: row.id,
    name: row.name || '',
    code: (row as any).code || '',
    sort: (row as any).sort || 0,
    enabled: (row as any).enabled !== false,
    remark: (row as any).remark || '',
  };
  modalApi.setState({ title: '编辑岗位' }).open();
}

async function handleSubmit() {
  if (!formData.value.name) {
    notification.warning({ content: '请输入岗位名称', duration: 2000 });
    return;
  }
  if (!formData.value.code) {
    notification.warning({ content: '请输入岗位编码', duration: 2000 });
    return;
  }
  dialogLoading.value = true;
  modalApi.lock();
  try {
    if (isEdit.value && formData.value.id) {
      await editPost(formData.value as any);
    } else {
      await addPost(formData.value as any);
    }
    notification.success({ content: isEdit.value ? '岗位已更新' : '岗位已创建', duration: 2000 });
    modalApi.close();
    loadData();
  } catch (e: any) {
    console.error('Failed to submit:', e);
    notification.error({ content: '操作失败', description: e.message || '保存失败', duration: 3000 });
  } finally {
    dialogLoading.value = false;
    modalApi.unlock();
  }
}

function handleDelete(id: number) {
  dialog.warning({
    title: '提示',
    content: '确定要删除该岗位吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deletePost([id] as any);
        notification.success({ content: '岗位已删除', duration: 2000 });
        loadData();
      } catch (e: any) {
        console.error('Failed to delete:', e);
        notification.error({ content: '操作失败', description: e.message || '删除失败', duration: 3000 });
      }
    },
  });
}

function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    notification.warning({ content: '请选择要删除的岗位', duration: 2000 });
    return;
  }
  dialog.warning({
    title: '提示',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个岗位吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deletePost(selectedRowKeys.value as any);
        notification.success({ content: `已删除 ${selectedRowKeys.value.length} 个岗位`, duration: 2000 });
        selectedRowKeys.value = [];
        loadData();
      } catch (e: any) {
        console.error('Failed to batch delete:', e);
        notification.error({ content: '操作失败', description: e.message || '删除失败', duration: 3000 });
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
    await exportPostExcel(params);
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
      <!-- 搜索区 -->
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <NInput
          v-model:value="searchText"
          placeholder="搜索岗位名称"
          clearable
          class="!w-64"
          @keyup.enter="handleSearch"
        />
        <NButton type="primary" size="small" @click="handleSearch">搜索</NButton>
        <NButton size="small" @click="handleReset">重置</NButton>
      </div>

      <!-- 工具栏 -->
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <NButton v-access:code="'system:post:add'" type="primary" size="small" @click="handleAdd">新增</NButton>
        <NButton
          v-access:code="'system:post:edit'"
          size="small"
          type="info"
          :disabled="selectedRowKeys.length !== 1"
          @click="handleEditSelected"
        >
          修改
        </NButton>
        <NButton
          v-access:code="'system:post:del'"
          size="small"
          type="error"
          :disabled="selectedRowKeys.length === 0"
          @click="handleBatchDelete"
        >
          删除
        </NButton>
        <NButton
          v-access:code="'system:post:export'"
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
        :row-key="(row: PostItem) => row.id"
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

    <Modal class="w-[520px]">
      <NForm :model="formData" label-placement="left" :label-width="80">
        <NFormItem label="岗位名称" required>
          <NInput v-model:value="formData.name" placeholder="请输入岗位名称" />
        </NFormItem>
        <NFormItem label="岗位编码" required>
          <NInput v-model:value="formData.code" placeholder="请输入岗位编码" />
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="formData.sort" :min="0" class="w-full" />
        </NFormItem>
        <NFormItem label="状态">
          <NSwitch v-model:value="formData.enabled" />
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </NFormItem>
      </NForm>
    </Modal>
  </div>
</template>