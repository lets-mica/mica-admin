import type { RouteRecordRaw } from 'vue-router';

import { $t } from '#/locales';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      hideInMenu: true,
      title: $t('page.system.userMessage'),
    },
    name: 'User',
    path: '/user',
    children: [
      {
        name: 'UserMessage',
        path: 'message',
        component: () => import('#/views/system/user/message.vue'),
        meta: {
          hideInMenu: true,
          hideInTab: false,
          icon: 'lucide:mail',
          title: $t('page.system.userMessage'),
        },
      },
    ],
  },
];

export default routes;
