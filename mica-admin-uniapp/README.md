# mica-admin-uniapp

mica-admin 移动办公 App,基于 **uniapp + Vue 3 + Vite + TypeScript + Pinia + uni-ui**,对接 mica-admin-server(Java)后端。

> **本工程按 [`docs/app/`](../../docs/app/README.md) 文档实现 v1.0 全部 12 个模块**。
> 设计原则:**通用模块不修改,二次开发只新增**。

## 技术栈

- **框架**: uniapp + Vue 3.4 + Vite 5
- **语言**: TypeScript 5
- **状态**: Pinia 2 + pinia-plugin-persistedstate(JWT 持久化)
- **UI**: @dcloudio/uni-ui
- **实时**: mqtt.js 5.x(对接 mica-mqtt broker 8083)
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
│   ├── monitor.ts        # 监控、Token 管理
│   └── im/               # IM 客户端
│       ├── conversation.ts
│       ├── group.ts
│       └── user.ts
├── modules/              # 业务模块(详见 docs/app/roadmap.md §v1.0)
│   ├── auth/             # 登录(RSA + 算术验证码)— 通用
│   ├── workbench/        # 工作台(顶层页)
│   ├── message/          # 消息中心(顶层页)
│   ├── menu/             # 应用中心 + WebView 兜底
│   ├── profile/          # 我的 + 子页(改密、改邮箱、关于)
│   ├── contacts/         # 通讯录(读 + "发消息"按钮)
│   ├── notice/           # 通知公告
│   ├── file/             # 文件中心
│   ├── token/            # Token 管理(管理员)
│   ├── monitor/          # 监控(管理员)
│   ├── dict/             # 字典查询(管理员)
│   ├── im/               # ⭐ IM 即时通讯
│   │   ├── mqtt-client.ts        # mqtt.js 5.x 封装
│   │   ├── components/UserPicker # 复用选人
│   │   ├── stores/im.ts          # 会话列表 + MQTT 生命周期
│   │   ├── stores/chat.ts        # 单聊/群聊窗口
│   │   ├── stores/group.ts       # 群管理
│   │   └── pages/                # 5 个页面
│   └── extension/        # ⭐ 二次开发预留
│       ├── approval.vue  # 审批占位
│       └── attendance.vue# 考勤占位
├── stores/               # 全局 store
│   └── auth.ts
├── utils/
│   ├── request.ts        # 网络请求封装(code = 0 适配)
│   ├── rsa.ts            # RSA 加密(密码)
│   ├── storage.ts        # 持久化
│   ├── mqtt-config.ts    # MQTT 配置 + topic 工具
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
- `/mqtt` → `ws://localhost:8083`(H5 端 MQTT WebSocket 代理)

### 后端要求

按 [`docs/im/`](../../docs/im/README.md) 与 [AGENTS.md §后端架构](../../AGENTS.md) 启动 mica-admin-server,
确保以下端点可用:

- HTTP: `/api/auth/*`、`/api/system/*`、`/api/im/*`
- MQTT: TCP `1883` / WebSocket `8083`
- JWT 鉴权(在 MQTT CONNECT 包的 username 传 JWT)

### 切换后端地址

编辑 `.env.development` / `.env.production`:

```bash
VITE_GLOB_API_URL=/api
VITE_GLOB_MQTT_URL=/mqtt
```

## 12 个模块一览

| # | 模块 | 入口 | 后端依赖 |
|---|---|---|---|
| 1 | 登录 | [modules/auth/pages/login.vue](src/modules/auth/pages/login.vue) | `/api/auth/*` + `/api/session` |
| 2 | 工作台 | [pages/index/index.vue](src/pages/index/index.vue) | 聚合(含 IM 未读) |
| 3 | 消息中心 | [pages/message/index.vue](src/pages/message/index.vue) | `/api/system/user/message/*` |
| 4 | 应用中心 | [pages/menu/index.vue](src/pages/menu/index.vue) | `/api/auth/menus` |
| 5 | 我的 | [pages/profile/index.vue](src/pages/profile/index.vue) | `/api/system/users/*` |
| 6 | 通讯录 | [modules/contacts/pages/](src/modules/contacts/pages/index.vue) | `/api/system/*` + `/api/im/users/*` |
| 7 | 通知公告 | [modules/notice/pages/list.vue](src/modules/notice/pages/list.vue) | `/api/system/notice` |
| 8 | 文件中心 | [modules/file/pages/index.vue](src/modules/file/pages/index.vue) | `/api/upload/**` |
| 9 | Token 管理 | [modules/token/pages/index.vue](src/modules/token/pages/index.vue) | `/api/auth/token` |
| 10 | 监控 | [modules/monitor/pages/server.vue](src/modules/monitor/pages/server.vue) | `/api/system/monitor/*` |
| 11 | 字典查询 | [modules/dict/pages/list.vue](src/modules/dict/pages/list.vue) | `/api/system/dict*` |
| 12 | **IM 即时通讯** | [modules/im/pages/](src/modules/im/pages/conversation-list.vue) | `/api/im/*` + MQTT `ws://host:8083/mqtt` |

## IM 模块接入说明

1. App 启动时(`App.vue onLaunch`),若已登录则调用 `useImStore().connectMqtt()`
2. mqtt-client.ts 建立 `ws://host:8083/mqtt` 连接,username 传 JWT
3. 订阅 `im/p2p/{userId}/inbox`、`im/group/{groupId}/inbox`、`im/sys/{userId}/system`
4. 收到消息 → 更新 store + 通过 `uni.$emit('im:message')` 通知聊天窗口
5. 发送消息 → 直接 publish 到对应 topic,服务端落库后推 inbox
6. 断线自动重连 + 重订阅

详细协议见 [`docs/im/api-design.md`](../../docs/im/api-design.md)。

## 二次开发

新增模块请放在 `src/modules/extension/{模块名}/` 下,**不要修改通用模块**。
完整指引见 [`docs/app/extension.md`](../../docs/app/extension.md)。

## 注意事项

- **生产前必须替换 RSA 加密**:`src/utils/rsa.ts` 当前为占位实现,生产前请引入 `jsencrypt` 并配置真实公钥。
- **Tabbar 图标**:`static/tabbar/` 下 8 个 PNG 需替换为设计稿(详见 [static/README.md](static/README.md))。
- **MQTT 平台兼容**:微信小程序不支持 MQTT 长连接,IM 模块在小程序端走 HTTP 轮询 + 微信模板消息(详见 [docs/im/architecture.md §1.1](../../docs/im/architecture.md))。
- **MQTT 鉴权**:CONNECT 包的 username **必须传 JWT**,不要传用户名。
- **响应格式**:后端成功 `code = 0`(非 200),`utils/request.ts` 已适配。

## License

Apache 2.0 — 与 mica-admin 一致。