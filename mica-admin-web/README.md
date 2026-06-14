# new-ui — mica-admin 前端改造（Vben Admin Naive UI）

将 `mica-admin-web`（Vue 2 + Element UI）替换为 Vben Admin 5.x（Vue 3 + Naive UI）。

## 架构

```
mica-admin-web/
├── package.json
├── vite.config.ts
├── pnpm-workspace.yaml
├── src/                    # mica-admin 业务代码
│   ├── adapter/
│   ├── api/
│   ├── layouts/
│   ├── router/
│   ├── store/
│   └── views/
└── vben/                   # 本地化的 Vben Admin 框架源码
    ├── packages/           # @vben/* (14 个包)
    ├── core/               # @vben-core/* (12 个包)
    └── tailwind-config/
```

## 源项目

Vben Admin monorepo: `E:\codes\ai\vue-vben-admin`

```
apps/web-naive/          → 目标 app（照搬入口、路由、页面、store）
packages/effects/access  → vben/packages/access
packages/effects/common-ui → vben/packages/common-ui
packages/constants       → vben/packages/constants
packages/effects/hooks   → vben/packages/hooks
packages/icons           → vben/packages/icons
packages/effects/layouts → vben/packages/layouts
packages/locales         → vben/packages/locales
packages/effects/plugins → vben/packages/plugins
packages/preferences     → vben/packages/preferences
packages/effects/request → vben/packages/request
packages/stores          → vben/packages/stores
packages/styles          → vben/packages/styles
packages/types           → vben/packages/types
packages/utils           → vben/packages/utils
packages/@core/*         → vben/core/*
```

## 计划步骤

见 `PLAN.md`
