/**
 * 认证 store(全局单例)
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { clearToken, getToken, setToken } from '@/utils/request'
import { encryptRSA, getPublicKey } from '@/utils/rsa'
import { getCaptcha, getCurrentUser, getMenus, loginByPassword, logout as apiLogout } from '@/api/auth'
import type { CaptchaVo, LoginForm } from '@/api/auth'
import type { CurrentUser, MenuItem } from '@/types/common'

export const useAuthStore = defineStore(
  'auth',
  () => {
    const token = ref<string>('')
    const user = ref<CurrentUser | null>(null)
    const menus = ref<MenuItem[]>([])
    const captcha = ref<CaptchaVo | null>(null)
    const initialized = ref(false)

    const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => !!user.value?.isAdmin)

    async function refreshCaptcha() {
      captcha.value = await getCaptcha()
      return captcha.value
    }

    async function doLogin(form: Omit<LoginForm, 'password'> & { password: string }) {
      // 1. 取 RSA 公钥并加密密码
      const pubKey = await getPublicKey()
      const encrypted = encryptRSA(form.password, pubKey)
      // 2. form-urlencoded 提交,字段名与后端 SecWebAuthenticationDetails 对齐
      const { token: t } = await loginByPassword({
        username: form.username,
        password: encrypted,
        validateCodeId: form.validateCodeId,
        validateCode: form.validateCode
      })
      token.value = t
      setToken(t)
      // 3. 拉用户信息 + 菜单
      await fetchProfile()
    }

    async function fetchProfile() {
      const [u, m] = await Promise.all([getCurrentUser(), getMenus()])
      user.value = u
      menus.value = m || []
    }

    async function doLogout() {
      try {
        await apiLogout()
      } catch {
        // 忽略服务端错误,本地强制清
      }
      token.value = ''
      user.value = null
      menus.value = []
      clearToken()
    }

    /** App 启动时调用,恢复登录态 */
    async function bootstrap() {
      if (initialized.value) return
      initialized.value = true
      const t = getToken()
      if (!t) return
      token.value = t
      try {
        await fetchProfile()
      } catch {
        // token 失效 → 清
        await doLogout()
      }
    }

    return {
      token,
      user,
      menus,
      captcha,
      initialized,
      isLoggedIn,
      isAdmin,
      refreshCaptcha,
      doLogin,
      fetchProfile,
      doLogout,
      bootstrap
    }
  },
  {
    persist: {
      pick: ['token', 'user']
    }
  }
)