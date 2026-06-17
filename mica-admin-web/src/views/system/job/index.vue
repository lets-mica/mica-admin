<script setup lang="ts">
import type { DataTableColumns, SelectOption } from 'naive-ui';
import type { JobForm, JobQuery, RunOnceForm, SysJob } from '#/api/system/job';
import {
  addJob,
  checkRegistered,
  deleteJob,
  exportJobExcel,
  getJob,
  listJob,
  refreshJob,
  runOnceWithParams,
  startJob,
  stopJob,
  updateJob,
} from '#/api/system/job';

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
  NSwitch,
  NTag,
  NText,
} from 'naive-ui';
import { useAccess } from '@vben/access';
import { useVbenModal } from '@vben-core/popup-ui';
import { h, onMounted, reactive, ref, watch } from 'vue';
import { dialog, notification } from '#/adapter/naive';
import { formatDateTime } from '#/utils/format-date';

defineOptions({ name: 'JobManagement' });

const { hasAccessByCodes } = useAccess();
const canAccess = (codes: string | string[]) =>
  hasAccessByCodes(Array.isArray(codes) ? codes : [codes]);

// ---------- 列表状态 ----------
const loading = ref(false);
const data = ref<SysJob[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, total: 0 });
const searchForm = reactive<JobQuery & { enabledFilter: string }>({
  blurry: '',
  enabled: undefined,
  enabledFilter: '',
});
const selectedRowKeys = ref<number[]>([]);

const registeredKeys = ref<Set<string>>(new Set());

const statusOptions: SelectOption[] = [
  { label: '已启用', value: 'true' },
  { label: '已停用', value: 'false' },
];

// ---------- 新增 / 编辑 Modal ----------
const [FormModal, formModalApi] = useVbenModal({
  onConfirm: handleSubmit,
});
const isEdit = ref(false);
const formData = ref<JobForm>({
  id: undefined,
  jobKey: '',
  jobName: '',
  cronExpression: '',
  enabled: true,
  paramSchema: '',
  description: '',
});
const formSubmitting = ref(false);

// ---------- 立即执行（带参）Modal ----------
const [RunOnceModal, runOnceModalApi] = useVbenModal({
  onConfirm: handleRunOnceWithParamsConfirm,
});
const runOnceForm = ref<RunOnceForm>({ jobKey: '', params: undefined });
const runOnceSubmitting = ref(false);

/**
 * 任务参数类型（与后端 net.dreamlu.mica.admin.framework.job.core.JobParamType 保持一致）
 */
type JobParamType =
  | 'STRING'
  | 'INTEGER'
  | 'LONG'
  | 'BOOLEAN'
  | 'DOUBLE'
  | 'DATE'
  | 'DATETIME';

const VALID_PARAM_TYPES: JobParamType[] = [
  'STRING',
  'INTEGER',
  'LONG',
  'BOOLEAN',
  'DOUBLE',
  'DATE',
  'DATETIME',
];

interface ParamField {
  key: string;
  type: JobParamType;
}

function parseParamType(raw: string): JobParamType {
  const upper = (raw || '').toUpperCase();
  return VALID_PARAM_TYPES.includes(upper as JobParamType)
    ? (upper as JobParamType)
    : 'STRING';
}

/**
 * 解析 param_schema（形如 {"bizDate":"DATE","force":"BOOLEAN"}）
 * 解析失败 / 非对象均返回空数组
 */
function parseParamSchema(schema?: string): ParamField[] {
  if (!schema || !schema.trim()) return [];
  try {
    const obj = JSON.parse(schema);
    if (obj === null || typeof obj !== 'object' || Array.isArray(obj)) return [];
    return Object.entries(obj).map(([key, value]) => ({
      key,
      type: parseParamType(String(value)),
    }));
  } catch {
    return [];
  }
}

const paramFields = ref<ParamField[]>([]);
const runOnceFormValues = ref<Record<string, unknown>>({});

// ---------- 表格列 ----------
const columns: DataTableColumns<SysJob> = [
  { type: 'selection', width: 50 },
  { title: '任务Key', key: 'jobKey', width: 160, ellipsis: { tooltip: true } },
  { title: '任务名称', key: 'jobName', width: 180, ellipsis: { tooltip: true } },
  {
    title: '已注册',
    key: 'registered',
    width: 90,
    align: 'center',
    render(row) {
      const ok = registeredKeys.value.has(row.jobKey);
      return h(
        NTag,
        { type: ok ? 'success' : 'warning', size: 'small', bordered: false },
        () => (ok ? '已注册' : '未注册'),
      );
    },
  },
  {
    title: 'Cron',
    key: 'cronExpression',
    width: 180,
    ellipsis: { tooltip: true },
    render: (row) => row.cronExpression || h('span', { class: 'text-gray-400' }, '—'),
  },
  {
    title: '参数结构',
    key: 'paramSchema',
    width: 200,
    ellipsis: { tooltip: true },
    render: (row) =>
      row.paramSchema
        ? h('code', { class: 'text-xs' }, row.paramSchema)
        : h('span', { class: 'text-gray-400' }, '—'),
  },
  {
    title: '启用',
    key: 'enabled',
    width: 80,
    align: 'center',
    render: (row) =>
      h(
        NTag,
        { type: row.enabled ? 'success' : 'default', size: 'small', bordered: false },
        () => (row.enabled ? '已启用' : '已停用'),
      ),
  },
  { title: '描述', key: 'description', width: 200, ellipsis: { tooltip: true } },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 170,
    render: (row) => formatDateTime(row.createdAt),
  },
  {
    title: '操作',
    key: 'action',
    width: 360,
    fixed: 'right',
    align: 'left',
    render: (row) => {
      const actions: ReturnType<typeof h>[] = [];
      if (canAccess('system:job:edit')) {
        actions.push(
          h(
            NButton,
            {
              size: 'tiny',
              type: 'primary',
              tertiary: true,
              onClick: () => handleEdit(row),
            },
            () => '编辑',
          ),
        );
        if (row.enabled) {
          actions.push(
            h(
              NButton,
              {
                size: 'tiny',
                type: 'warning',
                tertiary: true,
                onClick: () => handleStop(row),
              },
              () => '停止',
            ),
          );
        } else {
          actions.push(
            h(
              NButton,
              {
                size: 'tiny',
                type: 'success',
                tertiary: true,
                onClick: () => handleStart(row),
              },
              () => '启动',
            ),
          );
        }
        actions.push(
          h(
            NButton,
            {
              size: 'tiny',
              type: 'info',
              tertiary: true,
              onClick: () => handleRefresh(row),
            },
            () => '刷新',
          ),
        );
        actions.push(
          h(
            NButton,
            {
              size: 'tiny',
              type: 'info',
              tertiary: true,
              onClick: () => handleRunOnce(row),
            },
            () => '执行一次',
          ),
        );
      }
      if (canAccess('system:job:remove')) {
        actions.push(
          h(
            NButton,
            {
              size: 'tiny',
              type: 'error',
              tertiary: true,
              onClick: () => handleDelete([row.id]),
            },
            () => '删除',
          ),
        );
      }
      return actions.length > 0 ? h(NSpace, { size: 4 }, () => actions) : '-';
    },
  },
];

// ---------- 列表加载 ----------
async function loadData() {
  loading.value = true;
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.pageSize,
      blurry: searchForm.blurry || undefined,
      enabled:
        searchForm.enabledFilter === ''
          ? undefined
          : searchForm.enabledFilter === 'true',
    };
    const result: any = await listJob(params);
    data.value = result.records ?? result.list ?? [];
    pagination.total = result.total ?? data.value.length;
  } catch (e: any) {
    console.error('Failed to load jobs:', e);
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
  searchForm.blurry = '';
  searchForm.enabled = undefined;
  searchForm.enabledFilter = '';
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
  selectedRowKeys.value = keys.map(Number);
}

// ---------- 新增 / 编辑 ----------
function resetForm() {
  formData.value = {
    id: undefined,
    jobKey: '',
    jobName: '',
    cronExpression: '',
    enabled: true,
    paramSchema: '',
    description: '',
  };
}

function handleAdd() {
  isEdit.value = false;
  resetForm();
  formModalApi.setState({ title: '新增任务' }).open();
}

async function handleEdit(row: SysJob) {
  isEdit.value = true;
  try {
    const detail = await getJob(row.id);
    formData.value = {
      id: detail.id,
      jobKey: detail.jobKey,
      jobName: detail.jobName,
      cronExpression: detail.cronExpression || '',
      enabled: detail.enabled,
      paramSchema: detail.paramSchema || '',
      description: detail.description || '',
    };
  } catch {
    formData.value = {
      id: row.id,
      jobKey: row.jobKey,
      jobName: row.jobName,
      cronExpression: row.cronExpression || '',
      enabled: row.enabled,
      paramSchema: row.paramSchema || '',
      description: row.description || '',
    };
  }
  formModalApi.setState({ title: '编辑任务' }).open();
}

async function handleSubmit() {
  if (!formData.value.jobKey) {
    notification.warning({ content: '请输入任务Key', duration: 2000 });
    return;
  }
  if (!formData.value.jobName) {
    notification.warning({ content: '请输入任务名称', duration: 2000 });
    return;
  }
  if (formData.value.paramSchema) {
    try {
      JSON.parse(formData.value.paramSchema);
    } catch {
      notification.warning({ content: '参数结构必须为合法 JSON', duration: 2500 });
      return;
    }
  }
  formSubmitting.value = true;
  formModalApi.lock();
  try {
    if (isEdit.value && formData.value.id) {
      await updateJob(formData.value);
    } else {
      await addJob(formData.value);
    }
    notification.success({ content: isEdit.value ? '任务已更新' : '任务已创建', duration: 2000 });
    formModalApi.close();
    loadData();
  } catch (e: any) {
    console.error('Failed to submit:', e);
  } finally {
    formSubmitting.value = false;
    formModalApi.unlock();
  }
}

// ---------- 启停 / 刷新 / 执行 ----------
async function handleStart(row: SysJob) {
  try {
    await startJob(row.jobKey);
    notification.success({ content: `任务 ${row.jobName} 已启动`, duration: 2000 });
    loadData();
  } catch (e: any) {
    console.error('Failed to start job:', e);
  }
}

async function handleStop(row: SysJob) {
  dialog.warning({
    title: '提示',
    content: `确定停止任务「${row.jobName}」的定时调度吗？停止后页面仍可手动执行。`,
    positiveText: '停止',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await stopJob(row.jobKey);
        notification.success({ content: `任务 ${row.jobName} 已停止`, duration: 2000 });
        loadData();
      } catch (e: any) {
      }
    },
  });
}

async function handleRefresh(row: SysJob) {
  try {
    await refreshJob(row.jobKey);
    notification.success({ content: `任务 ${row.jobName} 已重新加载`, duration: 2000 });
    loadData();
  } catch (e: any) {
    console.error('Failed to refresh job:', e);
  }
}

async function handleRunOnce(row: SysJob) {
  // 解析 param_schema，按字段类型动态生成输入项；默认全部 null
  const fields = parseParamSchema(row.paramSchema);
  paramFields.value = fields;
  const initValues: Record<string, unknown> = {};
  for (const f of fields) {
    initValues[f.key] = null;
  }
  runOnceFormValues.value = initValues;
  runOnceForm.value = { jobKey: row.jobKey, params: undefined };
  runOnceModalApi
    .setState({ title: `执行一次 - ${row.jobName}（${row.jobKey}）` })
    .open();
}

async function handleRunOnceWithParamsConfirm() {
  // 根据 paramFields 决定是否有结构化参数；空 schema 时按无参执行
  if (paramFields.value.length > 0) {
    runOnceForm.value.params = { ...runOnceFormValues.value };
  } else {
    runOnceForm.value.params = undefined;
  }
  runOnceSubmitting.value = true;
  runOnceModalApi.lock();
  try {
    await runOnceWithParams(runOnceForm.value);
    notification.success({ content: '已提交执行', duration: 2000 });
    runOnceModalApi.close();
  } catch (e: any) {
    console.error('Failed to run job once:', e);
  } finally {
    runOnceSubmitting.value = false;
    runOnceModalApi.unlock();
  }
}

// ---------- 删除 ----------
function handleDelete(ids: number[]) {
  const idsArr = Array.isArray(ids) ? ids : [ids];
  if (idsArr.length === 0) return;
  dialog.warning({
    title: '提示',
    content: `确定要删除选中的 ${idsArr.length} 个任务吗？删除后无法恢复。`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteJob(idsArr);
        notification.success({ content: `已删除 ${idsArr.length} 个任务`, duration: 2000 });
        selectedRowKeys.value = [];
        loadData();
      } catch (e: any) {
      }
    },
  });
}

// ---------- 导出 ----------
const exporting = ref(false);
async function handleExport() {
  exporting.value = true;
  try {
    await exportJobExcel({
      blurry: searchForm.blurry || undefined,
      enabled:
        searchForm.enabledFilter === ''
          ? undefined
          : searchForm.enabledFilter === 'true',
    });
    notification.success({ content: '导出成功', duration: 2000 });
  } catch (e: any) {
    console.error('Failed to export jobs:', e);
  } finally {
    exporting.value = false;
  }
}

// ---------- 检测已注册 jobKey ----------
async function loadRegisteredKeys() {
  const checks = await Promise.all(
    data.value.map(async (row) => {
      try {
        const ok = await checkRegistered(row.jobKey);
        return ok ? row.jobKey : null;
      } catch {
        return null;
      }
    }),
  );
  registeredKeys.value = new Set(checks.filter((v): v is string => !!v));
}

watch(data, () => {
  loadRegisteredKeys();
});

onMounted(() => loadData());
</script>

<template>
  <div class="p-4">
    <NCard :bordered="false" content-class="!p-4">
      <!-- 搜索区 -->
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <NInput
          v-model:value="searchForm.blurry"
          placeholder="搜索任务Key / 名称 / 描述"
          clearable
          size="small"
          class="!w-72"
          @keyup.enter="handleSearch"
        />
        <NSelect
          v-model:value="searchForm.enabledFilter"
          placeholder="启用状态"
          clearable
          size="small"
          class="!w-32"
          :options="statusOptions"
        />
        <NButton type="primary" size="small" @click="handleSearch">搜索</NButton>
        <NButton size="small" @click="handleReset">重置</NButton>
      </div>

      <!-- 工具栏 -->
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <NButton v-access:code="'system:job:add'" type="primary" size="small" @click="handleAdd">
          新增
        </NButton>
        <NButton
          v-access:code="'system:job:remove'"
          type="error"
          size="small"
          :disabled="selectedRowKeys.length === 0"
          @click="handleDelete(selectedRowKeys)"
        >
          删除
        </NButton>
        <NButton
          v-access:code="'system:job:export'"
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
        :row-key="(row: SysJob) => row.id"
        :checked-row-keys="selectedRowKeys"
        striped
        :scroll-x="1500"
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

    <!-- 新增 / 编辑 Modal -->
    <FormModal class="w-[640px]">
      <NForm :model="formData" label-placement="left" :label-width="100">
        <NFormItem label="任务Key" required>
          <NInput
            v-model:value="formData.jobKey"
            placeholder="请输入任务Key（对应 @SysJob.value）"
            :disabled="isEdit"
          />
        </NFormItem>
        <NFormItem label="任务名称" required>
          <NInput v-model:value="formData.jobName" placeholder="请输入任务名称" />
        </NFormItem>
        <NFormItem label="Cron 表达式" path="cronExpression">
          <NInput
            v-model:value="formData.cronExpression"
            placeholder="例如：0/30 * * * * ?"
          />
        </NFormItem>
        <NFormItem label="参数结构" path="paramSchema">
          <NInput
            v-model:value="formData.paramSchema"
            type="textarea"
            :rows="3"
            placeholder='JSON 对象，例如：{"bizDate":"DATE","force":"BOOLEAN"}'
          />
        </NFormItem>
        <NFormItem label="描述">
          <NInput
            v-model:value="formData.description"
            type="textarea"
            :rows="2"
            placeholder="请输入描述"
          />
        </NFormItem>
        <NFormItem label="启用">
          <NSwitch
            :value="!!formData.enabled"
            @update:value="(v: boolean) => (formData.enabled = v)"
          >
            <template #checked>已启用</template>
            <template #unchecked>已停用</template>
          </NSwitch>
        </NFormItem>
      </NForm>
    </FormModal>

    <!-- 立即执行 Modal：根据 param_schema 动态生成参数输入项 -->
    <RunOnceModal class="w-[640px]">
      <NForm :model="runOnceFormValues" label-placement="left" :label-width="160">
        <template v-if="paramFields.length > 0">
          <NFormItem label="任务参数">
            <NText :depth="3" class="text-xs">
              以下输入项根据任务的 param_schema 自动生成，未填写将以 null 提交
            </NText>
          </NFormItem>
          <NFormItem
            v-for="field in paramFields"
            :key="field.key"
            :label="`${field.key}（${field.type}）`"
          >
            <!-- STRING -->
            <NInput
              v-if="field.type === 'STRING'"
              :value="(runOnceFormValues[field.key] as string | null) ?? ''"
              @update:value="
                (v: string) => {
                  runOnceFormValues[field.key] = v && v.trim() !== '' ? v : null;
                }
              "
              placeholder="字符串"
              clearable
            />
            <!-- INTEGER / LONG -->
            <NInputNumber
              v-else-if="field.type === 'INTEGER' || field.type === 'LONG'"
              :value="(runOnceFormValues[field.key] as number | null) ?? null"
              @update:value="
                (v: number | null) => {
                  runOnceFormValues[field.key] = v;
                }
              "
              :show-button="false"
              :placeholder="field.type === 'LONG' ? '长整数' : '整数'"
              class="!w-full"
            />
            <!-- DOUBLE -->
            <NInputNumber
              v-else-if="field.type === 'DOUBLE'"
              :value="(runOnceFormValues[field.key] as number | null) ?? null"
              @update:value="
                (v: number | null) => {
                  runOnceFormValues[field.key] = v;
                }
              "
              :show-button="false"
              :precision="2"
              placeholder="浮点数"
              class="!w-full"
            />
            <!-- BOOLEAN -->
            <NSwitch
              v-else-if="field.type === 'BOOLEAN'"
              :value="!!runOnceFormValues[field.key]"
              @update:value="
                (v: boolean) => {
                  runOnceFormValues[field.key] = v;
                }
              "
            >
              <template #checked>是</template>
              <template #unchecked>否</template>
            </NSwitch>
            <!-- DATE -->
            <NDatePicker
              v-else-if="field.type === 'DATE'"
              :value="(runOnceFormValues[field.key] as number | null) ?? null"
              @update:value="
                (v: number | null) => {
                  runOnceFormValues[field.key] = v;
                }
              "
              type="date"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              clearable
              class="!w-full"
            />
            <!-- DATETIME -->
            <NDatePicker
              v-else-if="field.type === 'DATETIME'"
              :value="(runOnceFormValues[field.key] as number | null) ?? null"
              @update:value="
                (v: number | null) => {
                  runOnceFormValues[field.key] = v;
                }
              "
              type="datetime"
              value-format="yyyy-MM-dd HH:mm:ss"
              format="yyyy-MM-dd HH:mm:ss"
              clearable
              class="!w-full"
            />
            <!-- 兜底：未知类型回退为文本输入 -->
            <NInput
              v-else
              :value="(runOnceFormValues[field.key] as string | null) ?? ''"
              @update:value="
                (v: string) => {
                  runOnceFormValues[field.key] = v;
                }
              "
              placeholder="文本"
              clearable
            />
          </NFormItem>
        </template>

        <NFormItem v-else label="执行参数">
          <NText :depth="3" class="text-xs">
            此任务未配置参数结构（param_schema），将以无参方式执行
          </NText>
        </NFormItem>
      </NForm>
      <div class="mt-2 text-xs text-gray-500">
        任务方法中通过 <code>context.getParams().get("bizDate")</code> 读取参数；未填写的字段将以 <code>null</code> 提交。
      </div>
    </RunOnceModal>
  </div>
</template>
