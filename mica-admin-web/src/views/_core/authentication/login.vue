<script lang="ts" setup>
import type { VbenFormSchema } from '@vben/common-ui';
import type { Recordable } from '@vben/types';

import { computed, h, onMounted, ref } from 'vue';

import { AuthenticationLogin, z } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { useAuthStore } from '#/store';
import { getCaptchaApi } from '#/api/core/auth';

defineOptions({ name: 'Login' });

const authStore = useAuthStore();

const validateCodeId = ref('');
const codeBase64 = ref('');
const captchaLoading = ref(false);

async function loadCaptcha() {
  try {
    captchaLoading.value = true;
    const result = await getCaptchaApi();
    validateCodeId.value = result.captchaId;
    codeBase64.value = result.captchaImage;
  } catch (error) {
    console.error('[Login] Captcha load failed:', error);
  } finally {
    captchaLoading.value = false;
  }
}

onMounted(() => {
  loadCaptcha();
});

const formSchema = computed((): VbenFormSchema[] => {
  return [
    {
      component: 'VbenInput',
      fieldName: 'username',
      label: $t('authentication.username'),
      defaultValue: 'admin',
      componentProps: {
        placeholder: $t('authentication.usernameTip'),
      },
      rules: z.string().min(1, { message: $t('authentication.usernameTip') }),
    },
    {
      component: 'VbenInputPassword',
      fieldName: 'password',
      label: $t('authentication.password'),
      defaultValue: '123456',
      componentProps: {
        placeholder: $t('authentication.password'),
      },
      rules: z.string().min(1, { message: $t('authentication.passwordTip') }),
    },
    {
      component: 'VbenInput',
      fieldName: 'validateCode',
      label: '验证码',
      componentProps: {
        placeholder: '请输入验证码',
      },
      rules: z.string().min(1, { message: '请输入验证码' }),
      suffix: () => {
        if (captchaLoading.value) {
          return h(
            'span',
            { class: 'whitespace-nowrap text-sm text-muted-foreground' },
            '加载中...',
          );
        }
        if (!codeBase64.value) {
          return h('span');
        }
        return h('img', {
          alt: '验证码',
          class:
            'h-9 w-24 shrink-0 cursor-pointer rounded border border-border object-cover',
          src: codeBase64.value,
          onClick: loadCaptcha,
        });
      },
    },
  ];
});

async function handleLogin(values: Recordable<any>) {
  const { username, password, validateCode: code } = values;

  await authStore.authLogin({
    username,
    password,
    validateCodeId: validateCodeId.value,
    validateCode: code,
  });
}
</script>

<template>
  <AuthenticationLogin
    :form-schema="formSchema"
    :loading="authStore.loginLoading"
    :show-code-login="false"
    :show-forget-password="false"
    :show-qrcode-login="false"
    :show-register="false"
    :show-third-party-login="false"
    @submit="handleLogin"
  />
</template>
