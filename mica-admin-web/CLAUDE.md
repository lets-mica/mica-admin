# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

`new-ui` 是 mica-admin 项目的前端工程，从 `mica-admin-web`（Vue 2 + Element UI）改造为 **Vben Admin 5.x**（Vue 3 + Naive UI + Vite）。这是独立的 Vite 项目（非 monorepo），不通过 `@vben/*` npm 包安装，而是将源码完整提取到 `vben/` 目录作为 workspace package，通过本地别名解析。

源项目参考：`E:\codes\ai\vue-vben-admin`（完整 pnpm monorepo）。

后端：mica-admin（Spring Boot），通过 `/api` 代理访问（默认 `http://localhost:8080`）。

## 常用命令

```bash
# 开发
pnpm dev                       # 启动 dev server (端口 5888)

# 构建
pnpm build                     # 完整构建：vue-tsc 类型检查 + vite build
pnpm build:prod                # 仅打包，跳过类型检查
pnpm build:analyze             # 构建并分析包体积
pnpm preview                   # 预览构建产物

# 类型检查与 Lint
pnpm typecheck                 # vue-tsc --noEmit --skipLibCheck
pnpm lint                      # eslint --fix

# Maven 集成（Java 后端构建）
mvn package -Pprod             # 自动执行 pnpm install + pnpm build:prod，将 dist 打入 META-INF/resources
```

## 目录结构

```
new-ui/
├── package.json                 # 名称: mica-admin-web
├── pnpm-workspace.yaml          # 指向 vben/packages/*, vben/core/*, vben/tailwind-config
├── vite.config.ts               # 自定义 vbenResolver 处理 @vben/* 和 @vben-core/* 别名
├── tsconfig.json
├── pom.xml                      # Maven 集成（new-ui 产物打入 META-INF/resources）
├── index.html
├── src/
│   ├── main.ts                  # 入口：initPreferences -> bootstrap -> mount
│   ├── bootstrap.ts             # 启动链：adapter -> i18n -> stores -> directive -> router
│   ├── app.vue                  # 根组件：NConfigProvider + NNotificationProvider + NMessageProvider
│   ├── preferences.ts           # app 偏好覆盖
│   ├── adapter/                 # UI 适配层
│   │   ├── component/index.ts   # 组件注册：Input->NInput, Select->NSelect
│   │   ├── form.ts              # VbenForm 适配
│   │   ├── naive.ts             # createDiscreteApi
│   │   └── vxe-table.ts         # vxe-table 适配
│   ├── api/                     # API 接口层
│   │   ├── request.ts           # RequestClient 配置
│   │   ├── core/                # 核心 API
│   │   ├── system/              # 系统管理 API
│   │   ├── monitor/             # 系统监控 API
│   │   ├── oss/                 # OSS 存储 API
│   │   └── index.ts             # 统一导出
│   ├── layouts/                 # 布局组件
│   ├── locales/                 # i18n
│   ├── router/                  # 路由
│   ├── store/                   # Pinia stores
│   ├── styles/                  # 全局样式
│   ├── utils/                   # 工具函数
│   │   └── rsa.ts               # RSA 加密
│   └── views/                   # 业务页面
├── vben/                       # 本地化的 Vben 源码
│   ├── packages/                # 14 个 @vben/* 包
│   ├── core/                    # 12 个 @vben-core/* 包
│   └── tailwind-config/         # tailwind 主题配置
└── scripts/                     # 构建/转换脚本
```

## 关键架构与约定

### 1. 包解析机制（重要）

**不要**直接修改 `vben/` 内包的导入路径。Vite 配置中的 `vbenResolver()` 插件负责将：

- `@vben/xxx` -> `vben/packages/xxx/src/...`
- `@vben-core/xxx` -> `vben/core/xxx/src/...`
- `@vben/styles/naive` -> `vben/packages/styles/src/naive/index.css`
- `@vben/common-ui/es/xxx` -> `vben/packages/common-ui/src/components/xxx/...`
- `@vben/plugins/xxx` -> `vben/packages/plugins/src/xxx/...`
- `@vben/tailwind-config` -> `vben/tailwind-config/src/...`

所有 `vben/` 内的包通过 pnpm workspace 协议连接。

### 2. 认证流程（mica-admin 特有）

后端 mica-admin 与 Vben 默认认证不兼容，已在 `src/api/core/auth-mica-admin.ts` 和 `src/store/auth.ts` 中适配：

| 步骤 | 接口 | 说明 |
|------|------|------|
| 1. 获取公钥 | `GET /api/auth/public-key` | RSA 公钥缓存到 `cachedPublicKey` |
| 2. 获取验证码 | `GET /api/auth/captcha?t=xxx` | 返回 `{ uuid, base64 }`，转换为 `{ captchaId, captchaImage }` |
| 3. 登录 | `POST /api/session` (form-urlencoded) | 密码 RSA 加密，参数：`username`, `password`(加密), `validateCodeId`, `validateCode`, `remember-me` |
| 4. 用户信息 | `GET /api/auth/info` | 返回 `{ userInfo: {...} }` |
| 5. 菜单 | `GET /api/auth/menus` | 返回树形菜单数据 |
| 6. 登出 | `GET /api/logout` | 清理公钥缓存 |

`authLogin(params, onSuccess, useMicaAdminLogin=true)` — 调用 mica-admin 登录流程；`useMicaAdminLogin=false` 时使用 Vben 默认登录。

### 3. API 响应格式

后端 mica-admin 返回**两种格式**（成功无包装，失败有包装），`src/api/request.ts` 中的 `defaultResponseInterceptor` 处理：

```ts
defaultResponseInterceptor({
  codeField: 'code',
  dataField: 'data',
  successCode: 0,  // 注意：mica-admin 成功 code = 0
})
```

**分页响应**统一为 `{ list: [], total: 0 }` 格式，业务 API 文件需要自行转换（如 `src/api/system/user.ts` 中的 `getUserList` 处理 `records`/`data`/array 多种格式）。

### 4. 路由系统

- **核心路由** (`src/router/routes/core.ts`)：根路由 `Root`（挂载 `BasicLayout`）、auth/*、404、个人中心与我的消息（`Profile` / `UserMessage` 等不走权限的内建页面）
- **后端动态路由** (`src/router/access.ts`)：`preferences.app.accessMode === 'backend'`，`generateAccessible('backend', ...)` 拉取 `GET /api/auth/menus` 后用 `import.meta.glob('../views/**/*.vue')` 匹配页面组件、生成菜单与可访问路由。`access.ts` 还负责把后端扁平列表 `buildTree` 拼成父子树、把所有节点 path 升级为完整绝对路径、给外链生成 `/__external__/<id>` 占位
- **路由守卫** (`src/router/guard.ts`)：`setupCommonGuard`（进度条）+ `setupAccessGuard`（认证 + 触发动态路由生成）
- **历史**：`src/router/routes/modules/` 目录已废弃（`accessMode` 切到 `backend` 后不再扫描），新增业务页面**不要**在该目录写静态路由

### 5. UI 组件使用约定

| 旧 Element UI | Vben Admin 替代 | 备注 |
|--------------|----------------|------|
| `el-table` | `NDataTable` / `vxe-table` | 业务模块用 `NDataTable` |
| `el-form` | `VbenForm` | 通过 adapter/form.ts 适配 |
| `el-dialog` | `VbenModal` / `useVbenModal` | 使用 `modalApi.open()` / `modalApi.close()` |
| `el-tree` | `NTree` / `VbenTree` | 树形展示 |
| `el-select` | `NSelect` | |
| `el-input` | `NInput` | |
| `el-pagination` | `NPagination` | 启用 `show-size-picker` / `show-quick-jumper` |
| 启用/禁用 | `NSwitch` | 不用字符串值 `NSelect` |

**NDataTable 选择**：使用 `:checked-row-keys` + `@update:checked-row-keys`（**不要**用 `:selection` 或 `@update:selection-keys`）。

**对话框/确认**：使用 `dialog.warning()` 替代浏览器原生 `confirm()`。

**图标**：使用 `lucide-vue-next`（如 `lucide:users`, `lucide:settings`）。

### 6. 状态管理

- **业务 store** (`src/store/`)：使用 Pinia 定义，**单文件**直接 export（如 `useAuthStore`）。
- **共享 store** (`vben/packages/stores/`)：`useAccessStore`（token, 权限码, 菜单）、`useUserStore`、`useTabbarStore`、`useTimezoneStore`。
- **持久化**：通过 `pinia-plugin-persistedstate`，namespace 由 `import.meta.env.VITE_APP_NAMESPACE` 决定。

### 7. i18n

- 中文：`src/locales/langs/zh-CN/`
- 英文：`src/locales/langs/en-US/`
- 使用 `vue-i18n`，在路由 meta 中通过 `$t('page.system.user')` 引用

### 8. 样式

- **Tailwind CSS 4** + **Naive UI** 主题系统
- 全局样式：`src/styles/global.css` + `src/styles/theme.css`
- CSS 变量：通过 `--foreground`, `--primary`, `--accent` 等 HSL 值定义
- **不要**在 `vben/` 包内使用 `@apply`（已通过 `scripts/fix-apply.mjs` 批量转换为纯 CSS）

### 9. 关键文件清单

修改以下文件时需特别注意：

| 文件 | 作用 |
|------|------|
| `src/main.ts` | 应用入口 |
| `src/bootstrap.ts` | 启动顺序 |
| `src/app.vue` | 根组件 |
| `src/api/request.ts` | 全局请求拦截器（token 注入、错误处理、token 刷新） |
| `src/store/auth.ts` | 登录/登出/获取用户信息 |
| `src/api/core/auth-mica-admin.ts` | mica-admin 登录 API |
| `src/router/guard.ts` | 路由权限守卫 |
| `src/router/access.ts` | 动态路由生成 |
| `src/adapter/naive.ts` | Naive UI discrete API |
| `src/adapter/component/index.ts` | 组件注册映射 |
| `src/preferences.ts` | app 偏好覆盖 |
| `vite.config.ts` | 别名解析（vbenResolver） |

### 10. 环境变量

`.env.development` / `.env.production` 中定义：
- `VITE_APP_TITLE`：应用名
- `VITE_APP_NAMESPACE`：存储 namespace
- `VITE_APP_STORE_SECURE_KEY`：加密存储密钥
- `VITE_GLOB_API_URL`：API 基础路径（默认 `/api`）
- `VITE_ROUTER_HISTORY`：路由模式（`web`/`hash`）
- `VITE_INJECT_APP_LOADING`：是否注入加载动画
- `VITE_DEVTOOLS`：是否启用 Vue DevTools
- `VITE_PORT`：dev server 端口（默认 5888）

### 11. 代理配置

Vite dev server 默认将 `/api` 代理到 `http://localhost:8080`（在 `vite.config.ts` 中）。后端启动后才能正常登录。

## 常见任务

### 新增业务页面

1. 在 `src/api/system/`（或对应模块）创建 API 文件
2. 在 `src/views/<module>/<page>/index.vue` 创建页面（参考已有页面）
3. **不再写前端静态路由**：在 mica-admin 后端 `sys_menu` 表新增菜单项（`component` 填 `views/` 下的相对路径，如 `system/user/index`；`name` 全局唯一；`path` 填相对路径，顶级菜单由后端 `MenuVoUtil` 自动加前导 `/`；`parentId` 挂到对应父菜单），重新登录即可看到新菜单
4. 在 `src/locales/langs/zh-CN/` 和 `en-US/` 添加 i18n 翻译（菜单 `title` 由后端直接返回字符串，但页面内的提示用 i18n）

### 新增 API 方法

API 文件应统一处理分页响应（参考 `src/api/system/user.ts` 的 `getUserList`）。

### 修改登录流程

**不要**改 `src/api/core/auth.ts`（Vben 默认）。mica-admin 相关修改在 `src/api/core/auth-mica-admin.ts`。

### 接口类型

**无 swagger 代码生成**（`pnpm api` 脚本与 `src/api/Api.ts` 已移除）。新接口在 `src/api/<module>/<res>.ts` 手写 `interface` + 请求函数，字段以后端实体/VO 为准，需要核对时看后端源码或 `/doc.html`。后端改字段后要手工同步前端类型。

## 已知问题与历史

详见 `PLAN.md`、`MIGRATION.md`、`VIEW_MIGRATION_PLAN.md`。
