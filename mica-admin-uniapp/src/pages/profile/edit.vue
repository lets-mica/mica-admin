<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { http, getToken } from '@/utils/request'
import { env } from '@/config/env'

/**
 * /api/auth/info 响应结构(与 mica-admin 后端 JwtUser 对齐)
 * 后端返回:{ userInfo, publicKey }
 */
interface AuthInfoUserInfo {
  id?: number
  userName?: string
  nickName?: string
  gender?: number
  avatar?: string
  email?: string
  phone?: string
  isAdmin?: boolean
}
interface AuthInfoResp {
  userInfo?: AuthInfoUserInfo
  publicKey?: string
}

const auth = useAuthStore()
const submitting = ref(false)
const uploading = ref(false)

type Gender = 0 | 1 | 2

const form = reactive({
  nickName: '',
  gender: 0 as Gender,
  email: '',
  phone: ''
})

const avatar = ref('')

const genderOptions: { label: string; value: Gender }[] = [
  { label: '男', value: 0 },
  { label: '女', value: 1 },
  { label: '未知', value: 2 }
]

async function loadUserInfo() {
  try {
    const res = await http.get<AuthInfoResp>('/api/auth/info')
    const u = res?.userInfo || {}
    form.nickName = u.nickName || ''
    form.gender = (u.gender ?? 0) as Gender
    form.email = u.email || ''
    form.phone = u.phone || ''
    avatar.value = u.avatar || auth.user?.avatar || ''
  } catch {
    form.nickName = auth.user?.nickname || ''
    avatar.value = auth.user?.avatar || ''
  }
}

onShow(() => {
  loadUserInfo()
})

function uploadAvatarDirect(filePath: string): Promise<{ avatar: string }> {
  return new Promise((resolve, reject) => {
    const token = getToken()
    uni.uploadFile({
      url: `${env.apiUrl}/api/system/users/avatar`,
      filePath,
      name: 'file',
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success: (res) => {
        try {
          const body = JSON.parse(res.data) as {
            code: number
            msg?: string
            data?: { avatar?: string }
          }
          if (body.code === 0 && body.data?.avatar) {
            resolve({ avatar: body.data.avatar })
          } else {
            reject(new Error(body.msg || '上传失败'))
          }
        } catch (e) {
          reject(e as Error)
        }
      },
      fail: (err) => reject(err as unknown as Error)
    })
  })
}

function onPickAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const path = res.tempFilePaths[0]
      if (!path) return
      uploading.value = true
      uni.showLoading({ title: '上传中...' })
      try {
        const data = await uploadAvatarDirect(path)
        const url = data.avatar
        avatar.value = url
        if (auth.user) {
          auth.user.avatar = url
        }
        uni.showToast({ title: '头像已更新', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: (e as Error).message || '上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
        uploading.value = false
      }
    }
  })
}

function onClear(key: 'nickName' | 'email' | 'phone') {
  form[key] = ''
}

function onSelectGender(value: Gender) {
  form.gender = value
}

async function onSave() {
  if (!form.nickName.trim()) {
    uni.showToast({ title: '请输入昵称', icon: 'none' })
    return
  }
  if (form.email && !/^[\w.+-]+@[\w-]+(\.[\w-]+)+$/.test(form.email)) {
    uni.showToast({ title: '邮箱格式不正确', icon: 'none' })
    return
  }
  if (form.phone && !/^1[3-9]\d{9}$/.test(form.phone)) {
    uni.showToast({ title: '手机号格式不正确', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await http.put<void>('/api/system/users/center', {
      nickName: form.nickName,
      gender: form.gender,
      phone: form.phone
    })
    if (auth.user) {
      auth.user.nickname = form.nickName
      auth.user.phone = form.phone
      auth.user.email = form.email
    }
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 600)
  } catch (e) {
    uni.showToast({ title: (e as Error).message || '保存失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="page">
    <view class="avatar-block">
      <image
        class="avatar"
        :src="avatar || '/static/default-avatar.png'"
        mode="aspectFill"
      />
      <button
        class="pick-btn"
        :loading="uploading"
        :disabled="uploading"
        @tap="onPickAvatar"
      >
        选择头像
      </button>
    </view>

    <view class="form-card">
      <view class="field">
        <text class="lbl">昵称</text>
        <view class="control">
          <input
            v-model="form.nickName"
            class="ipt"
            placeholder="请输入昵称"
            maxlength="20"
          />
          <view v-if="form.nickName" class="clear" @tap="onClear('nickName')">
            <text class="clear-icon">×</text>
          </view>
        </view>
      </view>

      <view class="field">
        <text class="lbl">性别</text>
        <view class="gender-group">
          <view
            v-for="opt in genderOptions"
            :key="opt.value"
            class="radio"
            :class="{ active: form.gender === opt.value }"
            @tap="onSelectGender(opt.value)"
          >
            <view class="radio-dot" />
            <text class="radio-text">{{ opt.label }}</text>
          </view>
        </view>
      </view>

      <view class="field">
        <text class="lbl">邮箱</text>
        <view class="control">
          <input
            v-model="form.email"
            class="ipt"
            placeholder="请输入邮箱"
            type="text"
          />
          <view v-if="form.email" class="clear" @tap="onClear('email')">
            <text class="clear-icon">×</text>
          </view>
        </view>
      </view>

      <view class="field field-last">
        <text class="lbl">手机</text>
        <view class="control">
          <input
            v-model="form.phone"
            class="ipt"
            placeholder="请输入手机号"
            type="number"
            maxlength="11"
          />
          <view v-if="form.phone" class="clear" @tap="onClear('phone')">
            <text class="clear-icon">×</text>
          </view>
        </view>
      </view>
    </view>

    <view class="safe-area" />

    <view class="footer">
      <button
        class="save-btn"
        :loading="submitting"
        :disabled="submitting"
        @tap="onSave"
      >
        保存
      </button>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f6f8;
  padding-bottom: 200rpx;
}

.avatar-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0 50rpx;

  .avatar {
    width: 180rpx;
    height: 180rpx;
    border-radius: 50%;
    background: #e8e8e8;
    border: 4rpx solid #fff;
    box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.08);
  }

  .pick-btn {
    margin-top: 30rpx;
    background: $uni-color-primary;
    color: #fff;
    border-radius: 40rpx;
    font-size: 28rpx;
    height: 64rpx;
    line-height: 64rpx;
    padding: 0 40rpx;
    box-shadow: 0 4rpx 12rpx rgba(24, 163, 126, 0.25);
  }
}

.form-card {
  margin: 0 24rpx;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.field {
  display: flex;
  align-items: center;
  padding: 28rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  min-height: 96rpx;

  &.field-last {
    border-bottom: none;
  }

  .lbl {
    width: 120rpx;
    color: #1f2329;
    font-size: 30rpx;
    flex-shrink: 0;
  }

  .control {
    flex: 1;
    display: flex;
    align-items: center;
    min-width: 0;
  }

  .ipt {
    flex: 1;
    font-size: 30rpx;
    color: #1f2329;
    text-align: left;
    min-width: 0;
  }

  .clear {
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    background: #c8c9cc;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-left: 12rpx;
    flex-shrink: 0;
  }

  .clear-icon {
    color: #fff;
    font-size: 24rpx;
    line-height: 1;
  }
}

.gender-group {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 50rpx;
}

.radio {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 6rpx 0;

  .radio-dot {
    width: 36rpx;
    height: 36rpx;
    border-radius: 50%;
    border: 2rpx solid #c8c9cc;
    background: #fff;
    position: relative;
    flex-shrink: 0;
  }

  .radio-text {
    font-size: 30rpx;
    color: #1f2329;
  }

  &.active {
    .radio-dot {
      border-color: $uni-color-primary;
      background: $uni-color-primary;

      &::after {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%) rotate(45deg);
        width: 12rpx;
        height: 22rpx;
        border-right: 3rpx solid #fff;
        border-bottom: 3rpx solid #fff;
      }
    }

    .radio-text {
      color: $uni-color-primary;
    }
  }
}

.safe-area {
  height: 60rpx;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.04);
  z-index: 10;
}

.save-btn {
  background: $uni-color-primary;
  color: #fff;
  border-radius: 50rpx;
  font-size: 32rpx;
  height: 88rpx;
  line-height: 88rpx;
}
</style>
