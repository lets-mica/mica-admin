<script setup lang="ts">
import type { DataTableColumns, TreeOption } from 'naive-ui';
import type { UserItem, RoleItem } from '#/api/system/user';
import type { DeptItem } from '#/api/system/dept';
import {
  addUser,
  deleteUser,
  editUser,
  getAllRole,
  getAllPost,
  getUserList,
  exportUserExcel,
} from '#/api/system/user';
import { getDepts, getDeptTree } from '#/api/system/dept';

import {
  NButton,
  NCard,
  NDataTable,
  NDatePicker,
  NForm,
  NFormItem,
  NInput,
  NPagination,
  NRadio,
  NRadioGroup,
  NSelect,
  NSpace,
  NSwitch,
  NTree,
  NTreeSelect,
} from 'naive-ui';
import { useAccess } from '@vben/access';
import { useVbenModal } from '@vben-core/popup-ui';
import { useUserStore } from '@vben/stores';
import { computed, h, onMounted, reactive, ref, watch } from 'vue';
import { dialog, notification } from '#/adapter/naive';
import { formatDateTime, dayjs } from '#/utils/format-date';
import { isValidEmail, isValidPhone } from '#/utils/validate';

defineOptions({ name: 'UserManagement' });

const GENDER_OPTIONS = [
  { label: '男', value: 0 },
  { label: '女', value: 1 },
];

const ENABLED_FILTER_OPTIONS = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
];

const userStore = useUserStore();
const { hasAccessByCodes } = useAccess();
const currentUserId = computed(() => Number(userStore.userInfo?.id) || 0);
const canAccess = (codes: string | string[]) =>
  hasAccessByCodes(Array.isArray(codes) ? codes : [codes]);

const loading = ref(false);
const data = ref<UserItem[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, total: 0 });
const searchText = ref('');
const enabledFilter = ref<string | null>(null);
const dateRange = ref<[number, number] | null>(null);
const deptKeyword = ref('');
const selectedDept = ref<number | undefined>();
const deptTree = ref<TreeOption[]>([]);
const selectedRowKeys = ref<number[]>([]);

const roles = ref<RoleItem[]>([]);
const posts = ref<{ id: number; name: string }[]>([]);
const roleLevel = ref(3);

const isEdit = ref(false);
const tableMaxHeight = 480;

const [Modal, modalApi] = useVbenModal({
  onConfirm: handleSubmit,
});

const formData = ref({
  id: undefined as number | undefined,
  userName: '',
  nickName: '',
  phone: '',
  email: '',
  deptId: undefined as number | undefined,
  gender: 1,
  enabled: true,
  roleIds: [] as number[],
  postIds: [] as number[],
  remark: '',
});

function deptToTreeOption(items: DeptItem[]): TreeOption[] {
  return items.map((d) => ({
    key: d.id,
    label: d.name,
    children: d.children?.length ? deptToTreeOption(d.children) : undefined,
  }));
}

function isCurrentUser(row: UserItem) {
  return Number(row.id) === currentUserId.value;
}

function isRoleDisabled(role: RoleItem) {
  if (roleLevel.value === 1) return false;
  return (role.level ?? 99) <= roleLevel.value;
}

function roleOptions() {
  return roles.value.map((r) => ({
    label: r.name,
    value: r.id,
    disabled: isRoleDisabled(r),
  }));
}

const columns: DataTableColumns<UserItem> = [
  {
    type: 'selection',
    width: 50,
    disabled: (row) => isCurrentUser(row),
  },
  { title: '用户名', key: 'userName', width: 120, ellipsis: { tooltip: true } },
  { title: '昵称', key: 'nickName', width: 120, ellipsis: { tooltip: true } },
  {
    title: '性别',
    key: 'gender',
    width: 70,
    align: 'center',
    render: (row) => GENDER_OPTIONS.find((o) => o.value === row.gender)?.label || '未知',
  },
  { title: '电话', key: 'phone', width: 120, ellipsis: { tooltip: true } },
  { title: '邮箱', key: 'email', width: 180, ellipsis: { tooltip: true } },
  { title: '部门', key: 'dept', width: 120, render: (row) => row.dept?.name || '-' },
  {
    title: '状态',
    key: 'enabled',
    width: 90,
    align: 'center',
    render: (row) =>
      h(NSwitch, {
        value: row.enabled !== false,
        disabled: isCurrentUser(row) || !canAccess('system:user:edit'),
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
    width: 140,
    fixed: 'right',
    align: 'center',
    render: (row) => {
      const actions: ReturnType<typeof h>[] = [];
      if (canAccess('system:user:edit')) {
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
      if (canAccess('system:user:del')) {
        actions.push(
          h(
            NButton,
            {
              size: 'small',
              type: 'error',
              tertiary: true,
              disabled: isCurrentUser(row),
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
  const params: Parameters<typeof getUserList>[0] = {
    page: pagination.page,
    size: pagination.pageSize,
  };
  if (searchText.value) params!.blurry = searchText.value;
  if (selectedDept.value) params!.deptId = selectedDept.value;
  if (enabledFilter.value !== null) params!.enabled = enabledFilter.value === '1';
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
    const result = await getUserList(buildQueryParams());
    data.value = result.list;
    pagination.total = result.total;
  } catch (e: any) {
    console.error('Failed to load users:', e);
    data.value = [];
    pagination.total = 0;
    notification.error({
      content: '加载用户失败',
      description: e?.response?.data?.msg || e?.message || '请检查后端服务',
      duration: 4000,
    });
  } finally {
    loading.value = false;
  }
}

async function loadDeptTree() {
  try {
    const result = await getDeptTree();
    deptTree.value = deptToTreeOption(result);
  } catch (e: any) {
    console.error('Failed to load dept tree:', e);
    deptTree.value = [];
  }
}

async function loadRolesAndPosts() {
  try {
    const [roleResult, postResult] = await Promise.all([getAllRole(), getAllPost()]);
    roles.value = roleResult || [];
    posts.value = (postResult || []).map((p: { id: number; name: string }) => ({
      id: p.id,
      name: p.name,
    }));
    const levels = roles.value
      .filter((r) => formData.value.roleIds.includes(r.id))
      .map((r) => r.level ?? 99);
    if (levels.length) {
      roleLevel.value = Math.min(...levels);
    }
  } catch (e: any) {
    console.error('Failed to load roles and posts:', e);
    roles.value = [];
    posts.value = [];
  }
}

function handleSearch() {
  pagination.page = 1;
  loadData();
}

function handleReset() {
  searchText.value = '';
  enabledFilter.value = null;
  dateRange.value = null;
  selectedDept.value = undefined;
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

function handleDeptSelect(keys: Array<string | number>) {
  selectedDept.value = keys.length ? Number(keys[0]) : undefined;
  pagination.page = 1;
  loadData();
}

function resetFormData() {
  formData.value = {
    id: undefined,
    userName: '',
    nickName: '',
    phone: '',
    email: '',
    deptId: undefined,
    gender: 1,
    enabled: true,
    roleIds: [],
    postIds: [],
    remark: '',
  };
}

function handleAdd() {
  isEdit.value = false;
  resetFormData();
  modalApi.setState({ title: '新增用户' }).open();
}

function handleEditSelected() {
  if (selectedRowKeys.value.length !== 1) {
    notification.warning({ content: '请选择一条记录进行编辑', duration: 2000 });
    return;
  }
  const row = data.value.find((item) => item.id === selectedRowKeys.value[0]);
  if (row) handleEdit(row);
}

function handleEdit(row: UserItem) {
  isEdit.value = true;
  formData.value = {
    id: row.id,
    userName: row.userName || '',
    nickName: row.nickName || '',
    phone: row.phone || '',
    email: row.email || '',
    deptId: row.dept?.id ?? row.deptId,
    gender: row.gender ?? 1,
    enabled: row.enabled !== false,
    roleIds: row.roles?.map((r) => r.id) || [],
    postIds: row.posts?.map((p) => p.id) || [],
    remark: row.remark || '',
  };
  modalApi.setState({ title: '编辑用户' }).open();
}

function validateForm() {
  const f = formData.value;
  if (!f.userName) {
    notification.warning({ content: '请输入用户名', duration: 2000 });
    return false;
  }
  if (f.userName.length < 6 || f.userName.length > 12) {
    notification.warning({ content: '用户名长度需在 6-12 个字符', duration: 2000 });
    return false;
  }
  if (!f.nickName) {
    notification.warning({ content: '请输入用户昵称', duration: 2000 });
    return false;
  }
  if (f.nickName.length < 4 || f.nickName.length > 12) {
    notification.warning({ content: '昵称长度需在 4-12 个字符', duration: 2000 });
    return false;
  }
  if (!f.email || !isValidEmail(f.email)) {
    notification.warning({ content: '请输入正确的邮箱地址', duration: 2000 });
    return false;
  }
  if (!f.phone || !isValidPhone(String(f.phone))) {
    notification.warning({ content: '请输入正确的 11 位手机号码', duration: 2000 });
    return false;
  }
  if (!f.deptId) {
    notification.warning({ content: '部门不能为空', duration: 2000 });
    return false;
  }
  if (!f.postIds.length) {
    notification.warning({ content: '岗位不能为空', duration: 2000 });
    return false;
  }
  if (!f.roleIds.length) {
    notification.warning({ content: '角色不能为空', duration: 2000 });
    return false;
  }
  return true;
}

async function handleSubmit() {
  if (!validateForm()) return;

  modalApi.lock();
  try {
    const { id, userName, nickName, phone, email, deptId, gender, enabled, roleIds, postIds, remark } =
      formData.value;
    const payload = {
      id,
      userName,
      nickName,
      phone: String(phone),
      email,
      deptId,
      gender,
      enabled,
      roleIds,
      postIds,
      remark: remark || '',
    };
    if (isEdit.value) {
      await editUser(payload as any);
      notification.success({ content: '用户已更新', duration: 2000 });
    } else {
      await addUser(payload as any);
      notification.success({ content: '新增成功，默认密码：123456', duration: 3000 });
    }
    modalApi.close();
    loadData();
  } catch (e: any) {
    console.error('Failed to submit:', e);
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
  if (id === currentUserId.value) return;
  dialog.warning({
    title: '提示',
    content: '确定要删除该用户吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteUser([id]);
        notification.success({ content: '用户已删除', duration: 2000 });
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
  const ids = selectedRowKeys.value.filter((id) => id !== currentUserId.value);
  if (!ids.length) {
    notification.warning({ content: '请选择要删除的用户', duration: 2000 });
    return;
  }
  dialog.warning({
    title: '提示',
    content: `确定要删除选中的 ${ids.length} 个用户吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteUser(ids);
        notification.success({ content: `已删除 ${ids.length} 个用户`, duration: 2000 });
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

function handleChangeEnabled(row: UserItem, enabled: boolean) {
  const prev = row.enabled !== false;
  const label = enabled ? '启用' : '停用';
  dialog.warning({
    title: '提示',
    content: `此操作将 "${label}" ${row.userName}，是否继续？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await editUser({
          id: row.id,
          userName: row.userName,
          nickName: row.nickName,
          email: row.email,
          phone: row.phone,
          deptId: row.dept?.id ?? row.deptId,
          gender: row.gender,
          enabled,
          roleIds: row.roles?.map((r) => r.id) || [],
          postIds: row.posts?.map((p) => p.id) || [],
          remark: row.remark || '',
        } as any);
        row.enabled = enabled;
        notification.success({ content: `${label}成功`, duration: 2000 });
      } catch (e: any) {
        row.enabled = prev;
        notification.error({
          content: '操作失败',
          description: e?.response?.data?.msg || e?.message || '更新状态失败',
          duration: 3000,
        });
      }
    },
    onNegativeClick: () => {
      row.enabled = prev;
    },
    onClose: () => {
      row.enabled = prev;
    },
  });
}

const exporting = ref(false);

function handleCheckedRowKeysChange(keys: (string | number)[]) {
  selectedRowKeys.value = keys.map(Number);
}

async function handleExport() {
  exporting.value = true;
  try {
    const params = buildQueryParams() as Record<string, unknown>;
    delete params.page;
    delete params.size;
    await exportUserExcel(params);
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

watch(deptKeyword, async (keyword) => {
  if (!keyword?.trim()) {
    await loadDeptTree();
    return;
  }
  try {
    const list = await getDepts({ name: keyword.trim(), sort: 'id,desc' });
    deptTree.value = list.map((d: DeptItem) => ({ key: d.id, label: d.name }));
  } catch {
    // 保持当前树
  }
});

onMounted(() => {
  loadData();
  loadDeptTree();
  loadRolesAndPosts();
});
</script>

<template>
  <div class="user-page p-4">
    <div class="user-page__layout">
      <!-- 左侧部门树（对齐老版 el-col :lg="4"） -->
      <NCard class="user-page__dept" size="small" content-class="!p-3">
        <NInput
          v-model:value="deptKeyword"
          placeholder="输入部门名称搜索"
          size="small"
          clearable
          class="mb-3"
        />
        <div class="dept-tree-wrapper">
          <NTree
            :data="deptTree"
            :default-expand-all="true"
            block-line
            block-node
            selectable
            :selected-keys="selectedDept ? [selectedDept] : []"
            @update:selected-keys="handleDeptSelect"
          />
        </div>
      </NCard>

      <!-- 右侧用户列表（对齐老版 el-col :lg="20"） -->
      <NCard class="user-page__main" size="small" content-class="!p-4 user-page__main-body">
        <!-- 搜索区 -->
        <div class="user-page__search mb-3 flex flex-wrap items-center gap-2">
          <NInput
            v-model:value="searchText"
            placeholder="输入名称或者邮箱搜索"
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
          <NSelect
            v-model:value="enabledFilter"
            :options="ENABLED_FILTER_OPTIONS"
            placeholder="状态"
            clearable
            size="small"
            class="!w-28"
          />
          <NButton type="primary" size="small" @click="handleSearch">搜索</NButton>
          <NButton size="small" @click="handleReset">重置</NButton>
        </div>

        <!-- 工具栏（对齐老版 crudOperation） -->
        <div class="user-page__toolbar mb-3 flex flex-wrap items-center gap-2">
          <NButton v-access:code="'system:user:add'" type="primary" size="small" @click="handleAdd">新增</NButton>
          <NButton
            v-access:code="'system:user:edit'"
            size="small"
            type="info"
            :disabled="selectedRowKeys.length !== 1"
            @click="handleEditSelected"
          >
            修改
          </NButton>
          <NButton
            v-access:code="'system:user:del'"
            size="small"
            type="error"
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
          >
            删除
          </NButton>
          <NButton
            v-access:code="'system:user:export'"
            size="small"
            type="warning"
            :loading="exporting"
            @click="handleExport"
          >
            导出
          </NButton>
        </div>

        <!-- 表格 -->
        <NDataTable
          class="user-page__table"
          :loading="loading"
          :columns="columns"
          :data="data"
          :row-key="(row: UserItem) => row.id"
          :checked-row-keys="selectedRowKeys"
          :scroll-x="1100"
          :max-height="tableMaxHeight"
          striped
          size="small"
          @update:checked-row-keys="handleCheckedRowKeysChange"
        />

        <div class="user-page__pagination mt-3 flex items-center justify-between">
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
    </div>

    <!-- Modal 必须放在布局容器外，避免参与 flex/grid 占位 -->
    <Modal class="w-[680px]">
      <NForm :model="formData" label-placement="left" :label-width="72">
        <div class="grid grid-cols-2 gap-x-4">
          <NFormItem label="用户名" required>
            <NInput
              v-model:value="formData.userName"
              :disabled="isEdit"
              placeholder="6-12 个字符"
            />
          </NFormItem>
          <NFormItem label="电话" required>
            <NInput v-model:value="formData.phone" placeholder="11 位手机号" />
          </NFormItem>
          <NFormItem label="昵称" required>
            <NInput v-model:value="formData.nickName" placeholder="4-12 个字符" />
          </NFormItem>
          <NFormItem label="邮箱" required>
            <NInput v-model:value="formData.email" placeholder="请输入邮箱" />
          </NFormItem>
          <NFormItem label="部门" required>
            <NTreeSelect
              v-model:value="formData.deptId"
              :options="deptTree"
              placeholder="选择部门"
              clearable
              default-expand-all
            />
          </NFormItem>
          <NFormItem label="岗位" required>
            <NSelect
              v-model:value="formData.postIds"
              multiple
              :options="posts.map((p) => ({ label: p.name, value: p.id }))"
              placeholder="请选择"
            />
          </NFormItem>
          <NFormItem label="性别">
            <NRadioGroup v-model:value="formData.gender">
              <NSpace>
                <NRadio v-for="opt in GENDER_OPTIONS" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </NRadio>
              </NSpace>
            </NRadioGroup>
          </NFormItem>
          <NFormItem label="状态">
            <NRadioGroup
              v-model:value="formData.enabled"
              :disabled="isEdit && formData.id === currentUserId"
            >
              <NSpace>
                <NRadio :value="true">启用</NRadio>
                <NRadio :value="false">停用</NRadio>
              </NSpace>
            </NRadioGroup>
          </NFormItem>
          <NFormItem label="角色" required class="col-span-2">
            <NSelect
              v-model:value="formData.roleIds"
              multiple
              :options="roleOptions()"
              placeholder="请选择"
              class="w-full"
            />
          </NFormItem>
        </div>
        <NFormItem label="备注">
          <NInput
            v-model:value="formData.remark"
            type="textarea"
            :rows="2"
            placeholder="备注"
          />
        </NFormItem>
      </NForm>
    </Modal>
  </div>
</template>

<style scoped>
.user-page__layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.user-page__dept {
  min-width: 0;
}

.user-page__main {
  min-width: 0;
}

.user-page__main-body {
  display: flex;
  flex-direction: column;
}

.dept-tree-wrapper {
  max-height: calc(100vh - 240px);
  overflow-y: auto;
}

@media (max-width: 992px) {
  .user-page__layout {
    grid-template-columns: 1fr;
  }
}
</style>
