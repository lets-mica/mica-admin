import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'lucide:layout-grid',
      order: 70,
      title: '公共组件',
    },
    name: 'Components',
    path: '/components',
    children: [
      {
        name: 'ComponentECharts',
        path: 'echarts',
        component: () => import('#/views/components/ECharts.vue'),
        meta: {
          icon: 'lucide:pie-chart',
          title: '图表组件',
        },
      },
      {
        name: 'ComponentEditor',
        path: 'editor',
        component: () => import('#/views/components/Editor.vue'),
        meta: {
          icon: 'lucide:file-text',
          title: '富文本编辑器',
        },
      },
      {
        name: 'ComponentMarkdown',
        path: 'markdown',
        component: () => import('#/views/components/MarkDown.vue'),
        meta: {
          icon: 'lucide:file-code',
          title: 'Markdown编辑器',
        },
      },
      {
        name: 'ComponentIcons',
        path: 'icons',
        component: () => import('#/views/components/icons/index.vue'),
        meta: {
          icon: 'lucide:smile',
          title: '图标选择器',
        },
      },
    ],
  },
];

export default routes;
