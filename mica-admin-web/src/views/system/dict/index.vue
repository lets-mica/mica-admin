<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';
import type { DictItem, DictTypeItem } from '#/api/system/dict';
import { addDict, deleteDict, editDict, getDictList, getDictItems, addDictItem, editDictItem, deleteDictItem, exportDictExcel, exportDictInfoExcel } from '#/api/system/dict';

import { NButton, NCard, NDataTable, NForm, NFormItem, NInput, NInputNumber, NSpace, NPagination, NIcon } from 'naive-ui';
import { useAccess } from '@vben/access';
import { useVbenModal } from '@vben-core/popup-ui';
import { h, onMounted, ref } from 'vue';
import { dialog, notification } from '#/adapter/naive';
import { Plus, Search, UserRoundPen as EditIcon, Eraser as DeleteIcon } from '@vben/icons';

defineOptions({ name: 'DictManagement' });
const { hasAccessByCodes } = useAccess();
const canAccess = (codes: string | string[]) =>
  hasAccessByCodes(Array.isArray(codes) ? codes : [codes]);

const typesLoading = ref(false);
const dataLoading = ref(false);
const dictTypes = ref<DictTypeItem[]>([]);
const selectedType = ref<DictTypeItem | null>(null);
const dictData = ref<DictItem[]>([]);

const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const searchKey = ref('');

const typeFormData = ref({ id: undefined as number | undefined, name: '', description: '' });
const typeIsEdit = ref(false);

const dictFormData = ref({ id: undefined as number | undefined, label: '', value: '', dictSort: 999 });
const dictIsEdit = ref(false);

const [TypeModal, typeModalApi] = useVbenModal({
  onConfirm: handleSubmitType,
});

const [DictModal, dictModalApi] = useVbenModal({
  onConfirm: handleSubmitDict,
});

const dictColumns: DataTableColumns<DictItem> = [
  { title: '标签', key: 'label', width: 150 },
  { title: '值', key: 'value', width: 150 },
  { title: '排序', key: 'seq', width: 80, align: 'center' as const },
  {
    title: '操作',
    key: 'action',
    width: 160,
    align: 'center' as const,
    render: (row: DictItem) => {
      const actions: ReturnType<typeof h>[] = [];
      if (canAccess('system:dict:edit')) {
        actions.push(
          h(NButton, { size: 'small', type: 'primary', tertiary: true, onClick: () => handleEditDict(row) }, () => '编辑'),
        );
      }
      if (canAccess('system:dict:del')) {
        actions.push(
          h(NButton, { size: 'small', type: 'error', tertiary: true, onClick: () => handleDeleteDict(row.id) }, () => '删除'),
        );
      }
      return actions.length > 0 ? h(NSpace, { size: 'small' }, () => actions) : '-';
    },
  },
];

async function loadDictTypes() {
  typesLoading.value = true;
  try {
    const params: Record<string, string | number> = {
      page: page.value - 1,
      size: pageSize.value,
    };
    if (searchKey.value) {
      params.blurry = searchKey.value;
    }
    const result = await getDictList(params);
    dictTypes.value = result.list ?? [];
    total.value = result.total ?? 0;

    if (dictTypes.value.length > 0) {
      const currentName = selectedType.value?.name;
      const matched = currentName
        ? dictTypes.value.find((item) => item.name === currentName)
        : undefined;
      selectedType.value = matched ?? dictTypes.value[0];
      await loadDictData();
    } else {
      selectedType.value = null;
      dictData.value = [];
    }
  } catch (e: any) {
    console.error('Failed to load dict types:', e);
    dictTypes.value = [];
    total.value = 0;
    selectedType.value = null;
    dictData.value = [];
  } finally {
    typesLoading.value = false;
  }
}

async function loadDictData() {
  if (!selectedType.value) {
    dictData.value = [];
    return;
  }
  dataLoading.value = true;
  try {
    dictData.value = await getDictItems(selectedType.value.name);
  } catch (e: any) {
    console.error('Failed to load dict data:', e);
    dictData.value = [];
  } finally {
    dataLoading.value = false;
  }
}

function handleSearch() {
  page.value = 1;
  loadDictTypes();
}

function handlePageChange(pageNum: number) {
  page.value = pageNum;
  loadDictTypes();
}

function handlePageSizeChange(size: number) {
  pageSize.value = size;
  page.value = 1;
  loadDictTypes();
}

async function selectDictType(type: DictTypeItem) {
  selectedType.value = type;
  await loadDictData();
}

function handleAddType() {
  typeIsEdit.value = false;
  typeFormData.value = { id: undefined, name: '', description: '' };
  typeModalApi.setState({ title: '新增字典类型' }).open();
}

function handleEditType(row: DictTypeItem) {
  typeIsEdit.value = true;
  typeFormData.value = { id: row.id, name: row.name || '', description: row.description || '' };
  typeModalApi.setState({ title: '编辑字典类型' }).open();
}

async function handleSubmitType() {
  if (!typeFormData.value.name) {
    notification.warning({ content: '请输入字典名称', duration: 2000 });
    return;
  }
  typeModalApi.lock();
  try {
    if (typeIsEdit.value && typeFormData.value.id) {
      await editDict(typeFormData.value as any);
    } else {
      await addDict(typeFormData.value);
    }
    notification.success({ content: typeIsEdit.value ? '字典类型已更新' : '字典类型已创建', duration: 2000 });
    typeModalApi.close();
    loadDictTypes();
  } catch (e: any) {
    console.error('Failed to submit type:', e);
  } finally {
    typeModalApi.unlock();
  }
}

function handleDeleteType(id: number) {
  dialog.warning({
    title: '提示',
    content: '确定要删除该字典类型吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteDict([id]);
        notification.success({ content: '字典类型已删除', duration: 2000 });
        if (selectedType.value?.id === id) {
          selectedType.value = null;
          dictData.value = [];
        }
        loadDictTypes();
      } catch (e: any) {
        console.error('Failed to delete type:', e);
      }
    },
  });
}

function handleAddDict() {
  if (!selectedType.value) {
    notification.warning({ content: '请先选择字典类型', duration: 2000 });
    return;
  }
  dictIsEdit.value = false;
  dictFormData.value = { id: undefined, label: '', value: '', dictSort: 999 };
  dictModalApi.setState({ title: '新增' }).open();
}

function handleEditDict(row: DictItem) {
  dictIsEdit.value = true;
  dictFormData.value = {
    id: row.id,
    label: row.label || '',
    value: row.value || '',
    dictSort: row.seq ?? 999,
  };
  dictModalApi.setState({ title: '编辑字典项' }).open();
}

async function handleSubmitDict() {
  if (!dictFormData.value.label) {
    notification.warning({ content: '请输入标签', duration: 2000 });
    return;
  }
  if (!dictFormData.value.value) {
    notification.warning({ content: '请输入值', duration: 2000 });
    return;
  }
  dictModalApi.lock();
  try {
    const typeName = selectedType.value?.name;
    if (!typeName) {
      notification.warning({ content: '请先选择字典类型', duration: 2000 });
      return;
    }
    const itemPayload = {
      type: typeName,
      label: dictFormData.value.label,
      value: dictFormData.value.value,
      seq: dictFormData.value.dictSort,
    };
    if (dictIsEdit.value && dictFormData.value.id) {
      await editDictItem({ id: dictFormData.value.id, ...itemPayload });
    } else {
      await addDictItem(itemPayload);
    }
    notification.success({ content: dictIsEdit.value ? '字典项已更新' : '字典项已创建', duration: 2000 });
    dictModalApi.close();
    loadDictData();
  } catch (e: any) {
    console.error('Failed to submit dict:', e);
  } finally {
    dictModalApi.unlock();
  }
}

function handleDeleteDict(id: number) {
  dialog.warning({
    title: '提示',
    content: '确定要删除该字典项吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteDictItem(id);
        notification.success({ content: '字典项已删除', duration: 2000 });
        loadDictData();
      } catch (e: any) {
        console.error('Failed to delete dict:', e);
      }
    },
  });
}

onMounted(() => {
  loadDictTypes();
});

const exportingType = ref(false);
const exportingItem = ref(false);

async function handleExportType() {
  exportingType.value = true;
  try {
    const params: Record<string, unknown> = {};
    if (searchKey.value) params.blurry = searchKey.value;
    await exportDictExcel(params);
    notification.success({ content: '导出字典类型成功', duration: 2000 });
  } catch (e: any) {
    console.error('Export dict type failed:', e);
  } finally {
    exportingType.value = false;
  }
}

async function handleExportItem() {
  if (!selectedType.value) {
    notification.warning({ content: '请先选择字典类型', duration: 2000 });
    return;
  }
  exportingItem.value = true;
  try {
    await exportDictInfoExcel({ name: selectedType.value.name });
    notification.success({ content: '导出字典详情成功', duration: 2000 });
  } catch (e: any) {
    console.error('Export dict info failed:', e);
  } finally {
    exportingItem.value = false;
  }
}
</script>

<template>
  <div class="dict-page p-4">
    <div class="grid gap-4 lg:grid-cols-[minmax(280px,320px)_minmax(0,1fr)]">
    <NCard class="min-w-0" :bordered="false" content-class="!p-3">
      <template #header>
        <div class="text-base font-semibold">字典类型</div>
      </template>
      <template #header-extra>
        <NSpace size="small">
          <NButton v-access:code="'system:dict:add'" size="small" type="primary" @click="handleAddType">
            <template #icon><NIcon :size="16"><Plus /></NIcon></template>
            新增
          </NButton>
          <NButton v-access:code="'system:dict:export'" size="small" type="warning" :loading="exportingType" @click="handleExportType">
            导出
          </NButton>
        </NSpace>
      </template>

      <div class="mb-3 flex gap-2">
        <NInput
          v-model:value="searchKey"
          placeholder="输入名称或描述搜索"
          size="small"
          clearable
          @keyup.enter="handleSearch"
        />
        <NButton size="small" type="primary" @click="handleSearch">
          <template #icon><NIcon :size="14"><Search /></NIcon></template>
        </NButton>
      </div>

      <div v-if="typesLoading" class="py-8 text-center text-gray-400">加载中...</div>

      <div v-else-if="!dictTypes.length" class="py-12 text-center text-gray-400">
        <NIcon :size="48" class="mx-auto mb-3 opacity-50"><Search /></NIcon>
        <p>暂无字典类型</p>
      </div>

      <div v-else class="dict-type-list">
        <div
          v-for="type in dictTypes"
          :key="type.id"
          class="dict-type-card"
          :class="{ selected: selectedType?.id === type.id }"
          @click="selectDictType(type)"
        >
          <div class="card-content">
            <h4>{{ type.name }}</h4>
            <p>{{ type.description }}</p>
          </div>
          <div class="card-actions">
            <NButton v-access:code="'system:dict:edit'" size="tiny" tertiary @click.stop="handleEditType(type)">
              <template #icon><NIcon :size="14"><EditIcon /></NIcon></template>
            </NButton>
            <NButton v-access:code="'system:dict:del'" size="tiny" tertiary type="error" @click.stop="handleDeleteType(type.id)">
              <template #icon><NIcon :size="14"><DeleteIcon /></NIcon></template>
            </NButton>
          </div>
        </div>
      </div>

      <div v-if="total > pageSize" class="mt-4 flex justify-center pt-4">
        <NPagination
          :page="page"
          :page-size="pageSize"
          :item-count="total"
          size="small"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </NCard>

    <NCard class="min-w-0" :bordered="false" content-class="!p-4">
      <template #header>
        <div class="text-base font-semibold">
          {{ selectedType ? `${selectedType.description || selectedType.name}` : '字典详情' }}
        </div>
      </template>
      <template #header-extra>
        <NSpace size="small">
          <NButton v-access:code="'system:dict:add'" size="small" type="primary" :disabled="!selectedType" @click="handleAddDict">
            <template #icon><NIcon :size="16"><Plus /></NIcon></template>
            新增
          </NButton>
          <NButton v-access:code="'system:dict:export'" size="small" type="warning" :disabled="!selectedType" :loading="exportingItem" @click="handleExportItem">
            导出
          </NButton>
        </NSpace>
      </template>

      <div v-if="!selectedType" class="empty-detail py-20 text-center text-[var(--n-text-color-3)]">
        <p class="text-base">点击左侧字典类型查看详情</p>
      </div>

      <NDataTable
        v-else
        :loading="dataLoading"
        :columns="dictColumns"
        :data="dictData"
        :row-key="(row: DictItem) => row.id"
        :scroll-x="720"
        striped
      />
    </NCard>
    </div>

    <!-- Modal 放在单一根节点内，避免 RouterView/KeepAlive 指令失效导致白屏 -->
    <TypeModal class="w-[480px]">
      <NForm :model="typeFormData" label-placement="left" :label-width="80">
        <NFormItem label="字典名称" required>
          <NInput v-model:value="typeFormData.name" placeholder="请输入字典名称" />
        </NFormItem>
        <NFormItem label="描述">
          <NInput v-model:value="typeFormData.description" placeholder="请输入描述" />
        </NFormItem>
      </NForm>
    </TypeModal>

    <DictModal class="w-[480px]">
      <NForm :model="dictFormData" label-placement="left" :label-width="80">
        <NFormItem label="标签" required>
          <NInput v-model:value="dictFormData.label" placeholder="请输入标签" />
        </NFormItem>
        <NFormItem label="值" required>
          <NInput v-model:value="dictFormData.value" placeholder="请输入值" />
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="dictFormData.dictSort" :min="0" :max="9999" class="w-full" />
        </NFormItem>
      </NForm>
    </DictModal>
  </div>
</template>

<style scoped>
.dict-type-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.dict-type-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dict-type-card:hover {
  background-color: rgba(59, 130, 246, 0.05);
}

.dict-type-card.selected {
  background-color: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.5);
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-content h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 500;
}

.card-content p {
  margin: 0;
  font-size: 12px;
  color: var(--n-text-color-3, #999);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.dict-type-card:hover .card-actions,
.dict-type-card.selected .card-actions {
  opacity: 1;
}
</style>
