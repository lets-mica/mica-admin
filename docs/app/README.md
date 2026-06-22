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
| [features.md](./features.md) | 11 个开箱即用模块的功能详述 |
| [wireframes.md](./wireframes.md) | 各模块 ASCII 草图(可直接给设计师重画) |
| [api-mapping.md](./api-mapping.md) | 接口对接清单(method/path/入参/出参/权限) |
| [extension.md](./extension.md) | 二次开发扩展点(明确哪些需要新写后端) |
| [roadmap.md](./roadmap.md) | 迭代路线图(MVP → 1.0 → 1.1 → 2.0) |

## 产品定位

| 维度 | 内容 |
|---|---|
| **一句话定位** | 让 mica-admin 体系内的员工与管理员,在手机上完成 **待办、消息、查资料、轻量办公** |
| **目标用户** | 内部员工(查消息、找同事、改资料) + 各级管理员(审批前序工作、看监控日志) |
| **核心价值** | 复用 mica-admin 已有的 RBAC、JWT、消息、文件存储,**零后端改造搭出 App** |
| **不做什么** | 复杂 IM、视频会议、考勤打卡 —— 这些留到 v1.1+ 评估 |

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

**不在 mica-admin 中、需要二次开发的能力**(详见 [extension.md](./extension.md)):

- ❌ 工作流/审批引擎(无 flowable/activiti/camunda)
- ❌ 考勤打卡
- ❌ 即时通讯
- ❌ 实时推送(mica-mqtt 在 mica-admin-server **未接入**,仅 README 推荐)

## 信息架构

整体采用 **底部 4 Tab + 工作台聚合** 的扁平结构(对标钉钉/企业微信/飞书):

```
┌──────────────────────────────────────┐
│            MICA 移动 App              │
├────────┬────────┬──────────┬───────────┤
│  🏠    │  📨    │   📋     │   👤      │
│ 工作台 │ 消息   │ 应用中心 │  我的     │
└────────┴────────┴──────────┴───────────┘
```

**4 个 Tab 的真实后端对应**:

| Tab | 含子功能 | 后端对应接口 |
|---|---|---|
| 工作台 | 问候/未读统计/最新公告/快捷入口 | `/api/auth/info` + `/api/system/user/message/unread` + `/api/system/notice` |
| 消息 | 我的消息、标记已读 | `/api/system/user/message/*` |
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
| 推送(1.0) | 前端轮询 `/unread` | mica-mqtt 未接入,1.0 不接 |
| 存储 | pinia-plugin-persistedstate(JWT)+ uni.setStorageSync |  |
| 路由 | uniapp pages.json + 条件编译 |  |

## App 1.0 可交付模块清单

| # | 模块 | 可交付 | 后端依赖 | 二次开发后扩展 |
|---|---|---|---|---|
| 1 | 登录(RSA + 算术验证码) | ✅ | `/api/auth/*` + `/api/session` | - |
| 2 | 工作台 | ✅ | 聚合现有接口 | - |
| 3 | 消息中心 | ✅ | `/api/system/user/message/*` | 点击跳转业务单据 |
| 4 | 应用中心(动态菜单) | ✅(WebView 兜底) | `/api/auth/menus` | 原生化 |
| 5 | 我的 | ✅ | `/api/system/users/*` | - |
| 6 | 通讯录(读) | ✅ | `/api/system/users` + `/api/system/dept` | 拨打/IM |
| 7 | 通知公告(读) | ✅ | `/api/system/notice` | 评论 |
| 8 | 文件上传/预览 | ✅ | `/api/upload/**` | 协作 |
| 9 | Token 管理(管理员) | ✅ | `/api/auth/token` | - |
| 10 | 监控(简化版) | ✅ | `/api/system/monitor/server` | 告警推送 |
| 11 | 字典查询 | ✅ | `/api/system/dict` + `/api/system/dict-info` | - |
| - | 工作流/审批 | ❌ | - | 需新增 `sys_approval` 表 |
| - | 考勤打卡 | ❌ | - | 需新增 `sys_attendance` 表 |
| - | 实时推送 | ❌ | - | 需接入 mica-mqtt |

> 总计 **11 个开箱即用模块**,**对应 mica-admin 现有 16 个控制器**。

## 二次开发友好性

App 工程结构设计原则:**通用模块不修改,二次开发只新增**。

```
src/
├── api/                  # 自动生成(从 swagger)
│   └── Api.ts
├── modules/              # 业务模块
│   ├── auth/             # 登录(RSA + captcha)— 通用,不修改
│   ├── message/          # 消息中心(现有 sys_user_message)— 通用
│   ├── menu/             # 应用中心(动态菜单)— 通用
│   ├── profile/          # 我的(现有 sys_user/users)— 通用
│   ├── contacts/         # 通讯录(现有 sys_dept/sys_user)— 通用
│   ├── notice/           # 通知公告(现有 sys_notice)— 通用
│   └── extension/        # ⭐ 二次开发预留
│       ├── approval/     # 审批(等 sys_approval)— 二次开发
│       ├── attendance/   # 考勤(等 sys_attendance)— 二次开发
│       └── README.md     # 二次开发接入说明
├── pages.json            # 路由
└── config/
    └── env.ts
```

二次开发流程:

1. 后端新增表 + 服务层 + Controller
2. `pnpm api` 重新生成 swagger 客户端
3. 在 `extension/{模块名}/` 下写实现
4. 替换占位 UI(灰色 + "即将上线")

## 文档维护

- 任何 mica-admin 后端接口变更,同步更新 [api-mapping.md](./api-mapping.md)
- 任何新增二次开发模块,同步更新 [extension.md](./extension.md)
- 草图变更同步更新 [wireframes.md](./wireframes.md)