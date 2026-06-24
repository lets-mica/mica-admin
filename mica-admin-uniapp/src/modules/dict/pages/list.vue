<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDictItems, getDictTypes } from '@/api/dict'
import type { DictItem, DictType } from '@/api/dict'

const types = ref<DictType[]>([])
const items = ref<DictItem[]>([])
const currentType = ref<string>('')

async function loadTypes() {
  const p = await getDictTypes({ current: 1, size: 9999 })
  types.value = p.records || []
  if (types.value.length > 0) {
    currentType.value = types.value[0].name
    loadItems()
  }
}

async function loadItems() {
  if (!currentType.value) return
  const p = await getDictItems(currentType.value)
  items.value = p.records || []
}

onMounted(loadTypes)
</script>

<template>
  <view class="page">
    <view class="types">
      <scroll-view scroll-x class="scroll">
        <view
          v-for="t in types"
          :key="t.id"
          class="tag"
          :class="{ active: currentType === t.name }"
          @tap="currentType = t.name; loadItems()"
        >
          {{ t.name }}
        </view>
      </scroll-view>
    </view>

    <view class="items">
      <view v-for="i in items" :key="i.id" class="row">
        <text class="label">{{ i.label }}</text>
        <text class="value">{{ i.value }}</text>
      </view>
      <view v-if="items.length === 0" class="empty">
        <text>暂无数据</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 24rpx;
}
.types {
  margin-bottom: 20rpx;
}
.scroll {
  white-space: nowrap;
}
.tag {
  display: inline-block;
  padding: 12rpx 28rpx;
  margin-right: 16rpx;
  background: #fff;
  border-radius: 30rpx;
  font-size: 26rpx;
  color: #1f2329;
  &.active {
    background: $uni-color-primary;
    color: #fff;
  }
}
.items {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  .row {
    padding: 24rpx;
    display: flex;
    justify-content: space-between;
    border-bottom: 1rpx solid #f0f0f0;
    &:last-child {
      border-bottom: none;
    }
  }
  .label {
    color: #1f2329;
  }
  .value {
    color: #8f959e;
  }
  .empty {
    padding: 60rpx 0;
    text-align: center;
    color: #8f959e;
  }
}
</style>
