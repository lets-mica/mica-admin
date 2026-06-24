<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDictItems, getDictTypes } from '@/api/dict'
import type { DictType, DictItem } from '@/api/dict'

const types = ref<DictType[]>([])
const items = ref<DictItem[]>([])
const currentType = ref<string>('')

async function loadTypes() {
  types.value = await getDictTypes()
  if (types.value.length > 0) {
    currentType.value = types.value[0].type
    loadItems()
  }
}

async function loadItems() {
  if (!currentType.value) return
  items.value = await getDictItems(currentType.value)
}

onMounted(loadTypes)
</script>

<template>
  <view class="page">
    <view class="types">
      <scroll-view scroll-x class="scroll">
        <view
          v-for="t in types"
          :key="t.type"
          class="tag"
          :class="{ active: currentType === t.type }"
          @tap="currentType = t.type; loadItems()"
        >
          {{ t.type }}
        </view>
      </scroll-view>
    </view>

    <view class="items">
      <view v-for="i in items" :key="i.id" class="row">
        <text class="label">{{ i.label }}</text>
        <text class="value">{{ i.value }}</text>
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
}
</style>