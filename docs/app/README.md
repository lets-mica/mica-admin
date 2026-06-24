# MICA 移动 App — 设计文档

> 基于 uniapp(Vue 3 + Vite)的移动端 App,严格映射 mica-admin 后端现有能力,
> 为基于 mica-admin 的二次开发方提供移动端基线方案。

## 设计原则

1. **忠实映射**:App 模块一一对应 mica-admin-server 中已经实现的控制器,**不画饼**。
2. **二次开发友好**:App 工程结构为二次开发预留明确扩展点,新增功能不修改通用模块。
3. **零后端改造**:1.0 版本所有功能**直接复用** mica-admin 现有接口,**不需要修改后端**。

## 文档导航

| 文档 | 说明 |
|---|---|
| [README.md](./README.md) | 产品定位、用户、价值、IA 概述(本文) |
| [features.md](./features.md) | 12 个开箱即用模块的功能详述(含 IM) |
| [wireframes.md](./wireframes.md) | 各模块 ASCII 草图(可直接给设计师重画) |
| [api-mapping.md](./api-mapping.md) | 接口对接清单(method/path/入参/出参/权限) |
| [extension.md](./extension.md) | 二次开发扩展点(明确哪些需要新写后端) |
| [roadmap.md](./roadmap.md) | 迭代路线图(MVP → 1.0 → 1.1 → 2.0) |

## 关联模块

- **IM 模块**([docs/im/](../im/README.md)):基于 mica-mqtt 的实时通讯模块,
  **已在 mica-admin-server 中完整接入**(broker 端口 1883/8083,JWT 鉴权)。
  App 端 1.0 即可直接复用,详见 [extension.md §3](./extension.md#3-通讯录拨打im)。

## 产品定位

| 维度 | 内容 |
|---|---|
| **一句话定位** | 让 mica-admin 体系内的员工与管理员,在手机上完成 **待办、消息、即时通讯、查资料、轻量办公** |
| **目标用户** | 内部员工(查消息、找同事、改资料、聊天) + 各级管理员(审批前序工作、看监控日志) |
| **核心价值** | 复用 mica-admin 已有的 RBAC、JWT、消息、文件存储、**IM(mica-mqtt)**,**零后端改造搭出 App** |
| **不做什么** | 视频会议、考勤打卡 —— 这些留到 v1.1+ 评估 |

### App 命名建议(挑一个)

- `MICA Work` — 国际化方向
- `米卡办公` — 内部使用,辨识度高
- `MICA 掌中管` — 强调"掌中"管理

## mica-admin 现存能力清单(已 grep 验证)

下表是 mica-admin-server 里 **真实存在** 的控制器,App 模块只能从这里选:

| 能力域 | 控制器 | 主要端点 |
|---|---|---|
| 认证 | `AuthController` | `/api/auth/captcha` `public-key` `info` `menus` |
| 登录 | Spring Security | `/api/session` `/api/logout` |
| 个人中心 | `SysUserController` | `/api/system/users/center` `updatePass` `avatar` `updateEmail` |
| 消息 | `SysUserMessageController` | `/api/system/user/message/unread` `read/{id}` `read-all` |
| 消息管理(管理员) | `SysMessageController` | `/api/system/message` `publish/{id}` |
| 通知公告 | `SysNoticeController` | `/api/system/notice` `/{id}` |
| 部门 | `SysDeptController` | 树/列表 |
| 岗位 | `SysPostController` | 列表 |
| 字典 | `SysDictController` `SysDictInfoController` | 按 type 查 |
| 角色 | `SysRoleController` | 列表/分配菜单 |
| 用户查询 | `SysUserController` | `/api/system/users?blurry=` |
| 文件 | `SysFileStorageController` | 上传/下载/预览(`/api/upload/**`) |
| 监控 | `SysMonitorController` | `/api/system/monitor/{server,sql,redis}` |
| 日志 | `SysLogController` | 操作/登录/异常日志 |
| 配置 | `SysConfigController` | 系统参数 |
| Token | `AuthTokenController` | `/api/auth/token` |
| 邮箱验证码 | `SysVerifyController` | `/api/system/code/resetEmail` |
| IM 会话与消息 | `ImConversationController` | `/api/im/conversations` `p2p` `{id}/messages` `mark-read` `unread-total` `mark-all-read` |
| IM 群管理 | `ImGroupController` | `/api/im/groups` `my` `{id}` `{id}/members` |
| IM 用户查询 | `ImUserController` | `/api/im/users/search` `batch` |
| IM 监控(管理员) | `ImStatsController` | `/admin/im/stats/online` `broker` |
| IM 实时通道 | `MqttAuthInterceptor` + 内嵌 broker | TCP `1883` / WebSocket `8083` |

**不在 mica-admin 中、需要二次开发的能力**(详见 [extension.md](./extension.md)):

- ❌ 工作流/审批引擎(无 flowable/activiti/camunda)
- ❌ 考勤打卡
- ❌ 视频会议

> IM 与实时推送 **已就绪**(内嵌 mica-mqtt broker),无需后端改造即可在 App 端对接。
> MQTT Topic 协议与 HTTP 接口清单详见 [docs/im/api-design.md](../im/api-design.md) 与 [api-mapping.md](./api-mapping.md#模块-12-im-即时通讯)。

## 信息架构

整体采用 **底部 4 Tab + 工作台聚合 + 第二屏会话** 的扁平结构(对标钉钉/企业微信/飞书):

```
┌──────────────────────────────────────┐
│            MICA 移动 App              │
├────────┬────────┬──────────┬───────────┤
│  🏠    │  💬    │   📋     │   👤      │
│ 工作台 │ 消息   │ 应用中心 │  我的     │
└────────┴────────┴──────────┴───────────┘
```

> 「消息」Tab 从 1.0 起包含两类入口:
> - **系统消息**(现有 `sys_user_message`):通知公告、待办等的聚合;
> - **即时通讯**(mica-admin IM 模块):单聊、群聊、会话列表、群管理。
> App 内从通讯录用户卡片点"发消息"会直接拉起对应单聊会话。

**4 个 Tab 的真实后端对应**:

| Tab | 含子功能 | 后端对应接口 |
|---|---|---|
| 工作台 | 问候/未读统计(IM + 系统消息)/最新公告/快捷入口 | `/api/auth/info` + `/api/im/conversations/unread-total` + `/api/system/user/message/unread` + `/api/system/notice` |
| 消息 | 系统消息 + IM 会话列表 + 单聊/群聊 | `/api/system/user/message/*` + `/api/im/conversations` + `/api/im/groups/my` + MQTT WS 8083 |
| 应用中心 | 从 `/api/auth/menus` 动态生成 | `/api/auth/menus` |
| 我的 | 个人资料/改密/改头像/改邮箱/退出 | `/api/system/users/*` |

> **关键设计**:应用中心 Tab 直接吃后端动态菜单 —— 后台给用户配什么菜单,App 端就显示什么应用。二次开发方加新菜单时,App 自动跟上。

## 技术栈

| 类别 | 推荐 | 理由 |
|---|---|---|
| 基础框架 | uniapp x(Vue 3 + Vite 6) | 与 mica-admin-web 同代 |
| UI 库 | uni-ui 或 uView Plus | 按需引入,跨端一致 |
| 状态管理 | Pinia 3 | 与 mica-admin-web 对齐 |
| 网络请求 | 自封装 request.ts | **注意 mica-admin 成功 code = 0**(同 mica-admin-web) |
| 图表 | uCharts | 跨端、轻量 |
| IM 实时通道(1.0) | mqtt.js 5.x(`ws://host:8083/mqtt`) | mica-mqtt broker 已内置,JWT 鉴权已实现 |
| 推送(1.0) | MQTT topic `im/sys/{userId}/system` 接收系统消息推送;离线消息由 `/api/system/user/message/unread` 兜底 | 实时 + 离线双通道 |
| 存储 | pinia-plugin-persistedstate(JWT)+ uni.setStorageSync |  |
| 路由 | uniapp pages.json + 条件编译 |  |

## App 1.0 可交付模块清单

| # | 模块 | 可交付 | 后端依赖 | 二次开发后扩展 |
|---|---|---|---|---|
| 1 | 登录(RSA + 算术验证码) | ✅ | `/api/auth/*` + `/api/session` | - |
| 2 | 工作台 | ✅ | 聚合现有接口(含 IM 未读) | - |
| 3 | 消息中心 | ✅ | `/api/system/user/message/*` | 点击跳转业务单据 |
| 4 | 应用中心(动态菜单) | ✅(WebView 兜底) | `/api/auth/menus` | 原生化 |
| 5 | 我的 | ✅ | `/api/system/users/*` | - |
| 6 | 通讯录(读) | ✅ | `/api/system/users` + `/api/system/dept` | 拨打 |
| 7 | 通知公告(读) | ✅ | `/api/system/notice` | 评论 |
| 8 | 文件上传/预览 | ✅ | `/api/upload/**` | 协作 |
| 9 | Token 管理(管理员) | ✅ | `/api/auth/token` | - |
| 10 | 监控(简化版) | ✅ | `/api/system/monitor/server` | 告警推送 |
| 11 | 字典查询 | ✅ | `/api/system/dict` + `/api/system/dict-info` | - |
| 12 | **IM 即时通讯** | ✅ | `/api/im/*` + MQTT `ws://host:8083/mqtt` | 音视频通话/已读回执 |
| - | 工作流/审批 | ❌ | - | 需新增 `sys_approval` 表 |
| - | 考勤打卡 | ❌ | - | 需新增 `sys_attendance` 表 |
| - | 视频会议 | ❌ | - | 需对接第三方 SDK |

> 总计 **12 个开箱即用模块**,**对应 mica-admin 现有 21 个控制器**(system: 16 + im: 4 + auth-token: 1)。
> **通讯录拨打** 与 **实时推送**(系统消息)已通过 IM MQTT topic 间接提供,
> 如需原生拨打/系统级推送(APNs/华为/小米通道),参见 [extension.md §3/§6](./extension.md)。

## 二次开发友好性

App 工程结构设计原则:**通用模块不修改,二次开发只新增**。

```
src/
├── api/                  # 自动生成(从 swagger)
│   ├── Api.ts            # 系统模块客户端
│   └── im/               # IM 模块客户端(api/im.ts、api/group.ts、api/user.ts)
├── modules/              # 业务模块
│   ├── auth/             # 登录(RSA + captcha)— 通用,不修改
│   ├── message/          # 消息中心(现有 sys_user_message)— 通用
│   ├── menu/             # 应用中心(动态菜单)— 通用
│   ├── profile/          # 我的(现有 sys_user/users)— 通用
│   ├── contacts/         # 通讯录(现有 sys_dept/sys_user)— 通用
│   ├── notice/           # 通知公告(现有 sys_notice)— 通用
│   ├── im/               # ⭐ 即时通讯(单聊/群聊/会话/群管理),通用,不修改
│   │   ├── mqtt-client.ts        # mqtt.js 5.x 客户端封装
│   │   ├── conversation-list.ts  # 会话列表 store
│   │   ├── chat-window.ts        # 单聊/群聊窗口 store
│   │   └── group-mgr.ts          # 群管理 store
│   └── extension/        # ⭐ 二次开发预留
│       ├── approval/     # 审批(等 sys_approval)— 二次开发
│       ├── attendance/   # 考勤(等 sys_attendance)— 二次开发
│       └── README.md     # 二次开发接入说明
├── pages.json            # 路由
└── config/
    └── env.ts            # 含 MQTT_WS_URL、MQTT_TOPIC_PREFIX
```

二次开发流程:

1. 后端新增表 + 服务层 + Controller
2. `pnpm api` 重新生成 swagger 客户端
3. 在 `extension/{模块名}/` 下写实现
4. 替换占位 UI(灰色 + "即将上线")

**复用 IM 能力的二次开发**:`extension/` 下任何模块若需要"消息触达"或"群发通知",
可直接订阅 `im/sys/{userId}/system` topic 接收服务端推送(详见 [extension.md §6](./extension.md#6-实时推送)),
不需要额外搭建推送通道。

## 文档维护

- 任何 mica-admin 后端接口变更,同步更新 [api-mapping.md](./api-mapping.md)
- 任何新增二次开发模块,同步更新 [extension.md](./extension.md)
- 草图变更同步更新 [wireframes.md](./wireframes.md)
- IM 模块相关变更同步更新 [docs/im/](../im/README.md) 与本目录的 IM 章节
  (Topic 协议在 [docs/im/api-design.md §3](../im/api-design.md),
  HTTP 接口在本目录 [api-mapping.md §模块 12](./api-mapping.md#模块-12-im-即时通讯))
- 最近一次重大更新:**IM 模块全量纳入**(2026-06,对应 mica-mqtt broker 集成 + 12 个 HTTP 端点上线)。