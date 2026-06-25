<script setup lang="ts">
import { ref } from 'vue'
import { requestEmailCode, updateEmail } from '@/api/user'

const email = ref('')
const code = ref('')
const sending = ref(false)
const submitting = ref(false)
const countdown = ref(0)

async function onSendCode() {
  if (!email.value || !/^.+@.+$/.test(email.value)) {
    uni.showToast({ title: '邮箱格式错误', icon: 'none' })
    return
  }
  sending.value = true
  try {
    await requestEmailCode(email.value)
    uni.showToast({ title: '已发送', icon: 'success' })
    countdown.value = 60
    const t = setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) clearInterval(t)
    }, 1000)
  } finally {
    sending.value = false
  }
}

async function onSubmit() {
  if (!email.value || !code.value) {
    uni.showToast({ title: '请填写完整', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await updateEmail({ email: email.value, code: code.value })
    uni.showToast({ title: '修改成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 500)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="page">
    <view class="form">
      <view class="field">
        <text class="lbl">新邮箱</text>
        <input v-model="email" class="ipt" />
      </view>
      <view class="field">
        <text class="lbl">验证码</text>
        <input v-model="code" class="ipt" />
        <button size="mini" :disabled="countdown > 0" @tap="onSendCode">
          {{ countdown > 0 ? `${countdown}s` : '发送' }}
        </button>
      </view>
    </view>
    <button class="submit" :loading="submitting" @tap="onSubmit">提交</button>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 24rpx;
}
.form {
  background: #fff;
  border-radius: 16rpx;
  padding: 0 24rpx;
  .field {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
    .lbl {
      width: 160rpx;
      color: #1f2329;
    }
    .ipt {
      flex: 1;
    }
  }
}
.submit {
  margin-top: 40rpx;
  background: $uni-color-primary;
  color: #fff;
  border-radius: 50rpx;
  height: 88rpx;
  line-height: 88rpx;
}
</style>