# new-ui — mica-admin 前端改造（Vben Admin Naive UI）

将 `mica-admin-web`（Vue 2 + Element UI）替换为 Vben Admin 5.x（Vue 3 + Naive UI）。

## 架构

```
new-ui/
├── package.json          # 独立项目，pnpm 管理
├── vite.config.ts        # Vite 8 构建配置
├── tsconfig.json         # TypeScript 6
├── index.html
├── src/
│   ├── main.ts           # 入口
│   ├── bootstrap.ts      # 应用启动
│   ├── app.vue           # 根组件 (NConfigProvider)
│   ├── adapter/          # UI 适配层 (参考 web-naive/adapter)
│   ├── api/              # API 接口层
│   ├── layouts/          # 布局组件
│   ├── locales/          # i18n
│   ├── router/           # 路由
│   ├── store/            # Pinia stores
│   ├── views/            # 页面
│   └── _vben/            # 从 monorepo 提取的 @vben/* 和 @vben-core/* 源码
│       ├── packages/     # @vben/* (14 个包)
│       └── core/         # @vben-core/* (12 个包)
└── internal/
    ├── vite-config/      # @vben/vite-config
    └── tsconfig/         # @vben/tsconfig
```

## 源项目

Vben Admin monorepo: `E:\codes\ai\vue-vben-admin`

```
apps/web-naive/          → 目标 app（照搬入口、路由、页面、store）
packages/effects/access  → _vben/packages/access
packages/effects/common-ui → _vben/packages/common-ui
packages/constants       → _vben/packages/constants
packages/effects/hooks   → _vben/packages/hooks
packages/icons           → _vben/packages/icons
packages/effects/layouts → _vben/packages/layouts
packages/locales         → _vben/packages/locales
packages/effects/plugins → _vben/packages/plugins
packages/preferences     → _vben/packages/preferences
packages/effects/request → _vben/packages/request
packages/stores          → _vben/packages/stores
packages/styles          → _vben/packages/styles
packages/types           → _vben/packages/types
packages/utils           → _vben/packages/utils
packages/@core/*         → _vben/core/*
```

## 计划步骤

见 `PLAN.md`
