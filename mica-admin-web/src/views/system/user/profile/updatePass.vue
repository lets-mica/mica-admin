<script setup lang="ts">
import { reactive, ref } from 'vue';
import {
  NButton,
  NForm,
  NFormItem,
  NInput,
  NModal,
} from 'naive-ui';
import { updatePassword } from '#/api/system/user';
import { notification } from '#/adapter/naive';

defineOptions({ name: 'UpdatePassword' });

const show = ref(false);
const loading = ref(false);
const formRef = ref<any>(null);

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const rules: Record<string, any> = {
  oldPassword: { required: true, message: '请输入旧密码', trigger: 'blur' },
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator(_: any, value: string) {
        if (value !== form.newPassword) {
          return new Error('两次输入的密码不一致');
        }
        return true;
      },
      trigger: 'blur',
    },
  ],
};

async function handleSubmit() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }
  loading.value = true;
  try {
    await updatePassword(form.oldPassword, form.newPassword);
    notification.success({ content: '密码修改成功', duration: 2000 });
    show.value = false;
    resetForm();
  } catch (e: any) {
    notification.error({ content: '修改失败', description: e.message, duration: 3000 });
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  form.oldPassword = '';
  form.newPassword = '';
  form.confirmPassword = '';
  formRef.value?.restoreValidation();
}

function open() {
  resetForm();
  show.value = true;
}

function close() {
  show.value = false;
}

defineExpose({ open, close });
</script>

<template>
  <NModal
    v-model:show="show"
    preset="card"
    title="修改密码"
    style="width: 480px"
    :mask-closable="false"
  >
    <NForm
      ref="formRef"
      :model="form"
      :rules="rules"
      label-placement="left"
      :label-width="80"
    >
      <NFormItem label="旧密码" path="oldPassword">
        <NInput
          v-model:value="form.oldPassword"
          type="password"
          show-password-on="click"
          placeholder="请输入旧密码"
        />
      </NFormItem>
      <NFormItem label="新密码" path="newPassword">
        <NInput
          v-model:value="form.newPassword"
          type="password"
          show-password-on="click"
          placeholder="请输入新密码"
        />
      </NFormItem>
      <NFormItem label="确认密码" path="confirmPassword">
        <NInput
          v-model:value="form.confirmPassword"
          type="password"
          show-password-on="click"
          placeholder="请再次输入新密码"
        />
      </NFormItem>
    </NForm>
    <template #footer>
      <div class="flex justify-end gap-2">
        <NButton @click="show = false">取消</NButton>
        <NButton type="primary" :loading="loading" @click="handleSubmit">确认</NButton>
      </div>
    </template>
  </NModal>
</template>
