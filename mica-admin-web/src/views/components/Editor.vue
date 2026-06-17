<script setup lang="ts">
import { NCard, NButton, NSpace, NGrid, NGi, NIcon } from 'naive-ui';
import { ref } from 'vue';
import { notification } from '#/adapter/naive';
import { Copy as CopyIcon, Eraser as EraserIcon } from '@vben/icons';
import { uploadFileStorage } from '#/api/oss';

defineOptions({ name: 'RichEditor' });

const editorRef = ref<HTMLDivElement | null>(null);
const fileInputRef = ref<HTMLInputElement | null>(null);
const uploading = ref(false);
const content = ref<string>(`
  <h2>欢迎使用富文本编辑器</h2>
  <p>这是一个基于 <a href="https://www.kancloud.cn/wangfupeng/wangeditor3/332599" target="_blank">wangEditor</a> 的富文本编辑器。</p>
  <ul>
    <li>支持富文本编辑</li>
    <li>支持图片上传</li>
    <li>支持实时预览</li>
  </ul>
`);

interface ToolbarItem {
  name: string;
  label: string;
  command: string;
  arg?: string;
}

// 工具栏配置（文字按钮）
const toolbarItems: ToolbarItem[] = [
  { name: 'undo', label: '撤销', command: 'undo' },
  { name: 'redo', label: '重做', command: 'redo' },
  { name: 'h1', label: 'H1', command: 'formatBlock', arg: 'H1' },
  { name: 'h2', label: 'H2', command: 'formatBlock', arg: 'H2' },
  { name: 'bold', label: 'B', command: 'bold' },
  { name: 'italic', label: 'I', command: 'italic' },
  { name: 'underline', label: 'U', command: 'underline' },
  { name: 'strike', label: 'S̵', command: 'strikeThrough' },
  { name: 'ul', label: '• List', command: 'insertUnorderedList' },
  { name: 'ol', label: '1. List', command: 'insertOrderedList' },
  { name: 'left', label: 'Left', command: 'justifyLeft' },
  { name: 'center', label: 'Center', command: 'justifyCenter' },
  { name: 'right', label: 'Right', command: 'justifyRight' },
  { name: 'enter', label: '↵', command: 'insertHTML', arg: '<br>' },
];

function exec(item: ToolbarItem) {
  if (!editorRef.value) return;
  editorRef.value.focus();
  try {
    if (item.arg) {
      // eslint-disable-next-line @typescript-eslint/no-deprecated
      document.execCommand(item.command, false, item.arg);
    } else {
      // eslint-disable-next-line @typescript-eslint/no-deprecated
      document.execCommand(item.command, false, undefined);
    }
    content.value = editorRef.value.innerHTML;
  } catch (e) {
    console.error('execCommand failed:', e);
  }
}

function onEditorInput(e: Event) {
  const target = e.target as HTMLDivElement;
  content.value = target.innerHTML;
}

function pickImage() {
  fileInputRef.value?.click();
}

async function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  // 简单校验
  if (!file.type.startsWith('image/')) {
    notification.warning({ content: '请选择图片文件', duration: 2000 });
    target.value = '';
    return;
  }
  if (file.size > 5 * 1024 * 1024) {
    notification.warning({ content: '图片大小不能超过 5MB', duration: 2000 });
    target.value = '';
    return;
  }

  uploading.value = true;
  try {
    const result = await uploadFileStorage({ file, fileType: 'editor' });
    if (result?.url) {
      insertImage(result.url);
      notification.success({ content: '图片上传成功', duration: 1500 });
    } else {
      notification.error({ content: '上传成功但未返回图片地址', duration: 2000 });
    }
  } catch (err: any) {
    console.error('图片上传失败:', err);
  } finally {
    uploading.value = false;
    target.value = ''; // 允许重复选择同一文件
  }
}

function insertImage(url: string) {
  if (!editorRef.value) return;
  editorRef.value.focus();
  const img = `<img src="${url}" alt="图片" style="max-width: 100%; height: auto;" /><p><br></p>`;
  // eslint-disable-next-line @typescript-eslint/no-deprecated
  document.execCommand('insertHTML', false, img);
  content.value = editorRef.value.innerHTML;
}

function getContent() {
  notification.success({ content: '已获取内容到剪贴板', duration: 1500 });
  navigator.clipboard.writeText(content.value).catch(() => {
    notification.warning({ content: '请手动复制', duration: 2000 });
  });
}

function clearContent() {
  content.value = '';
  if (editorRef.value) {
    editorRef.value.innerHTML = '';
  }
}
</script>

<template>
  <div class="p-4">
    <NCard title="富文本编辑器">
      <template #header-extra>
        <NSpace>
          <NButton size="small" @click="getContent">
            <template #icon>
              <NIcon color="#18a058"><CopyIcon /></NIcon>
            </template>
            获取内容
          </NButton>
          <NButton size="small" type="error" @click="clearContent">
            <template #icon>
              <NIcon color="#d03050"><EraserIcon /></NIcon>
            </template>
            清空
          </NButton>
        </NSpace>
      </template>

      <p class="mb-3 text-sm text-gray-500">
        富文本基于
        <a href="https://www.kancloud.cn/wangfupeng/wangeditor3/332599" target="_blank" class="text-blue-500">wangEditor</a>
      </p>

      <NGrid :cols="2" :x-gap="16">
        <NGi>
          <div class="text-sm text-gray-500 mb-2">编辑区</div>
          <div class="editor-wrapper border rounded">
            <!-- 文字工具栏 -->
            <div class="toolbar flex flex-wrap items-center gap-1 border-b p-2 rounded-t" style="background-color: #f5f7fa;">
              <NButton
                v-for="item in toolbarItems"
                :key="item.name"
                size="small"
                quaternary
                :title="item.label"
                @click="exec(item)"
              >
                {{ item.label }}
              </NButton>

              <!-- 图片上传 -->
              <NButton
                size="small"
                quaternary
                :loading="uploading"
                title="插入图片"
                @click="pickImage"
              >
                🖼
              </NButton>

              <!-- 隐藏的文件选择器 -->
              <input
                ref="fileInputRef"
                type="file"
                accept="image/*"
                style="display: none"
                @change="onFileChange"
              />
            </div>
            <div
              ref="editorRef"
              class="editor-area"
              contenteditable="true"
              @input="onEditorInput"
              v-html="content"
            ></div>
          </div>
        </NGi>
        <NGi>
          <div class="text-sm text-gray-500 mb-2">预览</div>
          <div
            class="preview-wrapper p-3 border rounded min-h-[400px] bg-white"
            v-html="content"
          ></div>
        </NGi>
      </NGrid>
    </NCard>
  </div>
</template>

<style scoped>
:deep(.n-card) {
  border-radius: 8px;
}

.editor-wrapper {
  background: #fff;
  overflow: hidden;
}

/* 编辑区：显式深色文字 + 白底 + 可点击光标 */
.editor-area {
  min-height: 400px;
  padding: 16px;
  outline: none;
  background: #ffffff;
  color: #333639;
  font-size: 14px;
  line-height: 1.7;
  cursor: text;
  user-select: text;
}

.editor-area:focus {
  background: #ffffff;
  outline: none;
}

.editor-area:empty::before {
  content: attr(data-placeholder);
  color: #999;
  pointer-events: none;
}

/* 编辑区内的标题、段落、链接样式，避免被全局 reset 影响 */
.editor-area :deep(h1),
.editor-area :deep(h2),
.editor-area :deep(h3) {
  color: #1f2329;
  margin: 12px 0 8px;
  font-weight: 600;
}

.editor-area :deep(p) {
  margin: 8px 0;
  color: #333639;
}

.editor-area :deep(a) {
  color: #18a058;
  text-decoration: underline;
}

.editor-area :deep(ul),
.editor-area :deep(ol) {
  padding-left: 24px;
  margin: 8px 0;
}

.editor-area :deep(li) {
  margin: 4px 0;
  color: #333639;
}

.editor-area :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 8px 0;
  border-radius: 4px;
}

.preview-wrapper {
  word-break: break-word;
  color: #333639;
  background: #fff;
}

.preview-wrapper :deep(h1),
.preview-wrapper :deep(h2),
.preview-wrapper :deep(h3) {
  color: #1f2329;
  margin: 12px 0 8px;
}

.preview-wrapper :deep(a) {
  color: #18a058;
}

.preview-wrapper :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 8px 0;
  border-radius: 4px;
}

/* 工具栏文字按钮：显式深色 + hover 主题色 */
.toolbar :deep(.n-button) {
  color: #333639;
  font-weight: 600;
  min-width: 36px;
}

.toolbar :deep(.n-button:hover) {
  color: #18a058;
  background-color: rgba(24, 160, 88, 0.08);
}
</style>
