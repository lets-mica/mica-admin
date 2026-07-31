# CRUD 页面完整模板

以「轮播图」为例。替换 `Banner`/`banner`/`轮播图`/`system:banner:*` 即可。参照实现：`src/views/system/post/index.vue` + `src/api/system/post.ts`。

## 1. 接口层 `src/api/system/banner.ts`

```ts
import { api, parsePage } from '#/api/request';
import { exportExcel } from '#/utils/export-excel';

export interface BannerItem {
  id: number;
  title: string;
  imageUrl?: string;
  seq?: number;
  enabled?: boolean;
  remark?: string;
  createdAt?: string;
}

export async function getBannerList(params?: {
  current?: number;
  size?: number;
  title?: string;
}) {
  const data = await api.get<any>('/api/system/banner', { params });
  return parsePage<BannerItem>(data);
}

export async function addBanner(data: Partial<BannerItem>) {
  return api.post('/api/system/banner', data);
}

export async function editBanner(data: Partial<BannerItem>) {
  return api.put('/api/system/banner', data);
}

export async function deleteBanner(ids: number[]) {
  return api.delete('/api/system/banner', { data: ids });
}

export async function exportBannerExcel(params?: Record<string, unknown>) {
  return exportExcel({ api, url: '/api/system/banner/download', filename: '轮播图数据', params });
}
```

要点：删除是 `DELETE` + `{ data: ids }`（body 传 id 数组，对应后端 `Set<Long>`）；分页参数按后端 `Page<T>` 绑定用 `current` / `size`。若在 `src/api/system/index.ts` 有统一导出，记得补上。

## 2. 页面 `src/views/system/banner/index.vue` — script 部分

```vue
<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';
import type { BannerItem } from '#/api/system/banner';
import {
  addBanner, deleteBanner, editBanner, exportBannerExcel, getBannerList,
} from '#/api/system/banner';

import {
  NButton, NCard, NDataTable, NForm, NFormItem, NInput,
  NInputNumber, NPagination, NSpace, NSwitch, NTag,
} from 'naive-ui';
import { useAccess } from '@vben/access';
import { useVbenModal } from '@vben-core/popup-ui';
import { h, onMounted, reactive, ref } from 'vue';
import { dialog, notification } from '#/adapter/naive';
import { formatDateTime } from '#/utils/format-date';

defineOptions({ name: 'BannerManagement' });

const { hasAccessByCodes } = useAccess();
const canAccess = (codes: string | string[]) =>
  hasAccessByCodes(Array.isArray(codes) ? codes : [codes]);

const loading = ref(false);
const exporting = ref(false);
const data = ref<BannerItem[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, total: 0 });
const searchText = ref('');
const selectedRowKeys = ref<number[]>([]);
const isEdit = ref(false);

const [Modal, modalApi] = useVbenModal({ onConfirm: handleSubmit });

const formData = ref({
  id: undefined as number | undefined,
  title: '',
  imageUrl: '',
  seq: 0,
  enabled: true,
  remark: '',
});

const columns: DataTableColumns<BannerItem> = [
  { type: 'selection', width: 50 },
  { title: '标题', key: 'title', width: 200, ellipsis: { tooltip: true } },
  { title: '图片地址', key: 'imageUrl', width: 240, ellipsis: { tooltip: true } },
  { title: '排序', key: 'seq', width: 80, align: 'center' },
  {
    title: '状态',
    key: 'enabled',
    width: 80,
    align: 'center',
    render: (row) =>
      h(
        NTag,
        { type: row.enabled === false ? 'error' : 'success', size: 'small', bordered: false },
        () => (row.enabled === false ? '禁用' : '启用'),
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
      if (canAccess('system:banner:edit')) {
        actions.push(
          h(NButton, { size: 'small', type: 'primary', tertiary: true, onClick: () => handleEdit(row) }, () => '编辑'),
        );
      }
      if (canAccess('system:banner:del')) {
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
    const params: any = { current: pagination.page, size: pagination.pageSize };
    if (searchText.value) params.title = searchText.value;
    const result = await getBannerList(params);
    data.value = result.list;
    pagination.total = result.total;
  } catch (e: any) {
    console.error('Failed to load banners:', e);
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
  formData.value = { id: undefined, title: '', imageUrl: '', seq: 0, enabled: true, remark: '' };
}

function handleAdd() {
  isEdit.value = false;
  resetFormData();
  modalApi.setState({ title: '新增轮播图' }).open();
}

function handleEdit(row: BannerItem) {
  isEdit.value = true;
  formData.value = {
    id: row.id,
    title: row.title || '',
    imageUrl: row.imageUrl || '',
    seq: row.seq || 0,
    enabled: row.enabled !== false,
    remark: row.remark || '',
  };
  modalApi.setState({ title: '编辑轮播图' }).open();
}

function handleEditSelected() {
  if (selectedRowKeys.value.length !== 1) {
    notification.warning({ content: '请选择一条记录进行编辑', duration: 2000 });
    return;
  }
  const row = data.value.find((item) => item.id === selectedRowKeys.value[0]);
  if (row) handleEdit(row);
}

async function handleSubmit() {
  if (!formData.value.title) {
    notification.warning({ content: '请输入标题', duration: 2000 });
    return;
  }
  modalApi.lock();
  try {
    await (isEdit.value && formData.value.id
      ? editBanner(formData.value as any)
      : addBanner(formData.value as any));
    notification.success({ content: isEdit.value ? '已更新' : '已创建', duration: 2000 });
    modalApi.close();
    loadData();
  } catch (e: any) {
    console.error('Failed to submit:', e);
  } finally {
    modalApi.unlock();
  }
}

function handleDelete(id: number) {
  dialog.warning({
    title: '提示',
    content: '确定要删除该轮播图吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteBanner([id]);
        notification.success({ content: '已删除', duration: 2000 });
        loadData();
      } catch (e: any) {
        console.error('Failed to delete:', e);
      }
    },
  });
}

function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    notification.warning({ content: '请选择要删除的记录', duration: 2000 });
    return;
  }
  dialog.warning({
    title: '提示',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条记录吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteBanner(selectedRowKeys.value);
        notification.success({ content: '已删除', duration: 2000 });
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

async function handleExport() {
  exporting.value = true;
  try {
    const params: Record<string, unknown> = {};
    if (searchText.value) params.title = searchText.value;
    await exportBannerExcel(params);
    notification.success({ content: '导出成功', duration: 2000 });
  } catch (e: any) {
    console.error('Failed to export:', e);
  } finally {
    exporting.value = false;
  }
}

onMounted(() => loadData());
</script>
```

## 3. 页面 template 部分

```vue
<template>
  <div class="p-4">
    <NCard :bordered="false" content-class="!p-4">
      <!-- 搜索区 -->
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <NInput
          v-model:value="searchText"
          placeholder="搜索标题"
          clearable
          class="!w-64"
          @keyup.enter="handleSearch"
        />
        <NButton type="primary" size="small" @click="handleSearch">搜索</NButton>
        <NButton size="small" @click="handleReset">重置</NButton>
      </div>

      <!-- 工具栏 -->
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <NButton v-access:code="'system:banner:add'" type="primary" size="small" @click="handleAdd">
          新增
        </NButton>
        <NButton
          v-access:code="'system:banner:edit'"
          size="small"
          type="info"
          :disabled="selectedRowKeys.length !== 1"
          @click="handleEditSelected"
        >
          修改
        </NButton>
        <NButton
          v-access:code="'system:banner:del'"
          size="small"
          type="error"
          :disabled="selectedRowKeys.length === 0"
          @click="handleBatchDelete"
        >
          删除
        </NButton>
        <NButton
          v-access:code="'system:banner:export'"
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
        :row-key="(row: BannerItem) => row.id"
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
        <NFormItem label="标题" required>
          <NInput v-model:value="formData.title" placeholder="请输入标题" />
        </NFormItem>
        <NFormItem label="图片地址" required>
          <NInput v-model:value="formData.imageUrl" placeholder="请输入图片地址" />
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="formData.seq" :min="0" class="w-full" />
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
```

## 4. 挂菜单

前端不写静态路由。在后端 `docs/database/mysql.sql` 的 `sys_menu` 追加：`component` = `system/banner/index`（对应 `src/views/system/banner/index.vue`），`name` = `Banner`（全局唯一，与 `defineOptions({ name })` 对齐），并补 5 条 `menu_type=2` 的按钮权限行。授权角色后重新登录。

## 树形 / 非分页页面

参考 `src/views/system/dept/index.vue`（`NTree` / 树表）与 `src/views/system/menu/index.vue`。列表类无分页接口直接返回数组时，`parsePage` 也能兼容（`{ list: payload, total: payload.length }`）。
