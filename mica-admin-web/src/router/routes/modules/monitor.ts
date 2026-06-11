import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/monitor',
    name: 'Monitor',
    meta: { icon: 'lucide:monitor', order: 90, title: '系统监控' },
    children: [
      {
        name: 'MonitorToken',
        path: 'token',
        component: () => import('#/views/monitor/token/index.vue'),
        meta: { title: 'Token管理' },
      },
      {
        name: 'MonitorServer',
        path: 'server',
        component: () => import('#/views/monitor/server/index.vue'),
        meta: { title: '服务器监控' },
      },
      {
        name: 'MonitorLog',
        path: 'log',
        component: () => import('#/views/monitor/log/index.vue'),
        meta: { title: '操作日志' },
      },
      {
        name: 'MonitorErrorLog',
        path: 'log/error',
        component: () => import('#/views/monitor/log/errorLog.vue'),
        meta: { title: '异常日志' },
      },
      {
        name: 'MonitorRedis',
        path: 'redis',
        component: () => import('#/views/monitor/redis/index.vue'),
        meta: { title: 'Redis监控' },
      },
      {
        name: 'MonitorDruid',
        path: 'druid',
        component: () => import('#/views/monitor/druid/index.vue'),
        meta: { title: 'Druid监控' },
      },
      {
        name: 'MonitorOnline',
        path: 'online',
        component: () => import('#/views/monitor/online/index.vue'),
        meta: { title: '在线用户' },
      },
    ],
  },
];

export default routes;