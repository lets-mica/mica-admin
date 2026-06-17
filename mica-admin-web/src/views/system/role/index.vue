<script setup lang="ts">
import type { DataTableColumns, TreeOption } from 'naive-ui';
import type { MenuItem } from '#/api/system/menu';
import type { DeptItem } from '#/api/system/dept';
import type { RoleItem } from '#/api/system/role';
import {
  addRole,
  deleteRole,
  editRole,
  editRoleMenu,
  exportRoleExcel,
  getRole,
  getRoleList,
  getRoleMenu,
} from '#/api/system/role';
import { getMenuTree } from '#/api/system/menu';
import { getDepts, getDeptSuperior } from '#/api/system/dept';

import {
  NButton,
  NCard,
  NDataTable,
  NDatePicker,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NPagination,
  NSelect,
  NSpace,
  NTree,
  NTreeSelect,
} from 'naive-ui';
import { useAccess } from '@vben/access';
import { useVbenModal } from '@vben-core/popup-ui';
import { h, onMounted, reactive, ref } from 'vue';
import { dialog, notification } from '#/adapter/naive';
import { dayjs, formatDateTime } from '#/utils/format-date';

defineOptions({ name: 'RoleManagement' });
const { hasAccessByCodes } = useAccess();
const canAccess = (codes: string | string[]) =>
  hasAccessByCodes(Array.isArray(codes) ? codes : [codes]);

const DATA_SCOPE_OPTIONS = [
  { label: '全部', value: 1 },
  { label: '本级', value: 2 },
  { label: '自定义', value: 3 },
];

const loading = ref(false);
const data = ref<RoleItem[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, total: 0 });
const searchText = ref('');
const dateRange = ref<[number, number] | null>(null);
const selectedRowKeys = ref<number[]>([]);
const currentRoleId = ref<number | null>(null);
const tableMaxHeight = 420;

const menuTree = ref<TreeOption[]>([]);
const menuCheckedKeys = ref<number[]>([]);
const menuIndeterminateKeys = ref<number[]>([]);
const menuLoading = ref(false);
const canSaveMenu = ref(false);

const deptTreeOptions = ref<TreeOption[]>([]);
const isEdit = ref(false);

const [Modal, modalApi] = useVbenModal({
  onConfirm: handleSubmit,
});

const formData = ref({
  id: undefined as number | undefined,
  name: '',
  title: '',
  dataScope: 1,
  seq: 999,
  depts: [] as number[],
  remark: '',
});

function dataScopeLabel(scope?: number) {
  return DATA_SCOPE_OPTIONS.find((o) => o.value === scope)?.label || '-';
}

function isRootParent(parentId?: number | null) {
  return parentId === undefined || parentId === null || parentId === 0;
}

function buildMenuTree(items: MenuItem[], parentId?: number | null): TreeOption[] {
  return items
    .filter((item) =>
      isRootParent(parentId) ? isRootParent(item.parentId) : item.parentId === parentId,
    )
    .map((item) => ({
      key: item.id,
      label: item.title || item.name,
      children: buildMenuTree(items, item.id),
    }));
}

function buildDeptTree(items: DeptItem[], parentId?: number | null): DeptItem[] {
  return items
    .filter((item) =>
      isRootParent(parentId) ? isRootParent(item.parentId) : item.parentId === parentId,
    )
    .map((item) => ({
      ...item,
      children: buildDeptTree(items, item.id),
    }));
}

function deptToTreeOption(items: DeptItem[]): TreeOption[] {
  return items.map((d) => ({
    key: d.id,
    label: d.name,
    children: d.children?.length ? deptToTreeOption(d.children) : undefined,
  }));
}

const columns: DataTableColumns<RoleItem> = [
  { type: 'selection', width: 50 },
  { title: '名称', key: 'name', width: 120, ellipsis: { tooltip: true } },
  { title: '标识', key: 'title', width: 120, ellipsis: { tooltip: true } },
  {
    title: '数据权限',
    key: 'dataScope',
    width: 100,
    render: (row) => dataScopeLabel(row.dataScope),
  },
  { title: '排序', key: 'seq', width: 70, align: 'center' },
  {
    title: '描述',
    key: 'remark',
    minWidth: 100,
    ellipsis: { tooltip: true },
    render: (row) => row.remark || '-',
  },
  {
    title: '创建日期',
    key: 'createdAt',
    width: 170,
    render: (row) => formatDateTime(row.createdAt),
  },
  {
    title: '操作',
    key: 'action',
    width: 130,
    fixed: 'right',
    align: 'center',
    render: (row) => {
      const actions: ReturnType<typeof h>[] = [];
      if (canAccess('system:role:edit')) {
        actions.push(
          h(
            NButton,
            { size: 'small', type: 'primary', tertiary: true, onClick: () => handleEdit(row) },
            () => '编辑',
          ),
        );
      }
      if (canAccess('system:role:del')) {
        actions.push(
          h(
            NButton,
            { size: 'small', type: 'error', tertiary: true, onClick: () => handleDelete(row.id) },
            () => '删除',
          ),
        );
      }
      return actions.length > 0 ? h(NSpace, { size: 'small' }, () => actions) : '-';
    },
  },
];

function buildQueryParams() {
  const params: Parameters<typeof getRoleList>[0] = {
    page: pagination.page,
    size: pagination.pageSize,
    sort: 'seq,desc',
  };
  if (searchText.value) params!.blurry = searchText.value;
  if (dateRange.value) {
    const [start, end] = dateRange.value;
    params!.createTime = [
      dayjs(start).startOf('day').format('YYYY-MM-DD HH:mm:ss'),
      dayjs(end).endOf('day').format('YYYY-MM-DD HH:mm:ss'),
    ];
  }
  return params;
}

async function loadData() {
  loading.value = true;
  clearMenuPanel();
  try {
    const result = await getRoleList(buildQueryParams());
    data.value = result.list;
    pagination.total = result.total;
  } catch (e: any) {
    console.error('Failed to load roles:', e);
    data.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
}

async function loadMenuTree() {
  try {
    const result = await getMenuTree();
    const list = (Array.isArray(result) ? result : []) as MenuItem[];
    menuTree.value = buildMenuTree(list);
  } catch (e: any) {
    console.error('Failed to load menu tree:', e);
    menuTree.value = [];
  }
}

async function loadDeptOptionsForForm(deptIds?: number[]) {
  try {
    const list = deptIds?.length
      ? await getDeptSuperior(deptIds)
      : await getDepts({ enabled: 1 });
    deptTreeOptions.value = deptToTreeOption(buildDeptTree(list));
  } catch (e: any) {
    console.error('Failed to load depts:', e);
    deptTreeOptions.value = [];
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
  formData.value = {
    id: undefined,
    name: '',
    title: '',
    dataScope: 1,
    seq: 999,
    depts: [],
    remark: '',
  };
  deptTreeOptions.value = [];
}

function handleAdd() {
  isEdit.value = false;
  resetFormData();
  modalApi.setState({ title: '新增角色' }).open();
}

function handleEditSelected() {
  if (selectedRowKeys.value.length !== 1) {
    notification.warning({ content: '请选择一条记录进行编辑', duration: 2000 });
    return;
  }
  const row = data.value.find((item) => item.id === selectedRowKeys.value[0]);
  if (row) handleEdit(row);
}

async function handleEdit(row: RoleItem) {
  isEdit.value = true;
  resetFormData();
  try {
    const detail = await getRole(row.id);
    formData.value = {
      id: detail.id,
      name: detail.name || '',
      title: detail.title || '',
      dataScope: detail.dataScope ?? 1,
      seq: detail.seq ?? 999,
      depts: detail.depts || [],
      remark: detail.remark || '',
    };
  } catch {
    formData.value = {
      id: row.id,
      name: row.name || '',
      title: row.title || '',
      dataScope: row.dataScope ?? 1,
      seq: row.seq ?? 999,
      depts: row.depts || [],
      remark: row.remark || '',
    };
  }
  if (formData.value.dataScope === 3) {
    await loadDeptOptionsForForm(formData.value.depts);
  }
  modalApi.setState({ title: '编辑角色' }).open();
}

async function handleDataScopeChange(scope: number) {
  formData.value.dataScope = scope;
  if (scope === 3) {
    await loadDeptOptionsForForm(formData.value.depts);
  } else {
    formData.value.depts = [];
    deptTreeOptions.value = [];
  }
}

async function handleSubmit() {
  if (!formData.value.name?.trim()) {
    notification.warning({ content: '请输入名称', duration: 2000 });
    return;
  }
  if (!formData.value.title?.trim()) {
    notification.warning({ content: '请输入标识', duration: 2000 });
    return;
  }
  if (formData.value.dataScope === 3 && formData.value.depts.length === 0) {
    notification.warning({ content: '自定义数据权限不能为空', duration: 2000 });
    return;
  }

  const payload = {
    ...formData.value,
    depts: formData.value.dataScope === 3 ? formData.value.depts : [],
  };

  modalApi.lock();
  try {
    if (isEdit.value && formData.value.id) {
      await editRole(payload);
      notification.success({ content: '角色已更新', duration: 2000 });
    } else {
      await addRole(payload);
      notification.success({ content: '角色已创建', duration: 2000 });
    }
    modalApi.close();
    loadData();
  } catch (e: any) {
    console.error('Failed to submit role:', e);
  } finally {
    modalApi.unlock();
  }
}

function handleDelete(id: number) {
  dialog.warning({
    title: '提示',
    content: '确定要删除该角色吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteRole([id]);
        notification.success({ content: '角色已删除', duration: 2000 });
        if (currentRoleId.value === id) {
          clearMenuPanel();
        }
        loadData();
      } catch (e: any) {
        console.error('Failed to delete role:', e);
      }
    },
  });
}

function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    notification.warning({ content: '请选择要删除的角色', duration: 2000 });
    return;
  }
  dialog.warning({
    title: '提示',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个角色吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteRole(selectedRowKeys.value);
        notification.success({
          content: `已删除 ${selectedRowKeys.value.length} 个角色`,
          duration: 2000,
        });
        if (currentRoleId.value && selectedRowKeys.value.includes(currentRoleId.value)) {
          clearMenuPanel();
        }
        selectedRowKeys.value = [];
        loadData();
      } catch (e: any) {
        console.error('Failed to batch delete roles:', e);
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
    const params = buildQueryParams() as Record<string, unknown>;
    delete params.page;
    delete params.size;
    delete params.sort;
    await exportRoleExcel(params);
    notification.success({ content: '导出成功', duration: 2000 });
  } catch (e: any) {
    console.error('Failed to export roles:', e);
  } finally {
    exporting.value = false;
  }
}

function clearMenuPanel() {
  currentRoleId.value = null;
  menuCheckedKeys.value = [];
  menuIndeterminateKeys.value = [];
  canSaveMenu.value = false;
}

async function handleRoleRowClick(row: RoleItem) {
  currentRoleId.value = row.id;
  canSaveMenu.value = true;
  menuCheckedKeys.value = [];
  menuIndeterminateKeys.value = [];
  try {
    menuCheckedKeys.value = await getRoleMenu(row.id);
  } catch (e: any) {
    console.error('Failed to load role menus:', e);
    menuCheckedKeys.value = [];
  }
}

function handleMenuCheck(keys: Array<string | number>) {
  menuCheckedKeys.value = keys.map(Number);
}

function handleMenuIndeterminate(keys: Array<string | number>) {
  menuIndeterminateKeys.value = keys.map(Number);
}

async function saveMenu() {
  if (!currentRoleId.value) {
    notification.warning({ content: '请先选择角色', duration: 2000 });
    return;
  }
  const menuIds = [
    ...new Set([...menuIndeterminateKeys.value, ...menuCheckedKeys.value]),
  ];
  if (menuIds.length === 0) {
    notification.warning({ content: '请至少选择一个菜单', duration: 2000 });
    return;
  }
  menuLoading.value = true;
  try {
    await editRoleMenu({ id: currentRoleId.value, menuIds });
    notification.success({ content: '菜单分配已保存', duration: 2000 });
  } catch (e: any) {
    console.error('Failed to save role menus:', e);
  } finally {
    menuLoading.value = false;
  }
}

function rowProps(row: RoleItem) {
  return {
    style: currentRoleId.value === row.id ? 'cursor: pointer' : 'cursor: pointer',
    class: currentRoleId.value === row.id ? 'role-row--active' : '',
    onClick: () => handleRoleRowClick(row),
  };
}

onMounted(() => {
  loadData();
  loadMenuTree();
});
</script>

<template>
  <div class="role-page p-4">
    <div class="role-page__layout">
      <!-- 左侧：角色列表 -->
      <NCard class="role-page__list" size="small" content-class="!p-4">
        <template #header>
          <span class="font-semibold">角色列表</span>
        </template>

        <div class="mb-3 flex flex-wrap items-center gap-2">
          <NInput
            v-model:value="searchText"
            placeholder="输入名称或者描述搜索"
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

        <div class="mb-3 flex flex-wrap items-center gap-2">
          <NButton v-access:code="'system:role:add'" type="primary" size="small" @click="handleAdd">新增</NButton>
          <NButton
            v-access:code="'system:role:edit'"
            size="small"
            type="info"
            :disabled="selectedRowKeys.length !== 1"
            @click="handleEditSelected"
          >
            修改
          </NButton>
          <NButton
            v-access:code="'system:role:del'"
            size="small"
            type="error"
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
          >
            删除
          </NButton>
          <NButton
            v-access:code="'system:role:export'"
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
          :row-key="(row: RoleItem) => row.id"
          :checked-row-keys="selectedRowKeys"
          :scroll-x="900"
          :max-height="tableMaxHeight"
          :row-props="rowProps"
          striped
          size="small"
          @update:checked-row-keys="handleCheckedRowKeysChange"
        />

        <div class="mt-3 flex items-center justify-between">
          <span class="text-muted-foreground text-sm">共 {{ pagination.total }} 条</span>
          <NPagination
            v-model:page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :item-count="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            size="small"
            show-size-picker
            @update:page="handlePageChange"
            @update:page-size="handlePageSizeChange"
          />
        </div>
      </NCard>

      <!-- 右侧：菜单分配 -->
      <NCard class="role-page__menu" size="small" content-class="!p-3">
        <template #header>
          <div class="flex w-full items-center justify-between gap-2">
            <span class="font-semibold">菜单分配</span>
            <NButton
              v-access:code="'system:role:edit'"
              type="primary"
              size="small"
              :disabled="!canSaveMenu"
              :loading="menuLoading"
              @click="saveMenu"
            >
              保存
            </NButton>
          </div>
        </template>
        <p v-if="!canSaveMenu" class="text-muted-foreground mb-2 text-xs">
          点击左侧表格中的角色行以分配菜单
        </p>
        <div class="menu-tree-wrapper">
          <NTree
            :data="menuTree"
            :checkable="true"
            :cascade="false"
            :checked-keys="menuCheckedKeys"
            :indeterminate-keys="menuIndeterminateKeys"
            :disabled="!canSaveMenu"
            block-line
            block-node
            default-expand-all
            @update:checked-keys="handleMenuCheck"
            @update:indeterminate-keys="handleMenuIndeterminate"
          />
        </div>
      </NCard>
    </div>

    <Modal class="w-[520px]">
      <NForm :model="formData" label-placement="left" :label-width="80">
        <NFormItem label="名称" required>
          <NInput v-model:value="formData.name" placeholder="请输入名称" />
        </NFormItem>
        <NFormItem label="标识" required>
          <NInput
            v-model:value="formData.title"
            :disabled="formData.title === 'admin'"
            placeholder="请输入标识"
          />
        </NFormItem>
        <NFormItem label="数据范围" required>
          <NSelect
            :value="formData.dataScope"
            :options="DATA_SCOPE_OPTIONS"
            placeholder="请选择数据范围"
            @update:value="handleDataScopeChange"
          />
        </NFormItem>
        <NFormItem v-if="formData.dataScope === 3" label="数据权限" required>
          <NTreeSelect
            v-model:value="formData.depts"
            :options="deptTreeOptions"
            multiple
            clearable
            default-expand-all
            placeholder="请选择"
            class="w-full"
          />
        </NFormItem>
        <NFormItem label="排序" required>
          <NInputNumber
            v-model:value="formData.seq"
            :min="0"
            :max="999"
            class="w-full"
          />
        </NFormItem>
        <NFormItem label="描述信息">
          <NInput
            v-model:value="formData.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入描述"
          />
        </NFormItem>
      </NForm>
    </Modal>
  </div>
</template>

<style scoped>
.role-page__layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 360px);
  gap: 16px;
  align-items: start;
}

.role-page__list,
.role-page__menu {
  min-width: 0;
}

.menu-tree-wrapper {
  max-height: calc(100vh - 280px);
  overflow-y: auto;
}

:deep(.role-row--active td) {
  background-color: hsl(var(--accent) / 0.35) !important;
}

@media (max-width: 992px) {
  .role-page__layout {
    grid-template-columns: 1fr;
  }
}
</style>
