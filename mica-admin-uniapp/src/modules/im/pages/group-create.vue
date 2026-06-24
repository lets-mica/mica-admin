<script setup lang="ts">
import { ref, computed } from 'vue'
import UserPicker from '../components/UserPicker.vue'
import { useGroupStore } from '../stores/group'
import { useImStore } from '../stores/im'
import type { ImUserVo } from '@/types/im'

const group = useGroupStore()
const im = useImStore()

const name = ref('')
const picked = ref<ImUserVo[]>([])
const showPicker = ref(false)
const submitting = ref(false)

const canSubmit = computed(() => name.value.trim() && picked.value.length >= 2)

function onPickConfirm(users: ImUserVo[]) {
  picked.value = users
}

async function onCreate() {
  if (!canSubmit.value) {
    uni.showToast({ title: '请填写群名并至少选 2 人', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    const g = await group.create({
      name: name.value.trim(),
      memberIds: picked.value.map((u) => u.userId)
    })
    await im.loadMyGroups()
    uni.redirectTo({
      url: `/modules/im/pages/chat-window?type=group&groupId=${g.id}`
    })
  } catch (e: any) {
    uni.showToast({ title: e.message || '创建失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="page">
    <view class="form">
      <view class="field">
        <text class="lbl">群名称</text>
        <input v-model="name" class="ipt" placeholder="例如:研发一组" />
      </view>
    </view>

    <view class="form">
      <view class="row-title">
        <text>群成员 ({{ picked.length }})</text>
        <text class="add" @tap="showPicker = true">+ 添加</text>
      </view>
      <view class="chips">
        <view v-for="u in picked" :key="u.userId" class="chip">
          <text>{{ u.nickname }}</text>
          <text class="x" @tap="picked = picked.filter(p => p.userId !== u.userId)">×</text>
        </view>
        <view v-if="picked.length === 0" class="hint">
          <text>至少选择 2 人(自动算上自己)</text>
        </view>
      </view>
    </view>

    <button class="submit" :loading="submitting" :disabled="!canSubmit" @tap="onCreate">创建</button>

    <UserPicker v-model="showPicker" title="添加成员" :excludeIds="picked.map(p => p.userId)" @confirm="onPickConfirm" />
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 24rpx;
}
.form {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  overflow: hidden;
}
.field {
  display: flex;
  align-items: center;
  padding: 24rpx;
  .lbl {
    width: 160rpx;
    color: #1f2329;
  }
  .ipt {
    flex: 1;
  }
}
.row-title {
  display: flex;
  justify-content: space-between;
  padding: 24rpx;
  font-weight: 600;
  border-bottom: 1rpx solid #f0f0f0;
  .add {
    color: $uni-color-primary;
    font-weight: 400;
  }
}
.chips {
  padding: 20rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  .chip {
    display: flex;
    align-items: center;
    background: #f0faf5;
    color: $uni-color-primary;
    padding: 8rpx 20rpx;
    border-radius: 30rpx;
    font-size: 26rpx;
    .x {
      margin-left: 8rpx;
    }
  }
  .hint {
    color: #8f959e;
    font-size: 24rpx;
    padding: 8rpx 20rpx;
  }
}
.submit {
  margin-top: 40rpx;
  background: $uni-color-primary;
  color: #fff;
  border-radius: 50rpx;
  height: 88rpx;
  line-height: 88rpx;
  &[disabled] {
    opacity: 0.5;
  }
}
</style>