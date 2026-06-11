import type { RouteRecordRaw } from 'vue-router';

import { $t } from '#/locales';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'lucide:wrench',
      order: 80,
      title: '系统工具',
    },
    name: 'Tools',
    path: '/tools',
    children: [
      {
        name: 'ToolsSwagger',
        path: 'swagger',
        component: () => import('#/views/tools/swagger/index.vue'),
        meta: {
          icon: 'lucide:book',
          title: '接口文档',
        },
      },
    ],
  },
];

export default routes;
