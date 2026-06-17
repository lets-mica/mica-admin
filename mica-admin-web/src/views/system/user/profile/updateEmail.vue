<script setup lang="ts">
import { reactive, ref } from 'vue';
import {
  NButton,
  NForm,
  NFormItem,
  NInput,
  NModal,
} from 'naive-ui';
import { editUser } from '#/api/system/user';
import { notification } from '#/adapter/naive';

defineOptions({ name: 'UpdateEmail' });

defineProps<{ email?: string }>();
const show = ref(false);
const loading = ref(false);
const formRef = ref<any>(null);

const form = reactive({
  email: '',
  password: '',
});

const rules: Record<string, any> = {
  email: [
    { required: true, message: '请输入新邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
  ],
  password: { required: true, message: '请输入当前密码进行验证', trigger: 'blur' },
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
    await editUser({ email: form.email } as any);
    notification.success({ content: '邮箱修改成功', duration: 2000 });
    show.value = false;
    resetForm();
  } catch (e: any) {
    console.error('Failed to update email:', e);
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  form.email = '';
  form.password = '';
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
    title="修改邮箱"
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
      <NFormItem label="新邮箱" path="email">
        <NInput
          v-model:value="form.email"
          placeholder="请输入新邮箱"
        />
      </NFormItem>
      <NFormItem label="密码验证" path="password">
        <NInput
          v-model:value="form.password"
          type="password"
          show-password-on="click"
          placeholder="请输入当前密码"
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
