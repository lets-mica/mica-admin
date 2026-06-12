<script setup lang="ts">
import type { DataTableColumns, TreeSelectOption } from 'naive-ui';
import type { DeptItem } from '#/api/system/dept';
import {
  addDept,
  buildDeptTree,
  deleteDept,
  editDept,
  exportDeptExcel,
  getDeptList,
  getDeptSuperior,
  getDepts,
  isRootDept,
} from '#/api/system/dept';

import {
  NButton,
  NCard,
  NDataTable,
  NDatePicker,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NRadio,
  NRadioGroup,
  NSelect,
  NSpace,
  NSwitch,
  NTreeSelect,
} from 'naive-ui';
import { useAccess } from '@vben/access';
import { useVbenModal } from '@vben-core/popup-ui';
import { h, onMounted, ref } from 'vue';
import { dialog, notification } from '#/adapter/naive';
import { dayjs, formatDateTime } from '#/utils/format-date';

defineOptions({ name: 'DeptManagement' });
const { hasAccessByCodes } = useAccess();
const canAccess = (codes: string | string[]) =>
  hasAccessByCodes(Array.isArray(codes) ? codes : [codes]);

const ENABLED_FILTER_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

const ENABLED_FORM_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

const loading = ref(false);
const data = ref<DeptItem[]>([]);
const searchName = ref('');
const enabledFilter = ref<number | null>(null);
const dateRange = ref<[number, number] | null>(null);
const selectedRowKeys = ref<number[]>([]);
const tableMaxHeight = 520;

const parentDeptOptions = ref<TreeSelectOption[]>([]);
const isEdit = ref(false);

const [Modal, modalApi] = useVbenModal({
  onConfirm: handleSubmit,
});

const formData = ref({
  id: undefined as number | undefined,
  name: '',
  seq: 999,
  isTop: '1' as '0' | '1',
  pid: undefined as number | undefined,
  enabled: 1,
});

function isProtectedDept(row: DeptItem) {
  return isRootDept(row.parentId);
}

function isDeptEnabled(row: DeptItem) {
  return row.enabled === 1 || row.enabled === true;
}

function deptToTreeSelect(items: DeptItem[]): TreeSelectOption[] {
  return items.map((d) => ({
    key: d.id,
    label: d.name,
    children:
      d.children?.length && d.children.length > 0
        ? deptToTreeSelect(d.children)
        : undefined,
  }));
}

function buildParentTreeFromFlat(items: DeptItem[]): TreeSelectOption[] {
  const tree = buildDeptTree(items);
  return deptToTreeSelect(tree);
}

const columns: DataTableColumns<DeptItem> = [
  {
    type: 'selection',
    width: 50,
    disabled: (row) => isProtectedDept(row),
  },
  { title: '名称', key: 'name', minWidth: 160, ellipsis: { tooltip: true } },
  { title: '负责人', key: 'leader', width: 100, render: (row) => row.leader || '-' },
  { title: '电话', key: 'phone', width: 120, render: (row) => row.phone || '-' },
  { title: '邮箱', key: 'email', width: 160, ellipsis: { tooltip: true }, render: (row) => row.email || '-' },
  { title: '排序', key: 'seq', width: 70, align: 'center', render: (row) => row.seq ?? '-' },
  {
    title: '状态',
    key: 'enabled',
    width: 90,
    align: 'center',
    render: (row) =>
      h(NSwitch, {
        value: isDeptEnabled(row),
        disabled: isProtectedDept(row) || !canAccess('system:dept:edit'),
        onUpdateValue: (val: boolean) => handleChangeEnabled(row, val),
      }),
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
      if (canAccess('system:dept:edit')) {
        actions.push(
          h(
            NButton,
            {
              size: 'small',
              type: 'primary',
              tertiary: true,
              onClick: () => handleEdit(row),
            },
            () => '编辑',
          ),
        );
      }
      if (canAccess('system:dept:del')) {
        actions.push(
          h(
            NButton,
            {
              size: 'small',
              type: 'error',
              tertiary: true,
              disabled: isProtectedDept(row),
              onClick: () => handleDelete(row.id),
            },
            () => '删除',
          ),
        );
      }
      return actions.length > 0 ? h(NSpace, { size: 'small' }, () => actions) : '-';
    },
  },
];

function buildQueryParams() {
  const params: Parameters<typeof getDeptList>[0] = {};
  if (searchName.value.trim()) params!.name = searchName.value.trim();
  if (enabledFilter.value !== null) params!.enabled = enabledFilter.value;
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
  try {
    const { list } = await getDeptList(buildQueryParams());
    data.value = buildDeptTree(list);
  } catch (e: any) {
    console.error('Failed to load depts:', e);
    data.value = [];
    notification.error({
      content: '加载部门失败',
      description: e?.response?.data?.msg || e?.message || '请检查后端服务',
      duration: 4000,
    });
  } finally {
    loading.value = false;
  }
}

async function loadParentDeptOptions(editingId?: number) {
  try {
    const list = editingId
      ? await getDeptSuperior(editingId)
      : await getDepts({ enabled: 1 });
    parentDeptOptions.value = buildParentTreeFromFlat(list);
  } catch (e: any) {
    console.error('Failed to load parent depts:', e);
    parentDeptOptions.value = [];
  }
}

function handleSearch() {
  loadData();
}

function handleReset() {
  searchName.value = '';
  enabledFilter.value = null;
  dateRange.value = null;
  loadData();
}

function resetFormData() {
  formData.value = {
    id: undefined,
    name: '',
    seq: 999,
    isTop: '1',
    pid: undefined,
    enabled: 1,
  };
  parentDeptOptions.value = [];
}

function handleAdd() {
  isEdit.value = false;
  resetFormData();
  loadParentDeptOptions();
  modalApi.setState({ title: '新增部门' }).open();
}

function handleEditSelected() {
  if (selectedRowKeys.value.length !== 1) {
    notification.warning({ content: '请选择一条记录进行编辑', duration: 2000 });
    return;
  }
  const row = findDeptById(data.value, selectedRowKeys.value[0]);
  if (row) handleEdit(row);
}

function findDeptById(items: DeptItem[], id: number): DeptItem | undefined {
  for (const item of items) {
    if (item.id === id) return item;
    if (item.children?.length) {
      const found = findDeptById(item.children, id);
      if (found) return found;
    }
  }
  return undefined;
}

async function handleEdit(row: DeptItem) {
  isEdit.value = true;
  const isTop = isRootDept(row.parentId) ? '1' : '0';
  formData.value = {
    id: row.id,
    name: row.name || '',
    seq: row.seq ?? 999,
    isTop,
    pid: isTop === '0' ? (row.parentId ?? undefined) : undefined,
    enabled: isDeptEnabled(row) ? 1 : 0,
  };
  await loadParentDeptOptions(row.id);
  modalApi.setState({ title: '编辑部门' }).open();
}

function handleIsTopChange(value: '0' | '1') {
  formData.value.isTop = value;
  if (value === '1') {
    formData.value.pid = undefined;
  }
}

async function handleSubmit() {
  if (!formData.value.name?.trim()) {
    notification.warning({ content: '请输入名称', duration: 2000 });
    return;
  }
  if (formData.value.seq === undefined || formData.value.seq === null) {
    notification.warning({ content: '请输入排序', duration: 2000 });
    return;
  }
  if (
    formData.value.isTop === '0' &&
    formData.value.pid &&
    formData.value.id &&
    formData.value.pid === formData.value.id
  ) {
    notification.warning({ content: '上级部门不能为自身', duration: 2000 });
    return;
  }

  const payload: Partial<DeptItem> = {
    id: formData.value.id,
    name: formData.value.name.trim(),
    seq: formData.value.seq,
    enabled: formData.value.enabled,
    parentId: formData.value.isTop === '1' ? null : formData.value.pid ?? null,
  };

  modalApi.lock();
  try {
    if (isEdit.value && formData.value.id) {
      await editDept(payload);
      notification.success({ content: '部门已更新', duration: 2000 });
    } else {
      await addDept(payload);
      notification.success({ content: '部门已创建', duration: 2000 });
    }
    modalApi.close();
    loadData();
  } catch (e: any) {
    notification.error({
      content: '操作失败',
      description: e?.response?.data?.msg || e?.message || '保存失败',
      duration: 3000,
    });
  } finally {
    modalApi.unlock();
  }
}

function handleDelete(id: number) {
  dialog.warning({
    title: '提示',
    content: '确定删除吗？如果存在下级节点则一并删除，此操作不能撤销！',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteDept(id);
        notification.success({ content: '部门已删除', duration: 2000 });
        selectedRowKeys.value = selectedRowKeys.value.filter((key) => key !== id);
        loadData();
      } catch (e: any) {
        notification.error({
          content: '操作失败',
          description: e?.response?.data?.msg || e?.message || '删除失败',
          duration: 3000,
        });
      }
    },
  });
}

function handleBatchDelete() {
  const ids = selectedRowKeys.value.filter((id) => {
    const row = findDeptById(data.value, id);
    return row && !isProtectedDept(row);
  });
  if (!ids.length) {
    notification.warning({ content: '请选择可删除的部门', duration: 2000 });
    return;
  }
  dialog.warning({
    title: '提示',
    content: `确定删除选中的 ${ids.length} 个部门吗？如果存在下级节点则一并删除，此操作不能撤销！`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteDept(ids);
        notification.success({ content: `已删除 ${ids.length} 个部门`, duration: 2000 });
        selectedRowKeys.value = [];
        loadData();
      } catch (e: any) {
        notification.error({
          content: '操作失败',
          description: e?.response?.data?.msg || e?.message || '删除失败',
          duration: 3000,
        });
      }
    },
  });
}

function handleChangeEnabled(row: DeptItem, enabled: boolean) {
  const prev = isDeptEnabled(row);
  const label = enabled ? '启用' : '停用';
  dialog.warning({
    title: '提示',
    content: `此操作将 "${label}" ${row.name} 部门，是否继续？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await editDept({
          id: row.id,
          name: row.name,
          seq: row.seq,
          enabled: enabled ? 1 : 0,
          parentId: row.parentId ?? null,
          leader: row.leader,
          phone: row.phone,
          email: row.email,
        });
        row.enabled = enabled ? 1 : 0;
        notification.success({ content: `${label}成功`, duration: 2000 });
      } catch (e: any) {
        row.enabled = prev ? 1 : 0;
        notification.error({
          content: '操作失败',
          description: e?.response?.data?.msg || e?.message || '更新状态失败',
          duration: 3000,
        });
      }
    },
    onNegativeClick: () => {
      row.enabled = prev ? 1 : 0;
    },
    onClose: () => {
      row.enabled = prev ? 1 : 0;
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
    await exportDeptExcel(params);
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

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="dept-page p-4">
    <NCard size="small" content-class="!p-4">
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <NInput
          v-model:value="searchName"
          placeholder="输入部门名称搜索"
          clearable
          size="small"
          class="!w-52"
          @keyup.enter="handleSearch"
        />
        <NSelect
          v-model:value="enabledFilter"
          :options="ENABLED_FILTER_OPTIONS"
          placeholder="状态"
          clearable
          size="small"
          class="!w-28"
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
        <NButton v-access:code="'system:dept:add'" type="primary" size="small" @click="handleAdd">新增</NButton>
        <NButton
          v-access:code="'system:dept:edit'"
          size="small"
          type="info"
          :disabled="selectedRowKeys.length !== 1"
          @click="handleEditSelected"
        >
          修改
        </NButton>
        <NButton
          v-access:code="'system:dept:del'"
          size="small"
          type="error"
          :disabled="selectedRowKeys.length === 0"
          @click="handleBatchDelete"
        >
          删除
        </NButton>
        <NButton
          v-access:code="'system:dept:export'"
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
        :row-key="(row: DeptItem) => row.id"
        :checked-row-keys="selectedRowKeys"
        default-expand-all
        :scroll-x="1000"
        :max-height="tableMaxHeight"
        striped
        size="small"
        @update:checked-row-keys="handleCheckedRowKeysChange"
      />
    </NCard>

    <Modal class="w-[500px]">
      <NForm :model="formData" label-placement="left" :label-width="80">
        <NFormItem label="名称" required>
          <NInput v-model:value="formData.name" placeholder="请输入名称" />
        </NFormItem>
        <NFormItem label="排序" required>
          <NInputNumber
            v-model:value="formData.seq"
            :min="0"
            :max="999"
            class="w-full"
          />
        </NFormItem>
        <NFormItem label="顶级部门">
          <NRadioGroup :value="formData.isTop" @update:value="handleIsTopChange">
            <NSpace>
              <NRadio value="1">是</NRadio>
              <NRadio value="0">否</NRadio>
            </NSpace>
          </NRadioGroup>
        </NFormItem>
        <NFormItem label="状态" required>
          <NRadioGroup v-model:value="formData.enabled">
            <NSpace>
              <NRadio
                v-for="opt in ENABLED_FORM_OPTIONS"
                :key="opt.value"
                :value="opt.value"
              >
                {{ opt.label }}
              </NRadio>
            </NSpace>
          </NRadioGroup>
        </NFormItem>
        <NFormItem v-if="formData.isTop === '0'" label="上级部门" required>
          <NTreeSelect
            v-model:value="formData.pid"
            :options="parentDeptOptions"
            placeholder="选择上级部门"
            clearable
            default-expand-all
            class="w-full"
          />
        </NFormItem>
      </NForm>
    </Modal>
  </div>
</template>
