# mica-admin-uniapp

mica-admin 移动办公 App,基于 **uniapp + Vue 3 + Vite + TypeScript + Pinia + uni-ui**,对接 mica-admin-server(Java)后端。

> **本工程按 [`docs/app/`](../../docs/app/README.md) 文档实现 v1.0 全部 11 个模块**。
> 设计原则:**通用模块不修改,二次开发只新增**。

## 技术栈

- **框架**: uniapp + Vue 3.4 + Vite 5
- **语言**: TypeScript 5
- **状态**: Pinia 2 + pinia-plugin-persistedstate(JWT 持久化)
- **UI**: @dcloudio/uni-ui
- **i18n**: vue-i18n 9
- **工具**: dayjs

## 工程结构

```
src/
├── api/                  # 自动/手写 API 客户端
│   ├── auth.ts           # 登录、公钥、当前用户、菜单
│   ├── user.ts           # 通讯录、个人中心
│   ├── message.ts        # 系统消息
│   ├── notice.ts         # 通知公告
│   ├── file.ts           # 文件上传
│   ├── dict.ts           # 字典
│   └── monitor.ts        # 监控、Token 管理
├── modules/              # 业务模块(详见 docs/app/roadmap.md §v1.0)
│   ├── auth/             # 登录(RSA + 算术验证码)— 通用
│   ├── workbench/        # 工作台(顶层页)
│   ├── message/          # 消息中心(顶层页)
│   ├── profile/          # 我的 + 子页(改密、改邮箱、关于)
│   ├── contacts/         # 通讯录(读 + 拨号按钮)
│   ├── notice/           # 通知公告
│   ├── file/             # 文件中心
│   ├── token/            # Token 管理(管理员)
│   ├── monitor/          # 监控(管理员)
│   ├── dict/             # 字典查询(管理员)
│   └── extension/        # ⭐ 二次开发预留
│       ├── approval.vue  # 审批占位
│       └── attendance.vue# 考勤占位
├── stores/               # 全局 store
│   └── auth.ts
├── utils/
│   ├── request.ts        # 网络请求封装(code = 0 适配)
│   ├── rsa.ts            # RSA 加密(密码)
│   ├── storage.ts        # 持久化
│   ├── format.ts         # 日期格式化
│   └── uuid.ts
├── types/                # 类型定义
├── config/
│   └── env.ts            # 环境变量类型化封装
├── locales/              # i18n
│   ├── langs/zh-CN.ts
│   └── langs/en-US.ts
└── pages.json            # 路由
```

## 快速开始

### 环境要求

- Node.js 18+
- pnpm 9+
- HBuilderX(可选,推荐用于 App/小程序调试)

### 安装

```bash
pnpm install
```

### 开发

```bash
# H5
pnpm dev:h5          # http://localhost:5889

# App(iOS/Android,需 HBuilderX 真机运行)
pnpm dev:app

# 微信小程序
pnpm dev:mp-weixin
```

### 类型检查

```bash
pnpm typecheck
```

### 生产构建

```bash
pnpm build:h5        # 产出 dist/
pnpm build:app       # 产出 unpackage/dist/dev/app-plus
pnpm build:mp-weixin # 产出 unpackage/dist/dev/mp-weixin
```

## 后端配置

### 开发环境代理

`vite.config.ts` 已配置:

- `/api/*` → `http://localhost:8080`

### 后端要求

按 [AGENTS.md §后端架构](../../AGENTS.md) 启动 mica-admin-server,
确保以下端点可用:

- HTTP: `/api/auth/*`、`/api/system/*`

### 切换后端地址

编辑 `.env.development` / `.env.production`:

```bash
VITE_GLOB_API_URL=/api
```

## 7 个模块一览

| # | 模块 | 入口 | 后端依赖 |
|---|---|---|---|
| 1 | 登录 | [modules/auth/pages/login.vue](src/modules/auth/pages/login.vue) | `/api/auth/*` + `/api/session` |
| 2 | 工作台 | [pages/index/index.vue](src/pages/index/index.vue) | 聚合(系统未读 + 最新公告) |
| 3 | 消息中心 | [pages/message/index.vue](src/pages/message/index.vue) | `/api/system/user/message/*` + `/api/system/notice/feed` |
| 4 | 我的 | [pages/profile/index.vue](src/pages/profile/index.vue) | `/api/system/users/*` |
| 5 | 通讯录 | [modules/contacts/pages/](src/modules/contacts/pages/index.vue) | `/api/system/*` |
| 6 | 通知公告 | [modules/notice/pages/list.vue](src/modules/notice/pages/list.vue) | `/api/system/notice/feed` |
| 7 | 文件中心 | [modules/file/pages/index.vue](src/modules/file/pages/index.vue) | `/api/upload/**` |

## 二次开发

新增模块请放在 `src/modules/extension/{模块名}/` 下,**不要修改通用模块**。
完整指引见 [`docs/app/extension.md`](../../docs/app/extension.md)。

## 注意事项

- **生产前必须替换 RSA 加密**:`src/utils/rsa.ts` 当前为占位实现,生产前请引入 `jsencrypt` 并配置真实公钥。
- **Tabbar 图标**:`static/tabbar/` 下 8 个 PNG 需替换为设计稿(详见 [static/README.md](static/README.md))。
- **响应格式**:后端成功 `code = 0`(非 200),`utils/request.ts` 已适配。

## License

Apache 2.0 — 与 mica-admin 一致。