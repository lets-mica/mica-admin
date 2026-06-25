<script lang="ts" setup>
import type { NotificationItem } from '@vben/layouts';

import type { UserMessageItem } from '#/api/system/user-message';
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import { AuthenticationLoginExpiredModal } from '@vben/common-ui';
import { useWatermark } from '@vben/hooks';
import {
  BasicLayout,
  LockScreen,
  Notification,
  UserDropdown,
} from '@vben/layouts';
import { preferences, usePreferences } from '@vben/preferences';
import { useAccessStore, useUserStore } from '@vben/stores';

import { getUnreadMessages, markAllAsRead, markAsRead } from '#/api/system/user-message';
import { $t } from '#/locales';
import { useAuthStore } from '#/store';
import { formatDateTime } from '#/utils/format-date';
import LoginForm from '#/views/auth/login.vue';

const DEFAULT_AVATAR = '/logo.svg';

// 分类图标映射（SVG data URI：彩色圆形 + 白色图标）
const CATEGORY_ICONS: Record<string, string> = {
  system: `data:image/svg+xml,${encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#3b82f6"/><g transform="translate(11,11)" fill="none" stroke="white" stroke-width="1.8" stroke-linecap="round"><circle cx="9" cy="9" r="3"/><path d="M9 1v2M9 15v2M1 9h2M15 9h2M3.3 3.3l1.4 1.4M13.3 13.3l1.4 1.4M3.3 14.7l1.4-1.4M13.3 4.7l1.4-1.4"/></g></svg>')}`,
  security: `data:image/svg+xml,${encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#ef4444"/><path d="M20 9l-7 3v5c0 5.5 3 9.5 7 11 4-1.5 7-5.5 7-11v-5l-7-3z" fill="none" stroke="white" stroke-width="1.8" stroke-linejoin="round" stroke-linecap="round"/></svg>')}`,
  business: `data:image/svg+xml,${encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#22c55e"/><rect x="12" y="16" width="16" height="12" rx="1.5" fill="none" stroke="white" stroke-width="1.8"/><path d="M16 16v-3a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v3" fill="none" stroke="white" stroke-width="1.8" stroke-linecap="round"/></svg>')}`,
  activity: `data:image/svg+xml,${encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="#f97316"/><path d="M20 10l2.5 5.5 6 .8-4.5 4.2 1 6L20 24l-5.5 2.5 1-6-4.5-4.2 6-.8z" fill="white"/></svg>')}`,
};

function getCategoryIcon(category?: string): string {
  return (category && CATEGORY_ICONS[category]) || DEFAULT_AVATAR;
}

function mapToNotification(item: UserMessageItem): NotificationItem {
  return {
    id: item.id,
    avatar: getCategoryIcon(item.category),
    date: formatDateTime(item.createdAt),
    isRead: item.isRead,
    title: item.title,
    message: (item.content || '').substring(0, 80),
    link: '/user/message',
  };
}

const notifications = ref<NotificationItem[]>([]);

async function loadNotifications() {
  try {
    const list: UserMessageItem[] = await getUnreadMessages() as any;
    notifications.value = (list || []).map(mapToNotification);
  } catch {
    notifications.value = [];
  }
}

const router = useRouter();
const userStore = useUserStore();
const authStore = useAuthStore();
const accessStore = useAccessStore();
const { destroyWatermark, updateWatermark } = useWatermark();
const { isDark } = usePreferences();
const showDot = computed(() =>
  notifications.value.some((item) => !item.isRead),
);

const menus = computed(() => [
  {
    handler: () => {
      router.push({ name: 'Profile' });
    },
    icon: 'lucide:user',
    text: $t('page.auth.profile'),
  },
  {
    handler: () => {
      router.push({ name: 'UserMessage' });
    },
    icon: 'lucide:mail',
    text: $t('page.system.userMessage'),
  },
]);

const avatar = computed(() => {
  return userStore.userInfo?.avatar ?? preferences.app.defaultAvatar;
});

// 用户描述（用户名）
const userInfoDescription = computed(() => {
  return userStore.userInfo?.username || '';
});

// 用户标签文本（首个角色名）
const userInfoTagText = computed(() => {
  const roles = userStore.userInfo?.roles;
  if (Array.isArray(roles) && roles.length > 0) {
    return roles[0];
  }
  return '';
});

async function handleLogout() {
  await authStore.logout(false);
}

function handleNoticeClear() {
  notifications.value = [];
}

async function markRead(id: number | string) {
  try {
    await markAsRead(Number(id));
    const item = notifications.value.find((item) => item.id === id);
    if (item) {
      item.isRead = true;
    }
  } catch {
    // ignore
  }
}

function remove(id: number | string) {
  notifications.value = notifications.value.filter((item) => item.id !== id);
}

async function handleMakeAll() {
  try {
    await markAllAsRead();
    notifications.value.forEach((item) => (item.isRead = true));
  } catch {
    // ignore
  }
}

const viewAll = () => {
  router.push({ name: 'UserMessage' });
};

const handleClick = (item: NotificationItem) => {
  // 如果通知项有链接，点击时跳转
  if (item.link) {
    navigateTo(item.link, item.query, item.state);
  }
};

function navigateTo(
  link: string,
  query?: Record<string, any>,
  state?: Record<string, any>,
) {
  if (link.startsWith('http://') || link.startsWith('https://')) {
    // 外部链接，在新标签页打开
    window.open(link, '_blank');
  } else {
    // 内部路由链接，支持 query 参数和 state
    router.push({
      path: link,
      query: query || {},
      state,
    });
  }
}

// 轮询刷新未读消息（60秒）
let pollTimer: ReturnType<typeof setInterval>;
onMounted(() => {
  loadNotifications();
  pollTimer = setInterval(loadNotifications, 60_000);
});
onUnmounted(() => clearInterval(pollTimer));

watch(
  () => ({
    enable: preferences.app.watermark,
    content: preferences.app.watermarkContent,
    isDark: isDark.value,
  }),
  async ({ enable, content, isDark: isDarkValue }) => {
    if (enable) {
      const watermarkColor = isDarkValue
        ? 'rgba(255, 255, 255, 0.12)'
        : 'rgba(0, 0, 0, 0.12)';

      await updateWatermark({
        advancedStyle: {
          colorStops: [
            {
              color: watermarkColor,
              offset: 0,
            },
            {
              color: watermarkColor,
              offset: 1,
            },
          ],
          type: 'linear',
        },
        content:
          content ||
          `${userStore.userInfo?.username} - ${userStore.userInfo?.realName}`,
      });
    } else {
      destroyWatermark();
    }
  },
  {
    immediate: true,
  },
);
</script>

<template>
  <BasicLayout @clear-preferences-and-logout="handleLogout">
    <template #user-dropdown>
      <UserDropdown
        :avatar
        :description="userInfoDescription"
        :menus
        :tag-text="userInfoTagText"
        :text="userStore.userInfo?.realName"
        @logout="handleLogout"
        @clear-preferences-and-logout="handleLogout"
      />
    </template>
    <template #notification>
      <Notification
        :dot="showDot"
        :notifications="notifications"
        @clear="handleNoticeClear"
        @read="(item) => item.id && markRead(item.id)"
        @remove="(item) => item.id && remove(item.id)"
        @make-all="handleMakeAll"
        @on-click="handleClick"
        @view-all="viewAll"
      />
    </template>
    <template #extra>
      <AuthenticationLoginExpiredModal
        v-model:open="accessStore.loginExpired"
        :avatar
      >
        <LoginForm />
      </AuthenticationLoginExpiredModal>
    </template>
    <template #lock-screen>
      <LockScreen :avatar @to-login="handleLogout" />
    </template>
  </BasicLayout>
</template>
