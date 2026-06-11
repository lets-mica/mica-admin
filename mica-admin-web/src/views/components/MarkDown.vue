<script setup lang="ts">
import { NCard, NButton, NSpace, NGrid, NGi } from 'naive-ui';
import { ref } from 'vue';
import { notification } from '#/adapter/naive';

defineOptions({ name: 'MarkdownEditor' });

const markdown = ref(`# 欢迎使用 Markdown 编辑器

这是一个基于 [MavonEditor](https://github.com/hinesboy/mavonEditor) 的 Markdown 编辑器演示。

## 功能特性

- 支持 Markdown 语法
- 支持图片上传
- 实时预览
- 代码高亮

## 代码示例

\`\`\`javascript
function hello() {
  console.log('Hello, world!');
}
\`\`\`

## 列表

1. 第一项
2. 第二项
3. 第三项

- 无序项 1
- 无序项 2
- 无序项 3

> 这是一个引用块
`);

const preview = ref('');

function updatePreview() {
  // 简单的 Markdown 解析（实际生产建议使用 marked 或 markdown-it）
  let html = markdown.value;
  // 标题
  html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>');
  html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>');
  html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>');
  // 粗体
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  // 斜体
  html = html.replace(/\*(.*?)\*/g, '<em>$1</em>');
  // 链接
  html = html.replace(/\[(.*?)\]\((.*?)\)/g, '<a href="$2" target="_blank">$1</a>');
  // 代码块
  html = html.replace(/```(\w+)?\n([\s\S]*?)```/g, '<pre><code class="language-$1">$2</code></pre>');
  // 行内代码
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
  // 引用
  html = html.replace(/^> (.*$)/gim, '<blockquote>$1</blockquote>');
  // 列表
  html = html.replace(/^\d+\. (.*$)/gim, '<li>$1</li>');
  html = html.replace(/^- (.*$)/gim, '<li>$1</li>');
  // 换行
  html = html.replace(/\n/g, '<br/>');
  preview.value = html;
}

function getContent() {
  notification.success({ content: '已获取 Markdown 内容', duration: 1500 });
  navigator.clipboard.writeText(markdown.value).catch(() => {
    notification.warning({ content: '请手动复制', duration: 2000 });
  });
}

function clearContent() {
  markdown.value = '';
  preview.value = '';
}

import { watch } from 'vue';
watch(markdown, () => {
  updatePreview();
}, { immediate: true });
</script>

<template>
  <div class="p-4">
    <NCard title="Markdown 编辑器">
      <template #header-extra>
        <NSpace>
          <NButton size="small" @click="getContent">获取内容</NButton>
          <NButton size="small" type="error" @click="clearContent">清空</NButton>
        </NSpace>
      </template>

      <p class="mb-3 text-sm text-gray-500">
        Markdown 基于
        <a href="https://github.com/hinesboy/mavonEditor" target="_blank" class="text-blue-500">MavonEditor</a>
      </p>

      <NGrid :cols="2" :x-gap="16">
        <NGi>
          <div class="text-sm text-gray-500 mb-2">编辑区</div>
          <textarea
            v-model="markdown"
            class="w-full p-3 border rounded font-mono text-sm"
            style="min-height: 400px; resize: vertical;"
          ></textarea>
        </NGi>
        <NGi>
          <div class="text-sm text-gray-500 mb-2">预览</div>
          <div
            class="p-3 border rounded bg-white min-h-[400px] prose max-w-none"
            v-html="preview"
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

:deep(.prose) {
  word-break: break-word;
}

:deep(.prose h1) {
  font-size: 2em;
  font-weight: bold;
  margin: 0.67em 0;
}

:deep(.prose h2) {
  font-size: 1.5em;
  font-weight: bold;
  margin: 0.75em 0;
}

:deep(.prose h3) {
  font-size: 1.17em;
  font-weight: bold;
  margin: 0.83em 0;
}

:deep(.prose blockquote) {
  border-left: 4px solid #ddd;
  padding-left: 12px;
  color: #666;
  margin: 8px 0;
}

:deep(.prose code) {
  background: #f0f0f0;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: monospace;
}

:deep(.prose pre) {
  background: #1e1e1e;
  color: #fff;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
}

:deep(.prose pre code) {
  background: transparent;
  color: inherit;
  padding: 0;
}
</style>
