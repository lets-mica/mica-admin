<script setup lang="ts">
/**
 * IM 模块用户选择器
 *
 * 功能：
 *   - 按用户名 / 昵称模糊搜索用户（debounce 300ms）
 *   - 单选 / 多选模式（multiple prop）
 *   - 已选项显示为 chip，可移除
 *   - 排除当前用户自己（后端也会过滤，这里再加一层防御）
 *
 * 父组件使用：
 *   <UserPicker v-model="userIds" :multiple="true" />
 */
import { computed, onMounted, ref, watch } from 'vue';

import { NAvatar, NCheckbox, NEmpty, NInput, NSpin, NTag } from 'naive-ui';
import { Search, X } from '@lucide/vue';

import type { ImUserItem } from '#/api/im/user';
import { searchUsers } from '#/api/im/user';

const props = defineProps<{
  modelValue: number[] | number | null;
  multiple?: boolean;
  excludeIds?: number[];
  placeholder?: string;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: number[] | number | null): void;
}>();

const keyword = ref('');
const loading = ref(false);
const userMap = ref<Record<number, ImUserItem>>({});
const searchResults = ref<ImUserItem[]>([]);

let debounceTimer: number | null = null;

function debounceSearch(kw: string) {
  if (debounceTimer != null) {
    clearTimeout(debounceTimer);
  }
  debounceTimer = window.setTimeout(async () => {
    loading.value = true;
    try {
      const list = await searchUsers({ keyword: kw, limit: 30 });
      searchResults.value = list ?? [];
      for (const u of searchResults.value) userMap.value[u.id] = u;
    } catch (e) {
      searchResults.value = [];
    } finally {
      loading.value = false;
    }
  }, 300);
}

onMounted(() => {
  // 初次加载：拉取一批活跃用户，方便没输入时也能看到可选列表
  debounceSearch('');
});

// watch keyword
watch(keyword, (val) => debounceSearch(val ?? ''));

// 已选 id 列表（统一为数组便于处理）
const selectedIds = computed<number[]>(() => {
  if (props.multiple) {
    return Array.isArray(props.modelValue) ? props.modelValue : [];
  }
  if (props.modelValue == null) return [];
  return [props.modelValue as number];
});

const selectedUsers = computed<ImUserItem[]>(() =>
  selectedIds.value
    .map((id) => userMap.value[id])
    .filter((u): u is ImUserItem => u != null),
);

function isExcluded(id: number): boolean {
  if (props.excludeIds && props.excludeIds.includes(id)) return true;
  return false;
}

function isSelected(id: number): boolean {
  return selectedIds.value.includes(id);
}

function toggleSelect(u: ImUserItem) {
  if (props.multiple) {
    const next = [...selectedIds.value];
    const idx = next.indexOf(u.id);
    if (idx >= 0) {
      next.splice(idx, 1);
    } else {
      next.push(u.id);
    }
    userMap.value[u.id] = u;
    emit('update:modelValue', next);
  } else {
    if (isSelected(u.id)) {
      emit('update:modelValue', null);
    } else {
      userMap.value[u.id] = u;
      emit('update:modelValue', u.id);
    }
  }
}

function removeSelected(id: number) {
  if (props.multiple) {
    const next = selectedIds.value.filter((x) => x !== id);
    emit('update:modelValue', next);
  } else {
    emit('update:modelValue', null);
  }
}

function avatarText(u: ImUserItem): string {
  const name = u.nickName || u.userName || '?';
  return name.slice(0, 1).toUpperCase();
}
</script>

<template>
  <div class="flex flex-col gap-3">
    <!-- 搜索框 -->
    <NInput
      v-model:value="keyword"
      :placeholder="props.placeholder ?? '搜索用户名或昵称'"
      clearable
    >
      <template #prefix>
        <Search class="h-4 w-4 text-gray-400" />
      </template>
    </NInput>

    <!-- 已选项 chip 区 -->
    <div v-if="selectedUsers.length > 0" class="flex flex-wrap gap-2 rounded border border-gray-100 bg-gray-50 p-2">
      <NTag
        v-for="u in selectedUsers"
        :key="u.id"
        closable
        :on-close="() => removeSelected(u.id)"
        type="primary"
        size="small"
        round
      >
        <template #default>
          <div class="flex items-center gap-1">
            <NAvatar round size="small" :src="u.avatar || undefined">
              {{ avatarText(u) }}
            </NAvatar>
            <span>{{ u.nickName || u.userName }}</span>
          </div>
        </template>
      </NTag>
    </div>

    <!-- 搜索结果列表 -->
    <div class="max-h-64 min-h-32 overflow-y-auto rounded border border-gray-100">
      <NSpin v-if="loading" class="block p-4 text-center">
        <span class="text-xs text-gray-400">加载中...</span>
      </NSpin>
      <div v-else-if="searchResults.length === 0" class="p-6">
        <NEmpty :description="keyword ? '未搜索到匹配的用户' : '暂无可选用户'" :show-icon="false" />
      </div>
      <div v-else class="divide-y divide-gray-50">
        <div
          v-for="u in searchResults"
          :key="u.id"
          class="flex cursor-pointer items-center gap-3 px-3 py-2 transition-colors hover:bg-gray-50"
          :class="{ 'bg-blue-50': isSelected(u.id), 'opacity-50': isExcluded(u.id) }"
          @click="!isExcluded(u.id) && toggleSelect(u)"
        >
          <NCheckbox
            v-if="props.multiple"
            :checked="isSelected(u.id)"
            :disabled="isExcluded(u.id)"
            @update:checked="() => !isExcluded(u.id) && toggleSelect(u)"
          />
          <NAvatar round size="small" :src="u.avatar || undefined">
            {{ avatarText(u) }}
          </NAvatar>
          <div class="min-w-0 flex-1">
            <div class="truncate text-sm font-medium text-gray-800">
              {{ u.nickName || u.userName }}
            </div>
            <div v-if="u.nickName" class="truncate text-xs text-gray-400">
              @{{ u.userName }}
            </div>
          </div>
          <span v-if="isSelected(u.id)" class="text-xs text-blue-500">已选</span>
          <span v-else-if="isExcluded(u.id)" class="text-xs text-gray-400">已加入</span>
        </div>
      </div>
    </div>

    <!-- 提示 -->
    <div v-if="selectedUsers.length > 0" class="text-xs text-gray-400">
      已选择 {{ selectedUsers.length }} 个用户
      <a v-if="selectedUsers.length > 0" class="ml-2 cursor-pointer text-blue-500 hover:underline" @click="props.multiple ? emit('update:modelValue', []) : emit('update:modelValue', null)">
        清空
      </a>
    </div>
  </div>
</template>