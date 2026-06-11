import type { RouteRecordRaw } from 'vue-router';

import { $t } from '#/locales';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'lucide:settings',
      order: 100,
      title: $t('page.system.title'),
    },
    name: 'System',
    path: '/system',
    children: [
      {
        name: 'SystemUser',
        path: 'user',
        component: () => import('#/views/system/user/index.vue'),
        meta: {
          icon: 'lucide:users',
          title: $t('page.system.user'),
        },
      },
      {
        name: 'SystemRole',
        path: 'role',
        component: () => import('#/views/system/role/index.vue'),
        meta: {
          icon: 'lucide:shield',
          title: $t('page.system.role'),
        },
      },
      {
        name: 'SystemMenu',
        path: 'menu',
        component: () => import('#/views/system/menu/index.vue'),
        meta: {
          icon: 'lucide:menu',
          title: $t('page.system.menu'),
        },
      },
      {
        name: 'SystemDept',
        path: 'dept',
        component: () => import('#/views/system/dept/index.vue'),
        meta: {
          icon: 'lucide:git-branch',
          title: $t('page.system.dept'),
        },
      },
      {
        name: 'SystemPost',
        path: 'post',
        component: () => import('#/views/system/post/index.vue'),
        meta: {
          icon: 'lucide:briefcase',
          title: $t('page.system.post'),
        },
      },
      {
        name: 'SystemDict',
        path: 'dict',
        component: () => import('#/views/system/dict/index.vue'),
        meta: {
          icon: 'lucide:bookmark',
          title: $t('page.system.dict'),
        },
      },
      {
        name: 'SystemNotice',
        path: 'notice',
        component: () => import('#/views/system/notice/index.vue'),
        meta: {
          icon: 'lucide:bell',
          title: $t('page.system.notice'),
        },
      },
      {
        name: 'SystemMessage',
        path: 'message',
        component: () => import('#/views/system/message/index.vue'),
        meta: {
          icon: 'lucide:mail',
          title: $t('page.system.message'),
        },
      },
      {
        name: 'SystemConfig',
        path: 'config',
        component: () => import('#/views/system/config/index.vue'),
        meta: {
          icon: 'lucide:sliders-horizontal',
          title: $t('page.system.config'),
        },
      },
      {
        name: 'SystemOss',
        path: 'oss',
        component: () => import('#/views/system/oss/index.vue'),
        meta: {
          icon: 'lucide:folder-open',
          title: 'OSS存储',
        },
      },
      {
        name: 'Profile',
        path: 'profile',
        component: () => import('#/views/system/user/profile/index.vue'),
        meta: {
          hideInMenu: true,
          hideInTab: false,
          title: '个人中心',
        },
      },
    ],
  },
];

export default routes;
