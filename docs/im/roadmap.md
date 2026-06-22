# IM 模块实施路线图

> 把 [requirements.md](./requirements.md) 中 Phase 0 / Phase 1 / Phase 1.1 的需求,
> 拆分成可执行的 PR,逐步交付,每个 PR 都能独立测试和合并。

## 总览

```
Phase 0: 推送通道     ────────┐
                              │ 1-2 周 · 3 个 PR
                              ↓
Phase 1: 单聊 MVP      ────────┐
                              │ 2-3 周 · 5 个 PR
                              ↓
Phase 1.1: 群聊       ────────┐
                              │ 2-3 周 · 4 个 PR
                              ↓
Phase 2.x: 富消息等     ──────── 后续评估
```

**总投入**:**5-8 周**(单人,含调试和文档),**12 个 PR**。

---

## Phase 0 — 推送通道(1-2 周 · 3 PR)

> **目标**:把 mica-mqtt 接入 mica-admin,把系统消息推送从轮询升级为实时。
> 这一步**不改数据库**,只新增依赖 + 配置 + 推送调用点。

### PR-0.1: mica-mqtt 依赖与配置(0.5 天)

**范围**:

- `pom.xml` 引入 `mica-mqtt-spring-boot-starter` + `mica-mqtt-server`
- `application.yml` 配置 broker 端口、JWT 拦截器
- 新增 `ImMqttConfig` 启动内嵌 broker
- 验证:`java -jar` 启动后,`netstat -an | grep 1883` 能看到监听

**涉及文件**:

```
mica-admin-server/pom.xml                            [MODIFY]   # 加 mica-mqtt 依赖
mica-admin-server/src/main/resources/application.yml [MODIFY]
mica-admin-server/src/main/java/net/dreamlu/mica/admin/im/
  └── config/
      └── ImMqttConfig.java                          [NEW]
```

**验收**:

- [ ] broker 启动成功,日志显示 `mqtt server started on port 1883`
- [ ] 用 mqtt.fx 或 mqtt.js 连上 `localhost:1883`(无鉴权)
- [ ] 单元测试覆盖 `ImMqttConfig`

---

### PR-0.2: JWT 鉴权拦截器(1 天)

**范围**:

- 实现 `MqttAuthInterceptor`,CONNECT 时校验 JWT
- 提取 userId,绑定到 `clientId`(Redis)
- 实现 `MqttTopicFilter`,基础白名单:`im/status/{userId}/state` / `im/sys/{userId}/system`

**涉及文件**:

```
mica-admin-server/src/main/java/net/dreamlu/mica/admin/im/
  ├── auth/
  │   ├── MqttAuthInterceptor.java                  [NEW]
  │   └── MqttSessionManager.java                   [NEW]
  └── topic/
      └── MqttTopicFilter.java                      [NEW]
```

**验收**:

- [ ] 无效 JWT → 拒绝连接(CONNACK 错误码 5)
- [ ] 有效 JWT → 通过,Redis 存 `im:mqtt:client:{clientId} = userId`
- [ ] 越权订阅 → 拒绝
- [ ] 单元测试覆盖

---

### PR-0.3: 系统消息实时推送(2-3 天)

**范围**:

- 实现 `MqttMessageListener`,全局监听 `im/*` topic
- `SysMessageServiceImpl.publish()` 增加 MQTT 推送
- 复用现有 `sys_user_message` 兜底离线
- Web 端集成 mqtt.js,订阅 `im/sys/{userId}/system`
- Web 端 UI:收到推送后用 Notification API 提示

**涉及文件**:

```
mica-admin-server/src/main/java/net/dreamlu/mica/admin/im/
  ├── listener/
  │   └── MqttMessageListener.java                  [NEW]
  └── service/
      └── ImPushService.java                        [NEW]
mica-admin-server/src/main/java/net/dreamlu/mica/admin/project/system/service/impl/
  └── SysMessageServiceImpl.java                    [MODIFY]   # 在 publish() 里加推送
mica-admin-web/src/
  ├── utils/mqtt.ts                                 [NEW]
  ├── components/NotificationCenter.vue             [NEW]
  └── store/notification.ts                         [NEW]
```

**验收**:

- [ ] 管理员发布 `sys_message` → 在线用户 5 秒内收到 Web 推送
- [ ] 离线用户下次登录后,从 `sys_user_message` 拉到未读
- [ ] 端到端测试通过

**⚠️ Phase 0 完成**:这是 mica-mqtt 接入的最小验证,可以独立交付。

---

## Phase 1 — 单聊 MVP(2-3 周 · 5 PR)

> **目标**:1-1 文本聊天完整可用,App + Web 端都能用。
> 引入 MySQL 表 + Redis + 完整业务逻辑。

### PR-1.1: 数据模型与基础 CRUD(2 天)

**范围**:

- 新建表 `im_conversation`、`im_conversation_member`、`im_message`
- 写迁移脚本 `docs/database/im-schema-phase-1.sql`
- Entity + Mapper XML
- Service 层:`ImConversationService.getOrCreateP2P()`
- Controller:`GET /api/im/conversations`、`GET /api/im/conversations/p2p/{userId}`、`GET /api/im/conversations/{id}`

**涉及文件**:

```
docs/database/
  └── im-schema-phase-1.sql                         [NEW]
mica-admin-server/src/main/java/net/dreamlu/mica/admin/im/
  ├── entity/
  │   ├── ImConversation.java                       [NEW]
  │   ├── ImConversationMember.java                 [NEW]
  │   └── ImMessage.java                            [NEW]
  ├── mapper/
  │   ├── ImConversationMapper.java                 [NEW]
  │   ├── ImConversationMapper.xml                  [NEW]
  │   ├── ImConversationMemberMapper.java           [NEW]
  │   ├── ImConversationMemberMapper.xml            [NEW]
  │   ├── ImMessageMapper.java                      [NEW]
  │   └── ImMessageMapper.xml                       [NEW]
  ├── service/
  │   ├── IImConversationService.java               [NEW]
  │   └── impl/ImConversationServiceImpl.java       [NEW]
  └── controller/
      └── ImConversationController.java             [NEW]
```

**验收**:

- [ ] 数据库迁移脚本可重复执行
- [ ] `GET /api/im/conversations/p2p/2` 返回会话(若不存在则创建)
- [ ] 单元测试覆盖 Service 层

---

### PR-1.2: 单聊消息发送与接收(3 天)

**范围**:

- `MqttMessageListener` 扩展:处理 `im/p2p/*/to/*` topic
- 消息入库(去重、更新时间)
- 更新会话最后消息
- 接收方订阅 topic 实现:`im/p2p/{myId}/from/+`

**涉及文件**:

```
mica-admin-server/src/main/java/net/dreamlu/mica/admin/im/
  ├── listener/
  │   └── ImP2pMessageHandler.java                  [NEW]
  ├── service/
  │   ├── IImMessageService.java                    [NEW]
  │   └── impl/ImMessageServiceImpl.java            [NEW]
  └── topic/
      └── ImP2pTopicHelper.java                     [NEW]
```

**验收**:

- [ ] 用户 A 发送消息,用户 B 收到(在线时)
- [ ] 消息入库,带 server_msg_id
- [ ] `client_msg_id` 去重生效
- [ ] 单元测试 + 集成测试

---

### PR-1.3: 历史消息拉取与会话列表(2 天)

**范围**:

- `GET /api/im/conversations/{id}/messages` 分页
- `GET /api/im/conversations` 列表(含最后一条消息预览)
- 性能优化:加 `idx_conv_created` 索引
- 会话最后消息预览(text 类型截前 50 字)

**涉及文件**:

```
mica-admin-server/src/main/java/net/dreamlu/mica/admin/im/
  ├── service/impl/ImConversationServiceImpl.java  [MODIFY]   # 列表聚合
  ├── service/impl/ImMessageServiceImpl.java       [MODIFY]   # 分页
  └── controller/ImMessageController.java           [NEW]
```

**验收**:

- [ ] 会话列表按最后消息时间倒序
- [ ] 历史消息分页性能 OK(< 300ms,P99)
- [ ] 索引生效(EXPLAIN 验证)

---

### PR-1.4: 未读消息与已读(2 天)

**范围**:

- 接收消息时 INCR Redis `im:unread:{userId}`,更新 MySQL `unread_count`
- `GET /api/im/unread/count` 接口
- `PUT /api/im/conversations/{id}/read` 标记已读
- 分布式锁防并发(用 Redis SETNX)

**涉及文件**:

```
mica-admin-server/src/main/java/net/dreamlu/mica/admin/im/
  ├── service/
  │   ├── IImUnreadService.java                     [NEW]
  │   └── impl/ImUnreadServiceImpl.java             [NEW]
  └── controller/ImUnreadController.java            [NEW]
```

**验收**:

- [ ] 未读数实时准确
- [ ] 标记已读后未读数归零
- [ ] 多端登录时未读数同步(以服务端为准)

---

### PR-1.5: 前端集成 — Web + App(2-3 天)

**范围**:

- **Web 端**(mica-admin-web):
  - `src/utils/mqtt.ts` 封装连接/重连
  - `src/store/im.ts` Pinia store
  - `src/views/im/ConversationList.vue` 会话列表
  - `src/views/im/Chat.vue` 聊天页
  - `src/components/ImMessageBubble.vue` 消息气泡

- **App 端**(App 1.0 `extension/im/`):
  - 同样 store + views + components
  - App 端用原生 mqtt-client

**涉及文件**:

```
mica-admin-web/src/
  ├── utils/mqtt.ts                                 [NEW]
  ├── store/im.ts                                   [NEW]
  ├── views/im/
  │   ├── ConversationList.vue                      [NEW]
  │   └── Chat.vue                                  [NEW]
  └── components/
      └── ImMessageBubble.vue                       [NEW]

mica-admin-web/src/router/routes/modules/im.ts      [NEW]   # 路由
```

> App 端代码在独立的 App 工程,不在 mica-admin-web。

**验收**:

- [ ] Web 端发起单聊,流程完整
- [ ] App 端发起单聊,流程完整
- [ ] 多端同步(两设备登录同一账号,消息互通)
- [ ] UI 体验 OK(消息滚动、发送状态、未读徽标)

**⚠️ Phase 1 完成**:单聊 MVP 可用,可以给种子用户试用。

---

## Phase 1.1 — 群聊(2-3 周 · 4 PR)

> **目标**:群创建、邀请、群消息、部门群自动同步。

### PR-1.1.1: 群基础数据模型(1 天)

**范围**:

- 新建表 `im_group`、`im_group_member`
- Entity + Mapper + Service
- Controller:`GET /api/im/my/groups`

**涉及文件**:

```
docs/database/im-schema-phase-1-1.sql               [NEW]
mica-admin-server/src/main/java/net/dreamlu/mica/admin/im/
  ├── entity/
  │   ├── ImGroup.java                              [NEW]
  │   └── ImGroupMember.java                        [NEW]
  ├── mapper/
  │   ├── ImGroupMapper.java                        [NEW]
  │   ├── ImGroupMapper.xml                         [NEW]
  │   ├── ImGroupMemberMapper.java                  [NEW]
  │   └── ImGroupMemberMapper.xml                   [NEW]
  ├── service/
  │   ├── IImGroupService.java                      [NEW]
  │   └── impl/ImGroupServiceImpl.java              [NEW]
  └── controller/ImGroupController.java             [NEW]
```

---

### PR-1.1.2: 群管理 API(2 天)

**范围**:

- `POST /api/im/groups` 创建群
- `POST /api/im/groups/{id}/members` 邀请
- `DELETE /api/im/groups/{id}/members/{userId}` 踢出
- `DELETE /api/im/groups/{id}` 解散
- `POST /api/im/groups/{id}/quit` 退群
- 权限校验(仅 owner/admin)
- 入群/退群/解散系统消息推送

**涉及文件**:

```
mica-admin-server/src/main/java/net/dreamlu/mica/admin/im/
  └── service/impl/ImGroupServiceImpl.java          [MODIFY]
```

---

### PR-1.1.3: 群消息收发(2 天)

**范围**:

- `MqttMessageListener` 扩展:处理 `im/group/*/inbox` topic
- 群消息入库,所有群成员订阅接收
- 群成员订阅管理(加入/退群时同步)
- 系统消息推送(入群/退群/解散)

**涉及文件**:

```
mica-admin-server/src/main/java/net/dreamlu/mica/admin/im/
  ├── listener/
  │   └── ImGroupMessageHandler.java                [NEW]
  └── topic/
      └── ImGroupTopicHelper.java                   [NEW]
```

---

### PR-1.1.4: 部门群自动同步 + 前端集成(2-3 天)

**范围**:

- **后端**:
  - 监听 `sys_dept` 创建/成员变更事件
  - 自动创建/同步部门群
  - 在 `SysDeptServiceImpl` 增补事件发布

- **前端**:
  - 群列表 / 群详情 / 群设置页
  - 创建群弹窗(选择联系人)
  - 群消息展示(sender 信息、成员列表)

**涉及文件**:

```
mica-admin-server/src/main/java/net/dreamlu/mica/admin/project/system/service/impl/
  └── SysDeptServiceImpl.java                       [MODIFY]   # 加事件
mica-admin-server/src/main/java/net/dreamlu/mica/admin/im/
  └── listener/
      └── DeptGroupSyncListener.java                [NEW]

mica-admin-web/src/views/im/
  ├── GroupList.vue                                 [NEW]
  ├── GroupDetail.vue                               [NEW]
  └── CreateGroup.vue                               [NEW]
```

**验收**:

- [ ] 部门创建 → 自动建部门群
- [ ] 部门成员变更 → 群成员同步
- [ ] 群消息收发正常
- [ ] 邀请/踢人/解散正常

**⚠️ Phase 1.1 完成**:群聊可用,IM 模块"可发布基座"。

---

## 后续(Phase 2.x,本次不做)

| 阶段 | 内容 | 工作量 |
|---|---|---|
| 2.0 | 文件/图片/语音消息(走 x-file-storage) | 2-3 周 |
| 2.1 | 已读回执 / 撤回 / @ / 引用 | 2 周 |
| 2.2 | 第三方推送(uniPush)解决小程序 | 1 周 |
| 2.3 | 消息搜索 / 历史归档 | 2 周 |
| 3.0 | 音视频(集成声网 SDK) | 3-4 周 |

---

## 每个 PR 的标准流程

```
1. 创建分支:feat/im-pr-{n}-{short-name}
   例:feat/im-pr-0-1-mqtt-deps

2. 提交代码 + 自测
   - 单元测试覆盖率 ≥ 60%
   - 集成测试覆盖核心场景
   - 手动验证(本地)

3. 文档同步
   - 代码变更 → 同步 api-design.md / data-model.md / architecture.md
   - 接口变更 → Swagger 注解 + 同步 docs/im/api-design.md

4. PR 描述包含:
   - 关联需求 ID(如 F1-3)
   - 截图(UI 变更)
   - 测试报告

5. Review + 合并
   - 至少 1 人 review
   - CI 通过
```

---

## 风险与缓解

| 风险 | 影响 | 缓解 |
|---|---|---|
| mica-mqtt 与 Spring Boot 2.7 兼容 | 高 | **Phase 0 第一天就验证**,失败立即找替代 |
| MQTT 鉴权性能 | 中 | Phase 0 压测;Redis 缓存 userId |
| MySQL 写瓶颈 | 中 | Phase 1.1 后评估分区;必要时异步写 |
| 部门群同步事件缺失 | 中 | mica-admin 当前可能没事件,需**自己加** |
| App 端 MQTT 库选型 | 中 | Phase 1 优先用成熟库(mqtt.js、Paho Android) |
| 小程序不支持 MQTT | 低 | Phase 0 暂不管,Phase 2.2 接 uniPush |

---

## 资源与依赖

| 资源 | 链接 |
|---|---|
| mica-mqtt 官方 | https://gitee.com/dromara/mica-mqtt |
| mica-mqtt 文档 | http://wiki.dromara.org/zh-cn/mica-mqtt |
| MQTT 3.1.1 协议 | https://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html |
| MQTT.js(前端) | https://github.com/mqttjs/MQTT.js |
| Paho Android | https://github.com/eclipse/paho.mqtt.android |

---

## 与 App 端集成

参考 [docs/app/](../app/README.md) 中 `extension/im/`:

```
src/modules/extension/im/
├── api/                  # HTTP REST 客户端
├── mqtt/                 # MQTT 客户端封装
├── store/                # Pinia store
├── views/                # 页面
└── components/           # 组件
```

**App 端开发节奏**:

- App 1.0 + IM 单聊 → Phase 1.5 同步开发
- App 1.0 + IM 群聊 → Phase 1.1.4 同步开发

---

## 完成定义(DoD)

每个阶段完成时,以下都必须 OK:

### 代码

- [ ] 所有功能 PR 合并
- [ ] 单元测试覆盖率 ≥ 60%
- [ ] 集成测试通过
- [ ] CI 全绿

### 文档

- [ ] docs/im/ 六份文档完整
- [ ] Swagger 注解完整
- [ ] 部署文档更新

### 部署

- [ ] `java -jar` 单进程启动成功
- [ ] Docker Compose 可启动
- [ ] 配置文件完整

### 演示

- [ ] Web 端录屏(发消息/收消息/历史/未读)
- [ ] App 端录屏(同上)
- [ ] 部门群自动同步录屏

### 用户验收

- [ ] 内部 5+ 用户试用 1 周无严重 bug
- [ ] 性能符合基线(P99 < 1s)

---

## 参考

- [README.md](./README.md) — 总入口
- [requirements.md](./requirements.md) — 需求
- [architecture.md](./architecture.md) — 架构
- [data-model.md](./data-model.md) — 数据模型
- [api-design.md](./api-design.md) — 接口设计
- [docs/app/](../app/README.md) — App 端设计
- [docs/app/extension.md §IM 模块](../app/extension.md) — App 端 IM 占位