<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';
import type { MessageItem } from '#/api/system/message';
import type { DictItem } from '#/api/system/dict';
import type { UserItem } from '#/api/system/user';
import type { DeptItem } from '#/api/system/dept';
import { addMessage, deleteMessage, editMessage, exportMessageExcel, getMessageList, publishMessage } from '#/api/system/message';
import { getDictItems } from '#/api/system/dict';
import { getUserList } from '#/api/system/user';
import { getDeptTree } from '#/api/system/dept';

import {
  NButton,
  NCard,
  NCheckbox,
  NDataTable,
  NDatePicker,
  NEmpty,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NPagination,
  NScrollbar,
  NSelect,
  NSpace,
  NSwitch,
  NTabPane,
  NTabs,
  NTag,
  NTree,
} from 'naive-ui';
import { useVbenModal } from '@vben-core/popup-ui';
import { computed, h, onMounted, reactive, ref } from 'vue';
import { dialog, notification } from '#/adapter/naive';
import { dayjs, formatDateTime } from '#/utils/format-date';

defineOptions({ name: 'MessageManagement' });

const loading = ref(false);
const data = ref<MessageItem[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, total: 0 });
const searchText = ref('');
const dateRange = ref<[number, number] | null>(null);
const selectedRowKeys = ref<number[]>([]);

const dialogLoading = ref(false);
const isEdit = ref(false);

const [Modal, modalApi] = useVbenModal({
  onConfirm: handleSubmit,
});

const [PublishModal, publishModalApi] = useVbenModal({
  onConfirm: handlePublishConfirm,
});

const formData = ref({
  id: undefined as number | undefined,
  category: '',
  title: '',
  content: '',
  sendFlag: '0',
  seq: 0,
  enabled: true,
  remark: '',
});

// 字典加载
const categoryOptions = ref<{ label: string; value: string }[]>([]);
const categoryMap = ref<Record<string, string>>({});

async function loadCategoryDict() {
  try {
    const items: DictItem[] = await getDictItems('sys_message_category');
    categoryOptions.value = items.map((item) => ({
      label: item.label,
      value: item.value,
    }));
    const map: Record<string, string> = {};
    for (const item of items) {
      map[item.value] = item.label;
    }
    categoryMap.value = map;
  } catch {
    categoryOptions.value = [];
  }
}

const columns: DataTableColumns<MessageItem> = [
  { type: 'selection', width: 50 },
  {
    title: '分类',
    key: 'category',
    width: 120,
    render: (row) =>
      h(NTag, { size: 'small', bordered: false }, () =>
        categoryMap.value[(row as any).category] || (row as any).category || '-',
      ),
  },
  { title: '标题', key: 'title', width: 220, ellipsis: { tooltip: true } },
  {
    title: '推送',
    key: 'sendFlag',
    width: 80,
    align: 'center',
    render: (row) =>
      h(NTag, { type: (row as any).sendFlag === '1' ? 'success' : 'default', size: 'small', bordered: false }, () =>
        (row as any).sendFlag === '1' ? '是' : '否',
      ),
  },
  { title: '排序', key: 'seq', width: 80, align: 'center' },
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
    width: 180,
    fixed: 'right',
    align: 'center',
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        h(NButton, { size: 'small', type: 'primary', tertiary: true, onClick: () => handleEdit(row) }, () => '编辑'),
        h(NButton, { size: 'small', type: 'warning', tertiary: true, onClick: () => handlePublish(row) }, () => '发布'),
        h(NButton, { size: 'small', type: 'error', tertiary: true, onClick: () => handleDelete(row.id) }, () => '删除'),
      ]),
  },
];

async function loadData() {
  loading.value = true;
  try {
    const params: any = { page: pagination.page, size: pagination.pageSize };
    if (searchText.value) params.title = searchText.value;
    if (dateRange.value) {
      const [start, end] = dateRange.value;
      params.createTime = [
        dayjs(start).startOf('day').format('YYYY-MM-DD HH:mm:ss'),
        dayjs(end).endOf('day').format('YYYY-MM-DD HH:mm:ss'),
      ];
    }

    const result = await getMessageList(params);
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
  formData.value = { id: undefined, category: '', title: '', content: '', sendFlag: '0', seq: 0, enabled: true, remark: '' };
}

function handleAdd() {
  isEdit.value = false;
  resetFormData();
  modalApi.setState({ title: '新增消息' }).open();
}

function handleEditSelected() {
  if (selectedRowKeys.value.length !== 1) {
    notification.warning({ content: '请选择一条记录进行编辑', duration: 2000 });
    return;
  }
  const row = data.value.find((item) => item.id === selectedRowKeys.value[0]);
  if (row) handleEdit(row);
}

function handleEdit(row: MessageItem) {
  isEdit.value = true;
  formData.value = {
    id: row.id,
    category: (row as any).category || '',
    title: (row as any).title || '',
    content: (row as any).content || '',
    sendFlag: (row as any).sendFlag || '0',
    seq: (row as any).seq || 0,
    enabled: (row as any).enabled !== false,
    remark: (row as any).remark || '',
  };
  modalApi.setState({ title: '编辑消息' }).open();
}

async function handleSubmit() {
  if (!formData.value.title) {
    notification.warning({ content: '请输入标题', duration: 2000 });
    return;
  }
  dialogLoading.value = true;
  modalApi.lock();
  try {
    if (isEdit.value && formData.value.id) {
      await editMessage(formData.value as any);
    } else {
      await addMessage(formData.value as any);
    }
    notification.success({ content: isEdit.value ? '消息已更新' : '消息已创建', duration: 2000 });
    modalApi.close();
    loadData();
  } catch (e: any) {
    console.error('Failed to submit:', e);
  } finally {
    dialogLoading.value = false;
    modalApi.unlock();
  }
}

// ---- 发布弹窗：选择用户/组织 ----
const publishMessageData = ref<MessageItem | null>(null);
const publishUsers = ref<UserItem[]>([]);
const publishSelectedUserIds = ref<number[]>([]);
const publishUserSearch = ref('');
const publishDepts = ref<DeptItem[]>([]);
const publishSelectedDeptIds = ref<number[]>([]);
const publishDeptSearch = ref('');
const publishLoading = ref(false);
const publishActiveTab = ref('user');

async function loadPublishData() {
  publishLoading.value = true;
  try {
    const [userResult, deptTree] = await Promise.all([
      getUserList({ page: 1, size: 999, enabled: true }),
      getDeptTree({ enabled: 1 }),
    ]);
    publishUsers.value = userResult.list;
    publishDepts.value = deptTree;
  } catch {
    publishUsers.value = [];
    publishDepts.value = [];
  } finally {
    publishLoading.value = false;
  }
}

const filteredPublishUsers = computed(() => {
  const keyword = publishUserSearch.value.trim().toLowerCase();
  if (!keyword) return publishUsers.value;
  return publishUsers.value.filter(
    (u) =>
      u.nickName?.toLowerCase().includes(keyword) ||
      u.userName?.toLowerCase().includes(keyword),
  );
});

const isAllPublishSelected = computed(() => {
  const filtered = filteredPublishUsers.value;
  if (filtered.length === 0) return false;
  return filtered.every((u) => publishSelectedUserIds.value.includes(u.id));
});

function togglePublishSelectAll() {
  const filteredIds = filteredPublishUsers.value.map((u) => u.id);
  if (isAllPublishSelected.value) {
    publishSelectedUserIds.value = publishSelectedUserIds.value.filter(
      (id) => !filteredIds.includes(id),
    );
  } else {
    const existing = new Set(publishSelectedUserIds.value);
    filteredIds.forEach((id) => existing.add(id));
    publishSelectedUserIds.value = [...existing];
  }
}

function togglePublishUser(userId: number) {
  const idx = publishSelectedUserIds.value.indexOf(userId);
  if (idx >= 0) {
    publishSelectedUserIds.value.splice(idx, 1);
  } else {
    publishSelectedUserIds.value.push(userId);
  }
}

// 部门树相关
const flattenedDepts = computed(() => {
  const result: DeptItem[] = [];
  function flatten(items: DeptItem[]) {
    for (const item of items) {
      result.push(item);
      if (item.children?.length) flatten(item.children);
    }
  }
  flatten(publishDepts.value);
  return result;
});

const filteredDeptTree = computed(() => {
  const keyword = publishDeptSearch.value.trim().toLowerCase();
  if (!keyword) return publishDepts.value;
  // 收集匹配的部门 ID
  const matchedIds = new Set<number>();
  for (const dept of flattenedDepts.value) {
    if (dept.name?.toLowerCase().includes(keyword)) {
      matchedIds.add(dept.id);
    }
  }
  // 过滤树：保留匹配的节点及其祖先
  function filterTree(items: DeptItem[]): DeptItem[] {
    return items
      .map((item) => {
        const filteredChildren = item.children ? filterTree(item.children) : [];
        const selfMatch = matchedIds.has(item.id);
        if (selfMatch || filteredChildren.length > 0) {
          return { ...item, children: filteredChildren.length > 0 ? filteredChildren : undefined };
        }
        return null;
      })
      .filter(Boolean) as DeptItem[];
  }
  return filterTree(publishDepts.value);
});

function handleDeptCheck(keys: number[]) {
  publishSelectedDeptIds.value = keys;
}

const publishSummary = computed(() => {
  const u = publishSelectedUserIds.value.length;
  const d = publishSelectedDeptIds.value.length;
  if (u === 0 && d === 0) return '未选择，将推送给所有用户';
  const parts: string[] = [];
  if (u > 0) parts.push(`${u} 位用户`);
  if (d > 0) parts.push(`${d} 个组织`);
  return `已选 ${parts.join('、')}`;
});

function handlePublish(row: MessageItem) {
  publishMessageData.value = row;
  publishSelectedUserIds.value = [];
  publishSelectedDeptIds.value = [];
  publishUserSearch.value = '';
  publishDeptSearch.value = '';
  publishActiveTab.value = 'user';
  loadPublishData();
  publishModalApi.setState({ title: `发布消息 - ${row.title}` }).open();
}

async function handlePublishConfirm() {
  if (!publishMessageData.value) return;
  publishLoading.value = true;
  publishModalApi.lock();
  try {
    const userIds = publishSelectedUserIds.value.length > 0 ? publishSelectedUserIds.value : undefined;
    const deptIds = publishSelectedDeptIds.value.length > 0 ? publishSelectedDeptIds.value : undefined;
    await publishMessage(publishMessageData.value.id, userIds, deptIds);
    const msg = (!userIds && !deptIds)
      ? '消息已发布，推送给所有用户'
      : `消息已发布${publishSummary.value.replace('已选 ', '，推送给 ')}`;
    notification.success({ content: msg, duration: 2000 });
    publishModalApi.close();
    loadData();
  } catch (e: any) {
    console.error('Failed to publish:', e);
  } finally {
    publishLoading.value = false;
    publishModalApi.unlock();
  }
}

function handleDelete(id: number) {
  dialog.warning({
    title: '提示',
    content: '确定要删除该消息吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteMessage([id]);
        notification.success({ content: '消息已删除', duration: 2000 });
        loadData();
      } catch (e: any) {
        console.error('Failed to delete:', e);
      }
    },
  });
}

function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    notification.warning({ content: '请选择要删除的消息', duration: 2000 });
    return;
  }
  dialog.warning({
    title: '提示',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条消息吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteMessage(selectedRowKeys.value);
        notification.success({ content: `已删除 ${selectedRowKeys.value.length} 条消息`, duration: 2000 });
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
    if (searchText.value) params.title = searchText.value;
    if (dateRange.value) {
      const [start, end] = dateRange.value;
      params.createTime = [
        dayjs(start).startOf('day').format('YYYY-MM-DD HH:mm:ss'),
        dayjs(end).endOf('day').format('YYYY-MM-DD HH:mm:ss'),
      ];
    }
    await exportMessageExcel(params);
    notification.success({ content: '导出成功', duration: 2000 });
  } catch (e: any) {
    console.error('Failed to export:', e);
  } finally {
    exporting.value = false;
  }
}

onMounted(() => {
  loadCategoryDict();
  loadData();
});
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
          class="!w-64"
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
        <NButton type="primary" size="small" @click="handleAdd">新增</NButton>
        <NButton
          size="small"
          type="info"
          :disabled="selectedRowKeys.length !== 1"
          @click="handleEditSelected"
        >
          修改
        </NButton>
        <NButton
          size="small"
          type="error"
          :disabled="selectedRowKeys.length === 0"
          @click="handleBatchDelete"
        >
          删除
        </NButton>
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
        :row-key="(row: MessageItem) => row.id"
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
        <NFormItem label="分类">
          <NSelect v-model:value="formData.category" :options="categoryOptions" clearable placeholder="请选择分类" />
        </NFormItem>
        <NFormItem label="标题" required>
          <NInput v-model:value="formData.title" placeholder="请输入标题" />
        </NFormItem>
        <NFormItem label="内容">
          <NInput v-model:value="formData.content" type="textarea" :rows="6" placeholder="请输入内容" />
        </NFormItem>
        <NFormItem label="推送">
          <NSwitch
            :value="formData.sendFlag === '1'"
            @update:value="(val: boolean) => formData.sendFlag = val ? '1' : '0'"
          >
            <template #checked>是</template>
            <template #unchecked>否</template>
          </NSwitch>
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="formData.seq" :min="0" placeholder="排序值" />
        </NFormItem>
        <NFormItem label="状态">
          <NSwitch v-model:value="formData.enabled">
            <template #checked>发布</template>
            <template #unchecked>草稿</template>
          </NSwitch>
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="formData.remark" placeholder="请输入备注" />
        </NFormItem>
      </NForm>
    </Modal>

    <PublishModal class="w-[560px]">
      <div class="px-2">
        <div class="mb-2 flex items-center justify-between text-xs text-gray-400">
          <span>{{ publishSummary }}</span>
        </div>
        <NTabs v-model:value="publishActiveTab" type="line" size="small">
          <!-- 用户 Tab -->
          <NTabPane name="user" tab="指定用户">
            <div class="mb-2 flex items-center gap-2">
              <NInput
                v-model:value="publishUserSearch"
                placeholder="搜索用户（姓名/账号）"
                clearable
                size="small"
                class="!flex-1"
              />
              <NButton size="small" :type="isAllPublishSelected ? 'default' : 'primary'" ghost @click="togglePublishSelectAll">
                {{ isAllPublishSelected ? '取消全选' : '全选' }}
              </NButton>
            </div>
            <NScrollbar style="max-height: 300px">
              <div v-if="publishLoading" class="flex-center py-8 text-gray-400">加载中...</div>
              <div v-else-if="filteredPublishUsers.length === 0" class="py-8">
                <NEmpty description="暂无用户" />
              </div>
              <div v-else class="flex flex-col gap-1">
                <div
                  v-for="user in filteredPublishUsers"
                  :key="user.id"
                  class="flex cursor-pointer items-center gap-3 rounded px-3 py-2 transition-colors hover:bg-gray-100 dark:hover:bg-gray-800"
                  @click="togglePublishUser(user.id)"
                >
                  <NCheckbox
                    :checked="publishSelectedUserIds.includes(user.id)"
                    @click.stop="togglePublishUser(user.id)"
                  />
                  <span class="font-medium">{{ user.nickName || user.userName }}</span>
                  <span class="text-xs text-gray-400">{{ user.userName }}</span>
                </div>
              </div>
            </NScrollbar>
          </NTabPane>

          <!-- 组织 Tab -->
          <NTabPane name="dept" tab="指定组织">
            <div class="mb-2">
              <NInput
                v-model:value="publishDeptSearch"
                placeholder="搜索组织名称"
                clearable
                size="small"
              />
            </div>
            <NScrollbar style="max-height: 300px">
              <div v-if="publishLoading" class="flex-center py-8 text-gray-400">加载中...</div>
              <div v-else-if="filteredDeptTree.length === 0" class="py-8">
                <NEmpty description="暂无组织" />
              </div>
              <NTree
                v-else
                block-line
                checkable
                :cascade="false"
                :checked-keys="publishSelectedDeptIds"
                :data="filteredDeptTree"
                key-field="id"
                label-field="name"
                children-field="children"
                selectable
                @update:checked-keys="handleDeptCheck"
              />
            </NScrollbar>
          </NTabPane>
        </NTabs>
      </div>
    </PublishModal>
  </div>
</template>
