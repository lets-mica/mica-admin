<script setup lang="ts">
/**
 * 审批中心占位页(二次开发方按需替换)
 * 详见 docs/app/extension.md §4
 */
</script>

<template>
  <view class="placeholder">
    <text class="emoji">📋</text>
    <text class="title">审批中心</text>
    <text class="desc">需新增 sys_approval 表 + 流程引擎</text>
    <text class="hint">详见 docs/app/extension.md §4</text>
  </view>
</template>

<style lang="scss" scoped>
.placeholder {
  text-align: center;
  padding: 200rpx 40rpx;
  .emoji {
    font-size: 120rpx;
  }
  .title {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
    margin-top: 20rpx;
  }
  .desc {
    display: block;
    color: #8f959e;
    margin-top: 12rpx;
    font-size: 28rpx;
  }
  .hint {
    display: block;
    color: #c0c4cc;
    margin-top: 12rpx;
    font-size: 24rpx;
  }
}
</style>