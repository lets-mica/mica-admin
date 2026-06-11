<script setup lang="ts">
import type { DataTableColumns, TreeSelectOption } from 'naive-ui';
import type { MenuItem } from '#/api/system/menu';
import { addMenu, deleteMenu, editMenu, exportMenuExcel, getMenuList } from '#/api/system/menu';

import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NSelect,
  NSpace,
  NTag,
  NTreeSelect,
} from 'naive-ui';
import { useAccess } from '@vben/access';
import { useVbenModal } from '@vben-core/popup-ui';
import { IconPicker } from '@vben/common-ui';
import { VbenIcon } from '@vben-core/shadcn-ui';
import { h, onMounted, ref } from 'vue';
import { dialog, notification } from '#/adapter/naive';

defineOptions({ name: 'MenuManagement' });
const { hasAccessByCodes } = useAccess();
const canAccess = (codes: string | string[]) =>
  hasAccessByCodes(Array.isArray(codes) ? codes : [codes]);

const loading = ref(false);
const data = ref<MenuItem[]>([]);
const flatData = ref<MenuItem[]>([]);
const menuTree = ref<TreeSelectOption[]>([]);
const searchText = ref('');

const dialogLoading = ref(false);
const isEdit = ref(false);

const [Modal, modalApi] = useVbenModal({
  onConfirm: handleSubmit,
});

const formData = ref({
  id: undefined as number | undefined,
  parentId: undefined as number | undefined,
  title: '',
  name: '',
  path: '',
  component: '',
  icon: '',
  sort: 0,
  type: 1,
  permission: '',
  status: 0,
  remark: '',
});

const typeOptions = [
  { label: '目录', value: 0 },
  { label: '菜单', value: 1 },
  { label: '按钮', value: 2 },
];

const typeColors: Record<number, 'info' | 'success' | 'warning'> = {
  0: 'info',
  1: 'success',
  2: 'warning',
};

const columns: DataTableColumns<MenuItem> = [
  { title: '菜单名称', key: 'title', width: 200, ellipsis: { tooltip: true } },
  { title: '路由名称', key: 'name', width: 160, ellipsis: { tooltip: true } },
  {
    title: '类型',
    key: 'type',
    width: 80,
    align: 'center',
    render: (row) =>
      h(NTag, { type: typeColors[row.type as number] || 'default', size: 'small', bordered: false }, () =>
        typeOptions.find((o) => o.value === row.type)?.label || '-',
      ),
  },
  {
    title: '图标',
    key: 'icon',
    width: 140,
    align: 'center',
    render: (row) => {
      const icon = row.icon;
      if (!icon) return '-';
      return h('div', { class: 'flex items-center justify-center gap-1' }, [
        h(VbenIcon, { icon, class: 'size-4 flex-shrink-0' }),
        h('span', { class: 'text-xs truncate max-w-[80px]' }, icon),
      ]);
    },
  },
  { title: '路由路径', key: 'path', width: 180, ellipsis: { tooltip: true } },
  { title: '组件路径', key: 'component', width: 200, ellipsis: { tooltip: true } },
  { title: '权限标识', key: 'permission', width: 150, ellipsis: { tooltip: true } },
  { title: '排序', key: 'sort', width: 70, align: 'center' },
  {
    title: '状态',
    key: 'status',
    width: 80,
    align: 'center',
    render: (row) => {
      // 后端 SysMenu.status: 0正常 1停用；0=启用，其他=禁用
      const enabled = (row as any).status === 0 || (row as any).status === undefined;
      return h(NTag, { type: enabled ? 'success' : 'error', size: 'small', bordered: false }, () =>
        enabled ? '启用' : '禁用',
      );
    },
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
    align: 'center',
    render: (row) => {
      const actions: ReturnType<typeof h>[] = [];
      if (canAccess('system:menu:add')) {
        actions.push(
          h(NButton, { size: 'small', type: 'primary', tertiary: true, onClick: () => handleAddChild(row) }, () => '新增'),
        );
      }
      if (canAccess('system:menu:edit')) {
        actions.push(
          h(NButton, { size: 'small', type: 'primary', tertiary: true, onClick: () => handleEdit(row) }, () => '编辑'),
        );
      }
      if (canAccess('system:menu:del')) {
        actions.push(
          h(NButton, { size: 'small', type: 'error', tertiary: true, onClick: () => handleDelete(row.id) }, () => '删除'),
        );
      }
      return actions.length > 0 ? h(NSpace, { size: 'small' }, () => actions) : '-';
    },
  },
];

function isRootMenu(parentId?: number | null) {
  return parentId === undefined || parentId === null || parentId === 0;
}

function buildTree(items: MenuItem[], parentId?: number | null): MenuItem[] {
  return items
    .filter((m) =>
      isRootMenu(parentId) ? isRootMenu(m.parentId) : m.parentId === parentId,
    )
    .map((m) => {
      const children = buildTree(items, m.id);
      return { ...m, children: children.length > 0 ? children : undefined };
    });
}

function buildTreeOptions(items: MenuItem[]): TreeSelectOption[] {
  return items.map((m: any) => ({
    key: m.id,
    label: m.title || m.name,
    children: m.children && m.children.length > 0 ? buildTreeOptions(m.children) : undefined,
  }));
}

async function loadData() {
  loading.value = true;
  try {
    const params: any = {};
    if (searchText.value) params.blurry = searchText.value;

    const result = await getMenuList(params);
    flatData.value = result.list;
    const tree = buildTree(result.list);
    data.value = tree;
    menuTree.value = buildTreeOptions(tree);
  } catch (e: any) {
    console.error('Failed to load menus:', e);
    flatData.value = [];
    data.value = [];
    menuTree.value = [];
    notification.error({
      content: '加载菜单失败',
      description: e?.response?.data?.msg || e?.message || '请检查后端服务',
      duration: 4000,
    });
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  loadData();
}

function handleReset() {
  searchText.value = '';
  loadData();
}

function resetFormData() {
  formData.value = {
    id: undefined,
    parentId: undefined,
    title: '',
    name: '',
    path: '',
    component: '',
    icon: '',
    sort: 0,
    type: 1,
    permission: '',
    status: 0,
    remark: '',
  };
}

function handleAdd() {
  isEdit.value = false;
  resetFormData();
  modalApi.setState({ title: '新增菜单' }).open();
}

function handleAddChild(row: MenuItem) {
  isEdit.value = false;
  resetFormData();
  formData.value.parentId = row.id;
  formData.value.type = 1; // 1=菜单（后端 0=目录 1=菜单 2=按钮）
  modalApi.setState({ title: `新增子菜单 - ${row.title || row.name}` }).open();
}

function handleEdit(row: MenuItem) {
  isEdit.value = true;
  formData.value = {
    id: row.id,
    parentId: row.parentId,
    title: row.title || '',
    name: row.name || '',
    path: row.path || '',
    component: row.component || '',
    icon: row.icon || '',
    sort: (row as any).sort ?? (row as any).seq ?? 0,
    type: row.type ?? 1,
    permission: row.permission || '',
    status: (row as any).status ?? 0,
    remark: (row as any).remark || '',
  };
  modalApi.setState({ title: '编辑菜单' }).open();
}

async function handleSubmit() {
  if (!formData.value.title) {
    notification.warning({ content: '请输入菜单标题', duration: 2000 });
    return;
  }
  if (!formData.value.name) {
    notification.warning({ content: '请输入路由名称', duration: 2000 });
    return;
  }
  dialogLoading.value = true;
  modalApi.lock();
  try {
    if (isEdit.value && formData.value.id) {
      await editMenu(formData.value as any);
    } else {
      await addMenu(formData.value as any);
    }
    notification.success({ content: isEdit.value ? '菜单已更新' : '菜单已创建', duration: 2000 });
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
    content: '确定要删除该菜单吗？删除后其下级菜单也会被删除。',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteMenu(id);
        notification.success({ content: '菜单已删除', duration: 2000 });
        loadData();
      } catch (e: any) {
        console.error('Failed to delete:', e);
        notification.error({ content: '操作失败', description: e.message || '删除失败', duration: 3000 });
      }
    },
  });
}

onMounted(() => loadData());

const exporting = ref(false);

async function handleExport() {
  exporting.value = true;
  try {
    const params: Record<string, unknown> = {};
    if (searchText.value) params.blurry = searchText.value;
    await exportMenuExcel(params);
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
</script>

<template>
  <div class="p-4">
    <NCard :bordered="false" content-class="!p-4">
      <!-- 搜索区 -->
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <NInput
          v-model:value="searchText"
          placeholder="搜索菜单标题或名称"
          clearable
          class="!w-64"
          @keyup.enter="handleSearch"
        />
        <NButton type="primary" size="small" @click="handleSearch">搜索</NButton>
        <NButton size="small" @click="handleReset">重置</NButton>
      </div>

      <!-- 工具栏 -->
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <NButton v-access:code="'system:menu:add'" type="primary" size="small" @click="handleAdd">新增</NButton>
        <NButton
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
        :row-key="(row: MenuItem) => row.id"
        default-expand-all
        striped
        :scroll-x="1440"
      />
    </NCard>

    <Modal class="w-[680px]">
      <NForm :model="formData" label-placement="left" :label-width="90">
        <NFormItem label="上级菜单">
          <NTreeSelect
            v-model:value="formData.parentId"
            :options="menuTree"
            placeholder="选择上级菜单 (不选则为根菜单)"
            clearable
            default-expand-all
          />
        </NFormItem>
        <div class="grid grid-cols-2 gap-x-4">
          <NFormItem label="菜单类型" required>
            <NSelect v-model:value="formData.type" :options="typeOptions" />
          </NFormItem>
          <NFormItem label="菜单标题" required>
            <NInput v-model:value="formData.title" placeholder="请输入菜单标题（显示用）" />
          </NFormItem>
          <NFormItem label="路由名称" required>
            <NInput v-model:value="formData.name" placeholder="请输入路由名称（英文标识）" />
          </NFormItem>
          <NFormItem label="图标">
            <IconPicker v-model="formData.icon" prefix="lucide" :page-size="40" />
          </NFormItem>
          <NFormItem label="排序">
            <NInputNumber v-model:value="formData.sort" :min="0" class="w-full" />
          </NFormItem>
          <NFormItem v-if="formData.type !== 2" label="路由路径">
            <NInput v-model:value="formData.path" placeholder="请输入路由路径" />
          </NFormItem>
          <NFormItem v-if="formData.type === 1" label="组件路径">
            <NInput v-model:value="formData.component" placeholder="请输入组件路径" />
          </NFormItem>
          <NFormItem v-if="formData.type !== 0" label="权限标识">
            <NInput v-model:value="formData.permission" placeholder="请输入权限标识" />
          </NFormItem>
          <NFormItem label="状态">
            <NSelect
              v-model:value="formData.status"
              :options="[
                { label: '启用', value: 0 },
                { label: '停用', value: 1 },
              ]"
            />
          </NFormItem>
        </div>
        <NFormItem label="备注">
          <NInput v-model:value="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </NFormItem>
      </NForm>
    </Modal>
  </div>
</template>

<style>
/* 菜单管理-图标选择器弹框：与编辑弹框同宽（680px），图标放大 */
.side-content.z-popup {
  width: 680px !important;
  max-width: 90vw !important;
}

/* 弹框内容区域使用更宽松的内边距 */
.side-content.z-popup > div {
  padding-left: 16px;
  padding-right: 16px;
}

/* 图标网格：8 列布局，更大图标 */
.side-content.z-popup .grid {
  grid-template-columns: repeat(8, minmax(0, 1fr)) !important;
  max-height: 460px !important;
  gap: 6px !important;
  padding: 0 8px;
}

/* 单个图标按钮：更大点击区域 */
.side-content.z-popup .grid > button {
  height: auto !important;
  padding: 12px 6px !important;
  aspect-ratio: 1 / 1;
}

/* 图标本身：放大到 28px */
.side-content.z-popup .grid > button svg {
  width: 28px !important;
  height: 28px !important;
}
</style>