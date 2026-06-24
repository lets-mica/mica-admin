<script setup lang="ts">
/**
 * IM 选人组件(单选 / 多选)
 * 复用:发起聊天、建群邀请、群管理邀请
 */
import { ref, computed } from 'vue'
import { searchImUsers } from '@/api/im/user'
import type { ImUserVo } from '@/types/im'

const props = defineProps<{
  modelValue: boolean
  multiple?: boolean
  excludeIds?: number[]
  title?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [v: boolean]
  confirm: [users: ImUserVo[]]
}>()

const keyword = ref('')
const users = ref<ImUserVo[]>([])
const selected = ref<Set<number>>(new Set())
const loading = ref(false)

const isMultiple = computed(() => props.multiple !== false)

async function search() {
  if (!keyword.value.trim()) {
    users.value = []
    return
  }
  loading.value = true
  try {
    const list = await searchImUsers(keyword.value, 30)
    users.value = (list || []).filter((u) => !(props.excludeIds || []).includes(u.userId))
  } finally {
    loading.value = false
  }
}

function toggle(u: ImUserVo) {
  if (isMultiple.value) {
    if (selected.value.has(u.userId)) selected.value.delete(u.userId)
    else selected.value.add(u.userId)
  } else {
    selected.value.clear()
    selected.value.add(u.userId)
    confirm()
  }
}

function confirm() {
  const ids = [...selected.value]
  const picked = users.value.filter((u) => ids.includes(u.userId))
  emit('confirm', picked)
  close()
}

function close() {
  emit('update:modelValue', false)
  keyword.value = ''
  users.value = []
  selected.value.clear()
}

function onPopupChange(e: { show: boolean }) {
  if (!e.show) close()
}
</script>

<template>
  <uni-popup :show="modelValue" position="bottom" @change="onPopupChange">
    <view class="picker">
      <view class="head">
        <text class="cancel" @tap="close">取消</text>
        <text class="title">{{ title || '选择联系人' }}</text>
        <text class="ok" @tap="confirm">确定 ({{ selected.size }})</text>
      </view>
      <uni-search-bar v-model="keyword" placeholder="搜索姓名/工号" @input="search" />
      <scroll-view scroll-y class="list">
        <view
          v-for="u in users"
          :key="u.userId"
          class="row"
          :class="{ active: selected.has(u.userId) }"
          @tap="toggle(u)"
        >
          <image class="avatar" :src="u.avatar || '/static/default-avatar.png'" />
          <view class="info">
            <text class="name">{{ u.nickname }}</text>
            <text class="dept">{{ u.deptName }}</text>
          </view>
          <view v-if="isMultiple" class="check">
            <text v-if="selected.has(u.userId)">✓</text>
          </view>
        </view>
        <view v-if="!loading && users.length === 0" class="empty">
          <text>输入关键字搜索</text>
        </view>
      </scroll-view>
    </view>
  </uni-popup>
</template>

<style lang="scss" scoped>
.picker {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  height: 80vh;
  display: flex;
  flex-direction: column;
}
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  .cancel,
  .ok {
    color: $uni-color-primary;
    font-size: 28rpx;
  }
  .title {
    font-size: 32rpx;
    font-weight: 600;
  }
}
.list {
  flex: 1;
}
.row {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  &.active {
    background: #f0faf5;
  }
  .avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: #e8e8e8;
  }
  .info {
    flex: 1;
    margin-left: 20rpx;
  }
  .name {
    display: block;
    font-size: 28rpx;
  }
  .dept {
    display: block;
    color: #8f959e;
    font-size: 24rpx;
    margin-top: 4rpx;
  }
  .check {
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    border: 2rpx solid #ccc;
    text-align: center;
    line-height: 36rpx;
    color: #fff;
    .row.active & {
      background: $uni-color-primary;
      border-color: $uni-color-primary;
    }
  }
}
.empty {
  text-align: center;
  color: #8f959e;
  padding: 80rpx 0;
}
</style>