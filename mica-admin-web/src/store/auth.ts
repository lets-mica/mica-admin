import type { Recordable, UserInfo } from '@vben/types';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';
import { resetAllStores, useAccessStore, useUserStore } from '@vben/stores';

import { defineStore } from 'pinia';

import { notification } from '#/adapter/naive';
import { loginApi, logoutApi } from '#/api/core/auth';
import { getUserInfoApi } from '#/api/core/user';
import { $t } from '#/locales';

export const useAuthStore = defineStore('auth', () => {
  const accessStore = useAccessStore();
  const userStore = useUserStore();
  const router = useRouter();

  const loginLoading = ref(false);

  /**
   * mica-admin 后端登录流程
   * 1. 获取 RSA 公钥 2. 加密密码 3. 提交登录 4. 拉取用户信息
   */
  async function authLogin(
    params: Recordable<any>,
    onSuccess?: () => Promise<void> | void,
  ) {
    let userInfo: null | UserInfo = null;

    try {
      loginLoading.value = true;

      const result = await loginApi({
        username: params.username || '',
        password: params.password || '',
        validateCodeId: params.validateCodeId || '',
        validateCode: params.validateCode || '',
      });

      if (result.accessToken) {
        // 存储 token
        accessStore.setAccessToken(result.accessToken);

        // 存储用户信息
        if (result.userInfo) {
          userInfo = {
            id: result.userInfo.id,
            username: result.userInfo.userName,
            realName: result.userInfo.nickName,
            avatar: result.userInfo.avatar,
            email: result.userInfo.email,
            roles: result.userInfo.roleList || [],
          } as UserInfo;
          userStore.setUserInfo(userInfo);
        }

        // 跳转到首页
        await router.push(preferences.app.defaultHomePath);

        notification.success({
          content: $t('authentication.loginSuccess'),
          description: `${userInfo?.realName || '用户'}，欢迎回来！`,
          duration: 3000,
        });
      }
    } catch (error: any) {
      console.error('[Auth] Login error:', error);
      // 错误已经在 API 层显示
    } finally {
      loginLoading.value = false;
    }

    return { userInfo };
  }

  async function logout(redirect: boolean = true) {
    try {
      await logoutApi();
    } catch {
      // 忽略错误
    }
    resetAllStores();
    accessStore.setLoginExpired(false);

    await router.replace({
      path: LOGIN_PATH,
      query: redirect
        ? {
            redirect: encodeURIComponent(router.currentRoute.value.fullPath),
          }
        : {},
    });
  }

  async function fetchUserInfo() {
    const userInfo = await getUserInfoApi();
    userStore.setUserInfo(userInfo);
    return userInfo;
  }

  function $reset() {
    loginLoading.value = false;
  }

  return {
    $reset,
    authLogin,
    fetchUserInfo,
    loginLoading,
    logout,
  };
});
