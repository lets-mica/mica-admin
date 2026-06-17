<script setup lang="ts">
import type { DataTableColumns, UploadFileInfo } from 'naive-ui';
import type { FileStorageItem, StorageType } from '#/api/oss';
import {
  deleteFileStorage,
  getFileStorageList,
  uploadFileStorage,
} from '#/api/oss';

import { createIconifyIcon } from '@vben-core/icons';
import { h, onMounted, reactive, ref } from 'vue';
import {
  NButton,
  NCard,
  NDataTable,
  NDatePicker,
  NForm,
  NFormItem,
  NIcon,
  NImage,
  NInput,
  NModal,
  NPagination,
  NSelect,
  NSpace,
  NSwitch,
  NTag,
  NText,
  NUpload,
  NUploadDragger,
} from 'naive-ui';
import { useVbenModal } from '@vben-core/popup-ui';
import { dialog, notification } from '#/adapter/naive';
import { formatDateTime } from '#/utils/format-date';
import {
  Copy as CopyIcon,
  Download as DownloadIcon,
  Eye as PreviewIcon,
  ImagePlus as UploadIcon,
  RotateCw as RefreshIcon,
} from '@vben/icons';

/** 通用 iconify 图标（@vben/icons 内置 lucide 集合中不存在的，用 iconify 兜底） */
const Trash2Icon = createIconifyIcon('mdi:trash-can-outline');
const FileIcon = createIconifyIcon('mdi:file-document-outline');

defineOptions({ name: 'FileStorageManagement' });

// =================== 列表 ===================
const loading = ref(false);
const data = ref<FileStorageItem[]>([]);
const pagination = reactive({ page: 1, pageSize: 10, total: 0 });
const searchText = ref('');
const fileTypeFilter = ref<string | null>(null);
const storageTypeFilter = ref<StorageType | null>(null);
const dateRange = ref<[number, number] | null>(null);
const selectedRowKeys = ref<number[]>([]);

// 预览
const previewVisible = ref(false);
const previewItem = ref<FileStorageItem | null>(null);

// 业务类型选项（可由后端动态下发，这里给出常用预设）
const fileTypeOptions = [
  { label: '头像 (avatar)', value: 'avatar' },
  { label: '图片 (image)', value: 'image' },
  { label: '文档 (document)', value: 'document' },
  { label: '视频 (video)', value: 'video' },
  { label: '音频 (audio)', value: 'audio' },
  { label: '压缩包 (archive)', value: 'archive' },
  { label: '其他 (other)', value: 'other' },
];

/**
 * 存储平台选项
 * value 统一使用 x-file-storage 的 platform 名称（kebab-case），
 * 后端 SysFileStorage.storageType 也按此原样存储/过滤。
 */
const storageTypeOptions = [
  { label: '本地 LOCAL', value: 'local-plus' },
  { label: '阿里云 OSS', value: 'aliyun-oss' },
  { label: '腾讯云 COS', value: 'tencent-cos' },
  { label: '七牛云 Kodo', value: 'qiniu-kodo' },
  { label: 'MinIO', value: 'minio' },
  { label: 'AWS S3', value: 'amazon-s3' },
  { label: '华为云 OBS', value: 'huawei-obs' },
  { label: '百度云 BOS', value: 'baidu-bos' },
];

/** 平台 value -> 表格里展示的中文名 */
const storageTypeLabel: Record<string, string> = Object.fromEntries(
  storageTypeOptions.map((o) => [o.value, o.label.replace(/\s*\(.+\)$/, '')]),
);

const storageTypeColor: Record<string, 'default' | 'info' | 'success' | 'warning' | 'primary'> = {
  'local-plus': 'default',
  'aliyun-oss': 'info',
  'tencent-cos': 'primary',
  'qiniu-kodo': 'success',
  'minio': 'warning',
  'amazon-s3': 'warning',
  'amazon-s3-v2': 'warning',
  'huawei-obs': 'info',
  'baidu-bos': 'primary',
};

// =================== 上传弹窗 ===================
const uploadFileList = ref<UploadFileInfo[]>([]);
const uploadLoading = ref(false);

const uploadForm = reactive({
  fileType: null as string | null,
  isPrivate: false,
  storageType: null as StorageType | null,
});

const uploadMaxSize = 50; // MB

/**
 * NUpload 的 before-upload 钩子：超过大小限制时给出提示并拒绝。
 * @param data NUpload 内部数据
 * @returns true 允许加入列表，false 拒绝
 */
function handleBeforeUpload(data: { file: UploadFileInfo }): boolean {
  const file = data.file.file;
  if (!file) return true;
  const sizeMB = file.size / 1024 / 1024;
  if (sizeMB > uploadMaxSize) {
    notification.warning({
      content: `文件 ${file.name} 超过 ${uploadMaxSize}MB，无法上传`,
      duration: 2500,
    });
    return false;
  }
  return true;
}

const [UploadModal, uploadModalApi] = useVbenModal({
  onConfirm: handleUploadConfirm,
  onOpenChange: (open: boolean) => {
    if (open) {
      // 打开时重置表单
      uploadForm.fileType = null;
      uploadForm.isPrivate = false;
      uploadForm.storageType = null;
      uploadFileList.value = [];
    }
  },
});

async function handleUploadConfirm() {
  if (uploadFileList.value.length === 0) {
    notification.warning({ content: '请先选择要上传的文件', duration: 2000 });
    return;
  }
  // 过滤：剔除超过最大尺寸的文件
  const valid: UploadFileInfo[] = [];
  for (const f of uploadFileList.value) {
    if (!(f.file instanceof File)) continue;
    const sizeMB = f.file.size / 1024 / 1024;
    if (sizeMB > uploadMaxSize) {
      notification.warning({
        content: `文件 ${f.name} 超过 ${uploadMaxSize}MB，已跳过`,
        duration: 2500,
      });
      continue;
    }
    valid.push(f);
  }
  if (valid.length === 0) {
    return;
  }
  uploadLoading.value = true;
  uploadModalApi.lock();
  try {
    let okCount = 0;
    let failCount = 0;
    for (const f of valid) {
      try {
        await uploadFileStorage({
          file: f.file as File,
          fileType: uploadForm.fileType || undefined,
          isPrivate: uploadForm.isPrivate,
          storageType: uploadForm.storageType || undefined,
        });
        okCount++;
      } catch (e) {
        failCount++;
        console.error('Upload file failed:', e);
      }
    }
    if (okCount > 0) {
      notification.success({
        content: `成功上传 ${okCount} 个文件${failCount ? `，失败 ${failCount} 个` : ''}`,
        duration: 2500,
      });
      uploadModalApi.close();
      loadData();
    } else {
      notification.error({ content: '上传失败，请重试', duration: 2500 });
    }
  } finally {
    uploadLoading.value = false;
    uploadModalApi.unlock();
  }
}

// =================== 列表列 ===================
const columns: DataTableColumns<FileStorageItem> = [
  { type: 'selection', width: 50 },
  {
    title: '预览',
    key: 'preview',
    width: 80,
    align: 'center',
    render: (row) => {
      if (isImageMime(row.mimeType, row.fileName)) {
        return h('img', {
          src: row.url,
          alt: row.fileName,
          class: 'preview-thumb',
          onClick: () => openPreview(row),
        });
      }
      return h(NIcon, { size: 28, color: '#909399' }, { default: () => h(FileIcon) });
    },
  },
  {
    title: '文件名',
    key: 'fileName',
    width: 240,
    ellipsis: { tooltip: true },
    render: (row) => {
      return h('div', { class: 'flex flex-col gap-0.5' }, [
        h('span', { class: 'truncate font-medium' }, row.fileName || '-'),
        row.suffix
          ? h(NText, { depth: 3, class: '!text-xs' }, { default: () => `.${row.suffix}` })
          : null,
      ]);
    },
  },
  {
    title: '大小',
    key: 'size',
    width: 100,
    render: (row) => formatFileSize(row.size),
  },
  {
    title: '业务类型',
    key: 'fileType',
    width: 110,
    render: (row) =>
      row.fileType
        ? h(NTag, { size: 'small', bordered: false, type: 'info' }, { default: () => row.fileType })
        : h('span', { class: 'text-gray-400' }, '-'),
  },
  {
    title: '存储',
    key: 'storageType',
    width: 110,
    render: (row) => {
      const value = row.storageType || '';
      return h(
        NTag,
        { size: 'small', bordered: false, type: storageTypeColor[value] || 'default' },
        { default: () => storageTypeLabel[value] || value || '-' },
      );
    },
  },
  {
    title: '上传者',
    key: 'createdBy',
    width: 110,
    ellipsis: { tooltip: true },
    render: (row) => row.createdBy || '-',
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
    width: 200,
    fixed: 'right',
    align: 'center',
    render: (row) =>
      h(NSpace, { size: 'small' }, () => [
        row.url
          ? h(
              NButton,
              {
                size: 'small',
                type: 'primary',
                tertiary: true,
                onClick: () => openPreview(row),
              },
              { default: () => '预览', icon: () => h(NIcon, null, { default: () => h(PreviewIcon) }) },
            )
          : null,
        row.url
          ? h(
              NButton,
              {
                size: 'small',
                type: 'info',
                tertiary: true,
                onClick: () => handleCopyUrl(row),
              },
              { default: () => '复制', icon: () => h(NIcon, null, { default: () => h(CopyIcon) }) },
            )
          : null,
        row.url
          ? h(
              NButton,
              {
                size: 'small',
                tertiary: true,
                onClick: () => handleDownload(row),
              },
              { default: () => '下载', icon: () => h(NIcon, null, { default: () => h(DownloadIcon) }) },
            )
          : null,
        h(
          NButton,
          {
            size: 'small',
            type: 'error',
            tertiary: true,
            onClick: () => handleDelete(row.id),
          },
          { default: () => '删除', icon: () => h(NIcon, null, { default: () => h(Trash2Icon) }) },
        ),
      ]),
  },
];

// =================== 数据加载 ===================
async function loadData() {
  loading.value = true;
  try {
    const params: Record<string, any> = {
      page: pagination.page,
      size: pagination.pageSize,
    };
    if (searchText.value) params.fileName = searchText.value;
    if (fileTypeFilter.value) params.fileType = fileTypeFilter.value;
    if (storageTypeFilter.value) params.storageType = storageTypeFilter.value;
    if (dateRange.value && dateRange.value.length === 2) {
      params.createdAt = dateRange.value.map((ts) => new Date(ts).toISOString());
    }

    const result = await getFileStorageList(params);
    data.value = result.list || [];
    pagination.total = result.total || 0;
  } catch (e: any) {
    console.error('Failed to load file list:', e);
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

function handleResetSearch() {
  searchText.value = '';
  fileTypeFilter.value = null;
  storageTypeFilter.value = null;
  dateRange.value = null;
  pagination.page = 1;
  loadData();
}

function handleRefresh() {
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

// =================== 操作 ===================
function openPreview(row: FileStorageItem) {
  if (!row.url) return;
  previewItem.value = row;
  previewVisible.value = true;
}

async function handleCopyUrl(row: FileStorageItem) {
  if (!row.url) {
    notification.warning({ content: '该文件没有可复制的地址', duration: 2000 });
    return;
  }
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(row.url);
    } else {
      // 降级方案
      const textarea = document.createElement('textarea');
      textarea.value = row.url;
      textarea.style.position = 'fixed';
      textarea.style.opacity = '0';
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
    }
    notification.success({ content: '链接已复制', duration: 1800 });
  } catch (e) {
    console.error('Copy failed:', e);
  }
}

function handleDownload(row: FileStorageItem) {
  if (!row.url) {
    notification.warning({ content: '该文件没有可下载的地址', duration: 2000 });
    return;
  }
  // 新窗口直接打开，由浏览器或对象存储自行处理下载
  const link = document.createElement('a');
  link.href = row.url;
  link.download = row.fileName || 'download';
  link.target = '_blank';
  link.rel = 'noopener noreferrer';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

function handleDelete(id: number) {
  dialog.warning({
    title: '提示',
    content: '确定要删除该文件吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteFileStorage([id]);
        notification.success({ content: '文件已删除', duration: 2000 });
        loadData();
      } catch (e: any) {
        console.error('Failed to delete:', e);
      }
    },
  });
}

function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    notification.warning({ content: '请选择要删除的文件', duration: 2000 });
    return;
  }
  dialog.warning({
    title: '提示',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个文件吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteFileStorage(selectedRowKeys.value);
        notification.success({
          content: `已删除 ${selectedRowKeys.value.length} 个文件`,
          duration: 2000,
        });
        selectedRowKeys.value = [];
        loadData();
      } catch (e: any) {
        console.error('Failed to batch delete:', e);
      }
    },
  });
}

function handleUpload() {
  uploadModalApi.setState({ title: '上传文件' }).open();
}

function handleCheckedRowKeysChange(keys: (string | number)[]) {
  selectedRowKeys.value = keys.map(Number);
}

// =================== 工具函数 ===================
/** 字节大小格式化为人类可读字符串 */
function formatFileSize(bytes?: number | null): string {
  if (bytes === undefined || bytes === null || isNaN(bytes)) return '-';
  if (bytes === 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const k = 1024;
  const i = Math.min(Math.floor(Math.log(bytes) / Math.log(k)), units.length - 1);
  const val = bytes / Math.pow(k, i);
  return `${val.toFixed(val >= 100 || i === 0 ? 0 : 2)} ${units[i]}`;
}

/** 判断是否为图片 MIME / 后缀 */
function isImageMime(mime?: string, name?: string): boolean {
  if (mime?.startsWith?.('image/')) return true;
  if (!name) return false;
  return /\.(png|jpe?g|gif|webp|bmp|svg|ico|avif)$/i.test(name);
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
          placeholder="搜索文件名"
          clearable
          class="!w-64"
          @keyup.enter="handleSearch"
        />
        <NSelect
          v-model:value="fileTypeFilter"
          :options="fileTypeOptions"
          placeholder="业务类型"
          clearable
          class="!w-44"
        />
        <NSelect
          v-model:value="storageTypeFilter"
          :options="storageTypeOptions"
          placeholder="存储类型"
          clearable
          class="!w-36"
        />
        <NDatePicker
          v-model:value="dateRange"
          type="daterange"
          clearable
          class="!w-80"
        />
        <NButton type="primary" size="small" @click="handleSearch">搜索</NButton>
        <NButton size="small" @click="handleResetSearch">重置</NButton>
      </div>

      <!-- 工具栏 -->
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <NButton type="primary" size="small" @click="handleUpload">
          <template #icon><NIcon><UploadIcon /></NIcon></template>
          上传文件
        </NButton>
        <NButton size="small" :loading="loading" @click="handleRefresh">
          <template #icon><NIcon><RefreshIcon /></NIcon></template>
          刷新
        </NButton>
        <NButton
          size="small"
          type="error"
          :disabled="selectedRowKeys.length === 0"
          @click="handleBatchDelete"
        >
          <template #icon><NIcon><Trash2Icon /></NIcon></template>
          删除
        </NButton>
      </div>

      <NDataTable
        :columns="columns"
        :data="data"
        :loading="loading"
        :row-key="(row: FileStorageItem) => row.id"
        :checked-row-keys="selectedRowKeys"
        :scroll-x="1280"
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

    <!-- 图片预览 -->
    <NModal
      v-model:show="previewVisible"
      preset="card"
      :title="previewItem?.fileName || '文件预览'"
      style="max-width: 900px"
    >
      <div v-if="previewItem" class="preview-modal-body">
        <NImage
          v-if="isImageMime(previewItem.mimeType, previewItem.fileName) && previewItem.url"
          :src="previewItem.url"
          :alt="previewItem.fileName"
          object-fit="contain"
          style="width: 100%; max-height: 70vh"
          show-toolbar
        />
        <div v-else class="file-detail">
          <NIcon size="64" color="#909399"><FileIcon /></NIcon>
          <p class="mt-2 text-base">{{ previewItem.fileName }}</p>
          <p class="text-sm text-gray-500">该文件类型不支持在线预览</p>
        </div>

        <NForm
          label-placement="left"
          :label-width="100"
          size="small"
          class="mt-4 detail-form"
        >
          <NFormItem label="访问地址">
            <div class="flex flex-1 items-center gap-2">
              <NInput :value="previewItem.url" readonly class="!flex-1" />
              <NButton
                size="small"
                type="primary"
                @click="previewItem && handleCopyUrl(previewItem)"
              >
                复制
              </NButton>
            </div>
          </NFormItem>
          <NFormItem label="存储类型">
            <NTag
              size="small"
              :bordered="false"
              :type="storageTypeColor[previewItem.storageType || ''] || 'default'"
            >
              {{ storageTypeLabel[previewItem.storageType || ''] || previewItem.storageType || '-' }}
            </NTag>
          </NFormItem>
          <NFormItem label="文件大小">{{ formatFileSize(previewItem.size) }}</NFormItem>
          <NFormItem label="MIME 类型">{{ previewItem.mimeType || '-' }}</NFormItem>
          <NFormItem label="业务类型">{{ previewItem.fileType || '-' }}</NFormItem>
          <NFormItem label="后缀">{{ previewItem.suffix || '-' }}</NFormItem>
          <NFormItem label="MD5">
            <NText :depth="3" class="break-all">{{ previewItem.md5 || '-' }}</NText>
          </NFormItem>
          <NFormItem label="上传者">{{ previewItem.createdBy || '-' }}</NFormItem>
          <NFormItem label="上传时间">{{ formatDateTime(previewItem.createdAt) }}</NFormItem>
        </NForm>
      </div>
    </NModal>

    <!-- 上传弹窗 -->
    <UploadModal class="upload-modal">
      <NForm label-placement="left" :label-width="80" size="small">
        <NFormItem label="业务类型">
          <NSelect
            v-model:value="uploadForm.fileType"
            :options="fileTypeOptions"
            placeholder="选择业务类型（可选）"
            clearable
            filterable
            tag
          />
        </NFormItem>
        <NFormItem label="存储类型">
          <NSelect
            v-model:value="uploadForm.storageType"
            :options="storageTypeOptions"
            placeholder="默认 LOCAL（可选）"
            clearable
          />
        </NFormItem>
        <NFormItem label="是否私有">
          <NSwitch v-model:value="uploadForm.isPrivate">
            <template #checked>私有</template>
            <template #unchecked>公开</template>
          </NSwitch>
        </NFormItem>
        <NFormItem label="选择文件" required>
          <NUpload
            v-model:file-list="uploadFileList"
            :max="20"
            :default-upload="false"
            :multiple="true"
            accept="*/*"
            @before-upload="handleBeforeUpload"
          >
            <NUploadDragger>
              <div class="upload-dragger-inner">
                <NIcon size="36" color="#18a058"><UploadIcon /></NIcon>
                <p class="mt-2 text-sm">点击或拖拽文件到此处上传</p>
                <p class="text-xs text-gray-400">
                  单文件最大 {{ uploadMaxSize }} MB，可同时选择多个
                </p>
              </div>
            </NUploadDragger>
          </NUpload>
        </NFormItem>
      </NForm>
    </UploadModal>
  </div>
</template>

<style scoped>
.preview-thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: opacity 0.2s;
  border: 1px solid var(--border-color, #f0f0f0);
}

.preview-thumb:hover {
  opacity: 0.8;
}

.preview-modal-body {
  display: flex;
  flex-direction: column;
}

.file-detail {
  text-align: center;
  padding: 48px 16px;
  color: #909399;
}

.detail-form :deep(.n-form-item) {
  margin-bottom: 8px;
}

.upload-dragger-inner {
  padding: 16px 8px;
  text-align: center;
}

.upload-modal :deep(.n-upload-trigger) {
  width: 100%;
}
</style>
