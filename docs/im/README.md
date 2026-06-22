# mica-im — 实时通讯模块(IM)

> 基于 [mica-mqtt](https://gitee.com/dromara/mica-mqtt) 的轻量级即时通讯模块,
> 作为 mica-admin 的官方扩展模块,打通**管理端 + App 端**,复用 mica-admin 现有用户/部门/权限/文件能力。

## 文档导航

| 文档 | 说明 |
|---|---|
| [README.md](./README.md) | 总览、定位、范围、里程碑(本文) |
| [requirements.md](./requirements.md) | 需求文档(用户故事、功能/非功能需求、验收标准) |
| [architecture.md](./architecture.md) | 技术架构(分层、mica-mqtt 接入、鉴权、可靠性) |
| [data-model.md](./data-model.md) | 数据模型(SQL、实体、Redis、Topic 设计) |
| [api-design.md](./api-design.md) | HTTP REST + MQTT Topic 设计 |
| [roadmap.md](./roadmap.md) | 分阶段实施计划 + PR 拆分 |

## 产品定位

| 维度 | 内容 |
|---|---|
| **一句话定位** | 打通 mica-admin 管理端 + App 端,提供轻量、可自部署、可二开的实时通讯能力 |
| **目标用户** | 已使用/基于 mica-admin 二次开发的团队 |
| **核心价值** | 开源、可自部署、数据自主、与 mica-admin 用户/组织/权限/文件深度打通 |
| **不做的事** | 不做钉钉级 IM(音视频/智能助手/打卡);不做商业 SaaS;不做企业级付费功能 |

### 差异化

- **开源 + 自部署** —— 对标 Rocket.Chat / Mattermost,但与 mica-admin 一体化
- **打通 mica-admin** —— 不是独立 IM,用户/组织/权限/文件全部复用
- **打通 mica-mqtt** —— Java 技术栈统一,与 mica-admin 集成简单
- **打通 mica App** —— [docs/app/](../app/README.md) 中 App 端原生集成

## 范围(本次)

| 阶段 | 内容 | 工作量 | 状态 |
|---|---|---|---|
| **Phase 0** | 消息推送通道(mica-mqtt 接入 + sys_message 推送升级) | 1-2 周 | 📝 计划中 |
| **Phase 1** | IM 单聊 MVP(1-1 文本 + 会话 + 离线 + 未读) | 2-3 周 | 📝 计划中 |
| **Phase 1.1** | 群聊(创建/邀请/解散/群消息) | 2-3 周 | 📝 计划中 |
| Phase 2.0 | 已读/撤回/@/引用/富消息 | - | ❌ 暂不做 |
| Phase 2.1 | 音视频 | - | ❌ 暂不做 |
| Phase 2.2 | 第三方推送(uniPush) | - | ❌ 暂不做 |

> 截止到 **Phase 1.1**,产出"可用的轻量 IM + 管理平台一体化"基座。

## 技术栈

| 类别 | 选型 | 理由 |
|---|---|---|
| MQTT Broker | **mica-mqtt-server** | Java 系、Apache 2.0、Spring Boot 集成简单 |
| MQTT 客户端(原生) | mica-mqtt-client | 统一技术栈 |
| MQTT 客户端(Web) | mqtt.js (Eclipse Paho) | 浏览器唯一靠谱选择 |
| 业务框架 | Spring Boot 2.7(同 mica-admin) | 复用 |
| ORM | MyBatis-Plus 3.5(同 mica-admin) | 复用 |
| 缓存 | Redis(同 mica-admin) | 离线消息队列、在线状态 |
| 数据库 | MySQL 5.7+(同 mica-admin) | 复用 |
| 消息存储 | MySQL + Redis(短期在线/长期归档) | 平衡查询性能与成本 |
| 实时推送(App) | MQTT 原生 | 协议支持 |
| 实时推送(小程序) | HTTP 轮询 + 微信模板消息 | 小程序不支持长连接 |

## 与 mica-admin 现有能力集成点

```
┌────────────────────────────────────────────────────┐
│                  mica-im 模块                       │
├────────────────────────────────────────────────────┤
│  MqttAuthInterceptor  ──►  JwtTokenService 鉴权   │
│  ImUserService        ──►  SysUser 查询用户信息   │
│  ImGroupService       ──►  SysDept 查询部门信息   │
│  ImPermissionService  ──►  SysRole 检查角色权限   │
│  ImFileService        ──►  SysFileStorage 上传   │
│  ImMessageService     ──►  SysMessage 双向打通   │
│  ImPushService        ──►  SysUserMessage 离线   │
└────────────────────────────────────────────────────┘
```

**复用原则**:

- ✅ **完全复用** sys_user、sys_dept、sys_role、sys_file、sys_message、sys_user_message
- 🆕 **新增** im_conversation、im_conversation_member、im_message、im_group
- ❌ **不重复**用户、组织、权限、文件相关表

## 与 App 端集成

App 端参考 [docs/app/](../app/README.md):

| App 端 | 集成方式 |
|---|---|
| 原生 App(iOS/Android) | MQTT 原生客户端订阅 topic |
| 微信小程序 | HTTP + 微信模板消息(降级方案) |
| App 内 WebView | mqtt.js(WebSocket over MQTT) |

> App 1.0 已经预留 `extension/im/` 占位 UI(详见 [docs/app/wireframes.md §二次开发占位 UI](../app/wireframes.md))。
> Phase 1 开始后,占位 UI 替换为真正的 IM 页面。

## 里程碑

```
v0.1 需求/设计    ──────────────────┐
                                  │ 当前
                                  ↓
Phase 0 推送通道  ─────────────────┐
                                │ 1-2 周
                                ↓
Phase 1 单聊 MVP  ─────────────────┐
                                │ 2-3 周
                                ↓
Phase 1.1 群聊    ─────────────────┐
                                │ 2-3 周
                                ↓
Phase 2.x 高阶能力 ─────────────────
                                │
                                └─ 视社区反馈启动
```

## 贡献

- 提交 PR 前先读 [roadmap.md](./roadmap.md) 确认阶段
- 数据模型变更需同步更新 [data-model.md](./data-model.md)
- API 变更需同步更新 [api-design.md](./api-design.md)

## 引用

- [mica-mqtt](https://gitee.com/dromara/mica-mqtt) — MQTT Broker/Client
- [mica-admin](https://gitee.com/596392912/mica) — 后台管理框架
- [docs/app/](../app/README.md) — App 端设计文档
- [MQTT 3.1.1 协议规范](https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html)