<script setup lang="ts">
import { ref } from 'vue'
import { updatePass } from '@/api/user'
import { encryptRSA, getPublicKey } from '@/utils/rsa'

const oldPassword = ref('')
const newPassword = ref('')
const confirm = ref('')
const submitting = ref(false)

async function onSubmit() {
  if (!oldPassword.value || !newPassword.value) {
    uni.showToast({ title: '请填写完整', icon: 'none' })
    return
  }
  if (newPassword.value !== confirm.value) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    const pub = await getPublicKey()
    await updatePass({
      oldPassword: encryptRSA(oldPassword.value, pub),
      newPassword: encryptRSA(newPassword.value, pub)
    })
    uni.showToast({ title: '修改成功,请重新登录', icon: 'success' })
    setTimeout(() => {
      uni.reLaunch({ url: '/modules/auth/pages/login' })
    }, 1000)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="page">
    <view class="form">
      <view class="field">
        <text class="lbl">原密码</text>
        <input v-model="oldPassword" type="password" password class="ipt" />
      </view>
      <view class="field">
        <text class="lbl">新密码</text>
        <input v-model="newPassword" type="password" password class="ipt" />
      </view>
      <view class="field">
        <text class="lbl">确认密码</text>
        <input v-model="confirm" type="password" password class="ipt" />
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