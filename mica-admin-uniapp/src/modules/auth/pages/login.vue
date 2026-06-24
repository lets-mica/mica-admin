<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const username = ref('')
const password = ref('')
const captchaCode = ref('')
const submitting = ref(false)
const captchaImage = ref('')

async function loadCaptcha() {
  const c = await auth.refreshCaptcha()
  captchaImage.value = c.captchaImage
}

async function onSubmit() {
  if (!username.value || !password.value || !captchaCode.value) {
    uni.showToast({ title: '请填写完整', icon: 'none' })
    return
  }
  if (!auth.captcha) {
    await loadCaptcha()
    return
  }
  submitting.value = true
  try {
    await auth.doLogin({
      username: username.value,
      password: password.value,
      validateCodeId: auth.captcha.captchaId,
      validateCode: captchaCode.value
    })
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/index/index' })
    }, 500)
  } catch {
    await loadCaptcha()
    captchaCode.value = ''
  } finally {
    submitting.value = false
  }
}

loadCaptcha()
</script>

<template>
  <view class="login">
    <view class="logo">
      <image src="/static/logo.png" class="logo-img" />
      <text class="title">MICA Work</text>
      <text class="subtitle">mica-admin 移动办公</text>
    </view>

    <view class="form">
      <uni-easyinput
        v-model="username"
        placeholder="账号"
        :input-border="false"
        prefix-icon="person"
      />
      <uni-easyinput
        v-model="password"
        type="password"
        placeholder="密码"
        :input-border="false"
        prefix-icon="locked"
      />
      <view class="captcha-row">
        <uni-easyinput
          v-model="captchaCode"
          placeholder="验证码"
          :input-border="false"
          prefix-icon="image"
          class="captcha-input"
        />
        <view class="captcha-img" @tap="loadCaptcha">
          <image
            v-if="captchaImage"
            :src="captchaImage"
            mode="aspectFit"
            class="captcha-image"
          />
          <text v-else>加载中…</text>
        </view>
      </view>
      <button class="submit" :loading="submitting" @tap="onSubmit">登录</button>
    </view>

    <view class="tips">
      <text>登录即代表同意《用户协议》《隐私政策》</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.login {
  padding: 80rpx 60rpx;
  min-height: 100vh;
  background: #fff;
}
.logo {
  text-align: center;
  margin-bottom: 80rpx;
  .logo-img {
    width: 120rpx;
    height: 120rpx;
  }
  .title {
    display: block;
    font-size: 44rpx;
    font-weight: 600;
    color: $uni-color-primary;
    margin-top: 20rpx;
  }
  .subtitle {
    display: block;
    color: #8f959e;
    font-size: 26rpx;
    margin-top: 8rpx;
  }
}
.form {
  background: #f7f8fa;
  border-radius: 16rpx;
  padding: 20rpx 30rpx;
  > view {
    border-bottom: 1rpx solid #ebebeb;
    &:last-of-type {
      border-bottom: none;
    }
  }
}
.captcha-row {
  display: flex;
  align-items: center;
  .captcha-input {
    flex: 1;
  }
  .captcha-img {
    width: 200rpx;
    height: 70rpx;
    background: #fff;
    border-radius: 8rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    letter-spacing: 4rpx;
    color: #1f2329;
    font-weight: 600;
    overflow: hidden;
  }
  .captcha-image {
    width: 100%;
    height: 100%;
  }
}
.submit {
  margin-top: 60rpx;
  background: $uni-color-primary;
  color: #fff;
  border-radius: 50rpx;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
}
.tips {
  text-align: center;
  margin-top: 40rpx;
  color: #8f959e;
  font-size: 24rpx;
}
</style>
