<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui';
import type { UserItem } from '#/api/system/user';
import type { LogItem } from '#/api/monitor/log';

import { h, onMounted, reactive, ref } from 'vue';
import {
  NAvatar,
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NPagination,
  NRadio,
  NRadioGroup,
  NTabPane,
  NTabs,
  NTag,
  NUpload,
} from 'naive-ui';
import { useAccessStore, useUserStore } from '@vben/stores';
import { Building, LogIn, Mail, Phone, Shield, User as UserIcon } from '@lucide/vue';

import { editUserCenter, getUser } from '#/api/system/user';
import { getOperationLogList } from '#/api/monitor/log';
import { notification } from '#/adapter/naive';
import { formatDateTime } from '#/utils/format-date';

import UpdatePass from './updatePass.vue';
import UpdateEmail from './updateEmail.vue';

defineOptions({ name: 'UserProfile' });

const userStore = useUserStore();
const accessStore = useAccessStore();

const loading = ref(false);
const saveLoading = ref(false);
const userInfo = ref<UserItem | null>(null);

const activeTab = ref<string>('profile');

const form = reactive({
  nickName: '',
  phone: '',
  gender: 0,
});

const formRules = {
  nickName: [
    { required: true, message: '请输入用户昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  phone: [
    {
      required: true,
      validator(_: any, value: string) {
        if (!value) return new Error('请输入手机号码');
        if (!/^1[3-9]\d{9}$/.test(value)) return new Error('请输入正确的11位手机号码');
        return true;
      },
      trigger: 'blur',
    },
  ],
};

const updatePassRef = ref<InstanceType<typeof UpdatePass> | null>(null);
const updateEmailRef = ref<InstanceType<typeof UpdateEmail> | null>(null);
const formRef = ref<any>(null);

// 操作日志
const logLoading = ref(false);
const logData = ref<LogItem[]>([]);
const logPagination = reactive({ page: 1, pageSize: 10, total: 0 });

const logColumns: DataTableColumns<LogItem> = [
  { title: '行为', key: 'description', ellipsis: { tooltip: true } },
  { title: 'IP', key: 'requestIp', width: 160 },
  { title: 'IP来源', key: 'address', width: 180, ellipsis: { tooltip: true } },
  { title: '操作系统', key: 'os', width: 160 },
  { title: '浏览器', key: 'browser', width: 120 },
  {
    title: '请求耗时',
    key: 'requestTime',
    width: 110,
    align: 'center',
    render: (row) => {
      const t = row.requestTime || 0;
      const type = t <= 300 ? 'success' : t <= 1000 ? 'warning' : 'error';
      return h(NTag, { type, size: 'small', bordered: false }, () => `${t}ms`);
    },
  },
  {
      title: '创建时间',
      key: 'createdAt',
      width: 180,
      render: (row) => formatDateTime(row.createdAt),
    },
];

async function loadUserInfo() {
  loading.value = true;
  try {
    const userId = (userStore.userInfo as any)?.userId || (userStore.userInfo as any)?.id;
    if (userId) {
      const data = await getUser(userId);
      userInfo.value = data;
      form.nickName = data.nickName || '';
      form.phone = data.phone || '';
      form.gender = data.gender ?? 0;
    }
  } catch (error) {
    console.error('Failed to load user info:', error);
  } finally {
    loading.value = false;
  }
}

async function loadLogs() {
  logLoading.value = true;
  try {
    const result = await getOperationLogList({
      page: logPagination.page,
      size: logPagination.pageSize,
    });
    logData.value = result.list || [];
    logPagination.total = result.total || 0;
  } catch (e: any) {
    console.error('Failed to load logs:', e);
    logData.value = [];
    logPagination.total = 0;
  } finally {
    logLoading.value = false;
  }
}

function handleTabChange(name: string) {
  activeTab.value = name;
  if (name === 'logs' && logData.value.length === 0) {
    loadLogs();
  }
}

function handleLogPageChange(page: number) {
  logPagination.page = page;
  loadLogs();
}

function handleLogPageSizeChange(pageSize: number) {
  logPagination.pageSize = pageSize;
  logPagination.page = 1;
  loadLogs();
}

async function handleSubmit() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    return; // 校验失败，naive-ui 已自动提示
  }
  saveLoading.value = true;
  try {
    await editUserCenter({
      nickName: form.nickName,
      phone: form.phone,
      gender: form.gender,
    } as any);
    notification.success({ content: '资料已更新', duration: 2000 });
    loadUserInfo();
  } catch (error: any) {
    console.error('Failed to update:', error);
  } finally {
    saveLoading.value = false;
  }
}

function handleAvatarSuccess({ file, event }: any) {
  // 后端返回 { data: { avatar: 'url' } } 结构
  try {
    const response = file?.response || (event?.target as XMLHttpRequest)?.response;
    let avatarUrl: string | undefined;
    if (response) {
      const parsed = typeof response === 'string' ? JSON.parse(response) : response;
      avatarUrl = parsed?.data?.avatar || parsed?.avatar;
    }
    if (avatarUrl && userInfo.value) {
      userInfo.value = { ...userInfo.value, avatar: avatarUrl } as any;
    } else {
      loadUserInfo();
    }
  } catch (e) {
    console.warn('Failed to parse avatar response:', e);
    loadUserInfo();
  }
  notification.success({ content: '头像修改成功', duration: 2000 });
}

function handleAvatarError() {
  notification.error({ content: '头像上传失败', duration: 2500 });
}

function handleUpdatePassword() {
  updatePassRef.value?.open();
}

function handleUpdateEmail() {
  updateEmailRef.value?.open();
}

onMounted(() => loadUserInfo());
</script>

<template>
  <div class="profile-page p-4">
    <div class="profile-grid">
      <!-- 左侧：个人信息 -->
      <div class="profile-left">
        <NCard>
          <div class="avatar-wrapper">
            <NUpload
              :show-file-list="false"
              accept="image/*"
              action="/api/system/users/avatar"
              name="file"
              method="post"
              :headers="{ Authorization: 'Bearer ' + (accessStore.accessToken || '') }"
              @finish="handleAvatarSuccess"
              @error="handleAvatarError"
            >
              <NAvatar
                :size="120"
                round
                :src="(userInfo as any)?.avatar"
                class="avatar"
              >
                <template v-if="!(userInfo as any)?.avatar" #default>
                  <NIcon :size="48"><UserIcon /></NIcon>
                </template>
              </NAvatar>
            </NUpload>
            <div class="avatar-hint">点击上传头像</div>
          </div>

          <ul class="user-info">
            <li>
              <div class="li-left">
                <NIcon class="info-icon"><LogIn /></NIcon>
                <span>登录账号</span>
              </div>
              <div class="user-right">{{ userInfo?.userName || '-' }}</div>
            </li>
            <li>
              <div class="li-left">
                <NIcon class="info-icon"><UserIcon /></NIcon>
                <span>用户昵称</span>
              </div>
              <div class="user-right">{{ userInfo?.nickName || '-' }}</div>
            </li>
            <li>
              <div class="li-left">
                <NIcon class="info-icon"><Building /></NIcon>
                <span>所属部门</span>
              </div>
              <div class="user-right">{{ userInfo?.dept?.name || '-' }}</div>
            </li>
            <li>
              <div class="li-left">
                <NIcon class="info-icon"><Phone /></NIcon>
                <span>手机号码</span>
              </div>
              <div class="user-right">{{ userInfo?.phone || '-' }}</div>
            </li>
            <li>
              <div class="li-left">
                <NIcon class="info-icon"><Mail /></NIcon>
                <span>用户邮箱</span>
              </div>
              <div class="user-right">{{ userInfo?.email || '-' }}</div>
            </li>
            <li>
              <div class="li-left">
                <NIcon class="info-icon"><Shield /></NIcon>
                <span>安全设置</span>
              </div>
              <div class="user-right">
                <a class="link" @click="handleUpdatePassword">修改密码</a>
                <a class="link" @click="handleUpdateEmail">修改邮箱</a>
              </div>
            </li>
          </ul>
        </NCard>
      </div>

      <!-- 右侧：tabs -->
      <div class="profile-right">
        <NCard>
          <NTabs v-model:value="activeTab" type="line" animated @update:value="handleTabChange">
            <NTabPane name="profile" tab="用户资料">
              <NForm
                ref="formRef"
                :model="form"
                :rules="formRules"
                label-placement="left"
                :label-width="65"
                size="small"
                class="profile-form"
              >
                <NFormItem label="昵称" path="nickName">
                  <NInput v-model:value="form.nickName" placeholder="请输入用户昵称" class="form-input" />
                  <span class="form-tip">用户昵称不作为登录使用</span>
                </NFormItem>
                <NFormItem label="手机号" path="phone">
                  <NInput v-model:value="form.phone" placeholder="请输入手机号" class="form-input" />
                  <span class="form-tip">手机号码不能重复</span>
                </NFormItem>
                <NFormItem label="性别">
                  <NRadioGroup v-model:value="form.gender">
                    <NRadio :value="0">男</NRadio>
                    <NRadio :value="1">女</NRadio>
                  </NRadioGroup>
                </NFormItem>
                <NFormItem label=" ">
                  <NButton size="small" type="primary" :loading="saveLoading" @click="handleSubmit">
                    保存配置
                  </NButton>
                </NFormItem>
              </NForm>
            </NTabPane>

            <NTabPane name="logs" tab="操作日志">
              <NDataTable
                :loading="logLoading"
                :columns="logColumns"
                :data="logData"
                :row-key="(row: LogItem) => row.id"
                striped
              />
              <div class="mt-4 flex justify-end">
                <NPagination
                  v-model:page="logPagination.page"
                  v-model:page-size="logPagination.pageSize"
                  :item-count="logPagination.total"
                  :page-sizes="[10, 20, 50, 100]"
                  show-size-picker
                  show-quick-jumper
                  @update:page="handleLogPageChange"
                  @update:page-size="handleLogPageSizeChange"
                />
              </div>
            </NTabPane>
          </NTabs>
        </NCard>
      </div>
    </div>

    <UpdatePass ref="updatePassRef" />
    <UpdateEmail ref="updateEmailRef" :email="userInfo?.email || ''" />
  </div>
</template>

<style scoped>
/* 整体栅格布局：左 1/4 + 间距 + 右 3/4 */
.profile-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

@media (min-width: 992px) {
  .profile-grid {
    grid-template-columns: 280px 1fr;
  }
}

/* 头像区域（与原版一致） */
.avatar-wrapper {
  text-align: center;
  margin-bottom: 16px;
}

.avatar {
  display: block;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  cursor: pointer;
  transition: opacity 0.2s;
  border: 1px solid #f0f0f0;
  margin: 0 auto;
}

.avatar:hover {
  opacity: 0.8;
}

.avatar-hint {
  margin-top: 8px;
  color: #999;
  font-size: 13px;
}

/* 用户信息列表（与原版一致） */
.user-info {
  list-style: none;
  padding-left: 0;
  margin: 0;
}

.user-info li {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #f0f3f4;
  padding: 11px 0;
  font-size: 13px;
  min-height: 38px;
}

.user-info li:last-child {
  border-bottom: none;
}

.li-left {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  flex: 0 1 auto;
}

.user-info .info-icon {
  color: #909399;
  font-size: 16px;
  display: inline-flex;
  align-items: center;
}

.user-info .user-right {
  margin-left: auto;
  color: #333;
  text-align: right;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.user-info .link {
  color: #317ef3;
  cursor: pointer;
  font-size: 13px;
}

.user-info .link:hover {
  text-decoration: underline;
}

/* 资料表单（与原版一致） */
.profile-form {
  margin-top: 10px;
  max-width: 100%;
}

:deep(.profile-form .form-input) {
  width: 280px !important;
  margin-right: 10px;
}

.form-tip {
  color: #c0c0c0;
  font-size: 12px;
}
</style>
