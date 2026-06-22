# IM 模块技术架构

> 基于 [README.md](./README.md) 的定位和 [requirements.md](./requirements.md) 的需求,
> 本文档给出 Phase 0 / Phase 1 / Phase 1.1 的完整技术架构。

## 1. 整体架构

### 1.1 分层视图

```
┌──────────────────────────────────────────────────────────────────┐
│                         客户端层(多端)                            │
├──────────────┬──────────────┬──────────────┬─────────────────────┤
│  原生 App    │  微信小程序  │   Web 端     │  其他(预留)         │
│  mqtt-client │ HTTP + 微信推│ mqtt.js over │                     │
│  (TCP 1883)  │ 送(降级)     │ WebSocket    │                     │
│              │              │ (9001)       │                     │
└──────┬───────┴──────┬───────┴──────┬───────┴──────┬──────────────┘
       │              │              │              │
       │ MQTT         │ HTTP         │ MQTT-WS      │
       │              │              │              │
┌──────▼──────────────▼──────────────▼──────────────▼──────────────┐
│                    mica-mqtt-server (Broker)                      │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  TCP Listener (1883)        WS Listener (9001)             │  │
│  │  - 连接接受                                                 │  │
│  │  - 协议解析(MQTT 3.1.1)                                     │  │
│  │  - 主题路由                                                 │  │
│  └────────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  MqttAuthInterceptor  ←─ JwtTokenService 校验              │  │
│  │  - CONNECT 时验证 username (JWT)                            │  │
│  │  - 校验 userId 与 username 一致                            │  │
│  │  - 拒绝 → CONNACK 错误码 5                                 │  │
│  └────────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  MqttMessageListener  (订阅 im/* topic)                    │  │
│  │  - 收到 PUBLISH → 路由到 ImMessageListener                  │  │
│  └────────────────────────────────────────────────────────────┘  │
└───────────────────────────┬──────────────────────────────────────┘
                            │
┌───────────────────────────▼──────────────────────────────────────┐
│              mica-admin-server (业务服务层)                       │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  ImMessageListener (Spring 事件)                           │  │
│  │  - 解析消息体                                              │  │
│  │  - 校验权限(发件人 == token userId)                        │  │
│  │  - 写入 im_message(MySQL)                                  │  │
│  │  - 更新 im_conversation.last_msg_*                          │  │
│  │  - 增加接收方未读数(Redis)                                 │  │
│  │  - 离线用户 → 写 sys_user_message 兜底                     │  │
│  └────────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  ImConversationService                                     │  │
│  │  - 会话创建 / 查询 / 删除                                   │  │
│  │  - 自动创建单聊会话                                         │  │
│  │  - 会话列表聚合(JOIN im_message)                           │  │
│  └────────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  ImGroupService                                            │  │
│  │  - 群 CRUD                                                 │  │
│  │  - 成员管理                                                │  │
│  │  - 部门群自动同步(监听 sys_dept 变更)                      │  │
│  └────────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  ImPushService                                             │  │
│  │  - 离线消息写 sys_user_message                              │  │
│  │  - (Phase 2.x) 第三方推送 uniPush / APNs                   │  │
│  └────────────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  ImFileService                                             │  │
│  │  - (Phase 2.0) 文件消息走 x-file-storage                   │  │
│  └────────────────────────────────────────────────────────────┘  │
└───────────────────────────┬──────────────────────────────────────┘
                            │
┌───────────────────────────▼──────────────────────────────────────┐
│                          数据层                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │   MySQL      │  │    Redis     │  │  x-file-     │            │
│  │              │  │              │  │  storage     │            │
│  │ im_* 表      │  │ 在线状态     │  │ (文件消息)   │            │
│  │ sys_* 复用   │  │ 离线消息队列 │  │              │            │
│  │              │  │ 未读数       │  │              │            │
│  └──────────────┘  └──────────────┘  └──────────────┘            │
└──────────────────────────────────────────────────────────────────┘
```

### 1.2 部署架构

**单进程模式**(推荐,MVP 阶段):

```
┌──────────────────────┐
│   mica-admin-server  │  Spring Boot 2.7
│   + mica-mqtt-server │  内嵌 mica-mqtt-broker
│   (java -jar)        │  共用进程,共享 Redis/MySQL
└──────────┬───────────┘
           │
┌──────────▼───────────┐
│     nginx            │  反代 HTTP + WS
│  /api/*  → 8080      │
│  /mqtt  → 8080/9001  │  WebSocket upgrade
└──────────────────────┘
```

**独立部署模式**(Phase 1.1 后可选):

```
┌──────────────────────┐  ┌──────────────────────┐
│   mica-admin-server  │  │   mica-mqtt-broker   │
│   (java -jar)        │  │   (独立进程)          │
└──────────┬───────────┘  └──────────┬───────────┘
           │                         │
┌──────────▼─────────────────────────▼───────────┐
│     Shared: Redis / MySQL / x-file-storage     │
└────────────────────────────────────────────────┘
```

> 单进程模式简单,适合百人以下团队。
> 独立部署适合规模扩大或 broker 需要独立水平扩展。

---

## 2. mica-mqtt 接入方案

### 2.1 依赖引入

```xml
<!-- mica-admin-server/pom.xml -->
<dependency>
    <groupId>org.dromara.mica-mqtt</groupId>
    <artifactId>mica-mqtt-spring-boot-starter</artifactId>
    <version>2.4.6</version>
</dependency>
<dependency>
    <groupId>org.dromara.mica-mqtt</groupId>
    <artifactId>mica-mqtt-server</artifactId>
    <version>2.4.6</version>
</dependency>
```

### 2.2 配置

```yaml
# application.yml
mica:
  mqtt:
    enabled: true
    # 单进程模式:启动内嵌 broker
    server:
      enabled: true
      port: 1883                    # TCP
      websocket-port: 9001          # WebSocket(浏览器用)
      heartbeat-timeout: 60s
      # 鉴权拦截器(自定义)
      auth-interceptor: net.dreamlu.mica.admin.im.auth.MqttAuthInterceptor
      # 全局消息监听(自定义)
      message-listener: net.dreamlu.mica.admin.im.listener.MqttMessageListener
```

### 2.3 内嵌 Broker 启动

```java
@Configuration
@RequiredArgsConstructor
public class ImMqttConfig {

    private final JwtTokenStore tokenStore;
    private final MqttAuthInterceptor authInterceptor;
    private final MqttMessageListener messageListener;

    @Bean
    public MqttServer mqttServer() {
        MqttServer server = MqttServer.create()
            .port(1883)
            .webPort(9001)
            .authHandler(authInterceptor)        // CONNECT 鉴权
            .messageListener(messageListener)    // 全局消息监听
            .heartbeatTimeout(60_000)
            .build();

        server.start();
        return server;
    }
}
```

---

## 3. 鉴权设计

### 3.1 JWT-based MQTT 鉴权

**核心思路**:把 mica-admin 的 JWT 当作 MQTT 的 username。

```
MQTT CONNECT
  clientId = "user-{userId}-{deviceId}-{random}"
  username = JWT token
  password = 空
```

```java
@Component
@RequiredArgsConstructor
public class MqttAuthInterceptor implements MqttAuthHandler {

    private final JwtTokenStore tokenStore;

    @Override
    public boolean authenticate(MqttConnectMessage message, String clientId) {
        String token = message.getUsername();
        if (StringUtil.isBlank(token)) {
            return false;
        }
        try {
            JwtUser jwtUser = tokenStore.parse(token);
            if (jwtUser == null) {
                return false;
            }
            // 缓存:clientId -> userId (用于后续消息路由校验)
            MqttSessionManager.bind(clientId, jwtUser.getUserId());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 3.2 Topic 权限

mica-mqtt 支持 `MqttTopicFilter` 拦截器:

```java
@Component
@RequiredArgsConstructor
public class MqttTopicFilter implements IMqttTopicFilter {

    @Override
    public boolean isTopicMatch(String clientId, String topic, MqttQoS qos) {
        Long userId = MqttSessionManager.getUserId(clientId);
        if (userId == null) return false;

        // 用户只能订阅与自己相关的 topic
        return isOwnTopic(topic, userId);
    }

    private boolean isOwnTopic(String topic, Long userId) {
        // im/p2p/{myId}/to/{otherId}   - 接收
        // im/p2p/{otherId}/from/{myId} - 不允许(只能发给 to,不订阅)
        // im/p2p/{myId}/from/{otherId} - 接收
        // im/group/{groupId}/inbox     - 必须是群成员
        // im/sys/{myId}/system         - 接收自己的系统消息
        // im/status/{myId}/state       - 发布自己的状态(允许)
        // im/status/{otherId}/state    - 订阅他人的状态(允许)

        if (topic.startsWith("im/p2p/")) {
            return topic.contains("/" + userId + "/");
        }
        if (topic.startsWith("im/group/")) {
            String groupId = extractGroupId(topic);
            return isGroupMember(userId, groupId);
        }
        if (topic.startsWith("im/sys/")) {
            return topic.contains("/" + userId + "/");
        }
        if (topic.startsWith("im/status/")) {
            return true; // 状态对所有登录用户可见
        }
        return false;
    }
}
```

### 3.3 消息发送鉴权

客户端 PUBLISH 到 `im/p2p/{myId}/to/{otherId}` 时,服务端 listener 校验:

```java
@Component
@RequiredArgsConstructor
public class MqttMessageListener implements IMqttMessageListener {

    @Override
    public void onMessage(ChannelContext ctx, String clientId, MqttPublishMessage message) {
        String topic = message.getTopic();
        Long senderId = MqttSessionManager.getUserId(clientId);

        // 校验:topic 中的 from 必须 == senderId
        if (!validateSender(topic, senderId)) {
            log.warn("IM: 非法 topic 发送, clientId={}, topic={}", clientId, topic);
            return; // 丢弃
        }

        // 路由
        if (topic.startsWith("im/p2p/")) {
            handleP2P(topic, senderId, message);
        } else if (topic.startsWith("im/group/")) {
            handleGroup(topic, senderId, message);
        }
    }
}
```

---

## 4. Topic 设计

### 4.1 Topic 命名规范

```
im/
├── p2p/
│   ├── {senderId}/to/{receiverId}        单聊:发送方 → 接收方(PUBLISH)
│   └── {receiverId}/from/{senderId}      单聊:接收方订阅(自动)
├── group/
│   └── {groupId}/inbox                   群聊:群内所有成员订阅 + 发送
├── sys/
│   └── {userId}/system                   系统消息(进群/被踢/被@)
└── status/
    └── {userId}/state                    在线状态(PUBLISH retained)
```

### 4.2 订阅模板

```typescript
// 客户端订阅集合
const subscribeTopics = [
  // 自己的单聊接收 topic(动态,所有可能给我发消息的人)
  `im/p2p/${myUserId}/from/+`,
  // 自己加入的所有群
  ...myGroups.map(g => `im/group/${g.id}/inbox`),
  // 系统消息
  `im/sys/${myUserId}/system`,
  // 关注的同事状态
  ...followingUserIds.map(id => `im/status/${id}/state`)
]
```

### 4.3 Status Topic (retained)

```java
// 客户端上线
mqttClient.publish(`im/status/${userId}/state`, "online".getBytes(), 1, true);

// 客户端下线(LWT - Last Will and Testament)
mqttClient.publish(`im/status/${userId}/state`, "offline".getBytes(), 1, true);

// 服务端 MqttAuthHandler 配置 LWT
.connectOptions()
  .will("im/status/" + userId + "/state", "offline".getBytes(), 1, true)
```

---

## 5. 消息流

### 5.1 单聊发送流程

```
┌─────────────┐                          ┌─────────────┐                  ┌─────────────┐
│   Client A  │                          │   Broker    │                  │   Client B  │
│  (sender)   │                          │  (mica-mqtt)│                  │ (receiver)  │
└──────┬──────┘                          └──────┬──────┘                  └──────┬──────┘
       │                                        │                                 │
       │ CONNECT (JWT as username)              │                                 │
       │───────────────────────────────────────►│                                 │
       │  CONNACK                                │                                 │
       │◄───────────────────────────────────────│                                 │
       │                                        │  CONNECT (JWT as username)      │
       │                                        │◄────────────────────────────────│
       │                                        │  CONNACK                        │
       │                                        │────────────────────────────────►│
       │                                        │                                 │
       │ SUBSCRIBE im/p2p/{B}/from/{A}          │                                 │
       │───────────────────────────────────────►│                                 │
       │                                        │                                 │
       │ PUBLISH im/p2p/{A}/to/{B}              │                                 │
       │ payload={"text":"hi","client_msg_id":..}│                                 │
       │ QoS=1                                  │                                 │
       │───────────────────────────────────────►│                                 │
       │                                        │ MqttMessageListener            │
       │                                        │  - 解析 payload                 │
       │                                        │  - 校验 sender == A            │
       │                                        │  - 写 MySQL (im_message)       │
       │                                        │  - 更新会话                     │
       │                                        │  - 增 B 未读 (Redis)            │
       │                                        │                                 │
       │  PUBACK                                 │                                 │
       │◄───────────────────────────────────────│                                 │
       │                                        │ 转发到 SUBSCRIBE 的客户端       │
       │                                        │ PUBLISH im/p2p/{B}/from/{A}    │
       │                                        │ payload={"text":"hi",...}      │
       │                                        │────────────────────────────────►│
       │                                        │                                 │
       │                                        │                            处理消息
       │                                        │                            HTTP 拉会话列表
       │                                        │                            未读数更新
```

### 5.2 离线消息兜底

```java
// MqttMessageListener.handleP2P()
private void handleP2P(String topic, Long senderId, MqttPublishMessage msg) {
    Long receiverId = extractReceiverId(topic);
    ImMessagePO message = parseAndSave(msg, senderId, receiverId);

    // 检查接收方是否在线
    if (isUserOnline(receiverId)) {
        // 在线:依赖 MQTT 推送(已订阅 topic)
    } else {
        // 离线:写 sys_user_message 兜底
        sysUserMessageService.push(receiverId, "im_message", message.getId(), message.getTitle());
        // 上线时通过 /api/system/user/message/unread 拉取
    }
}
```

### 5.3 多端同步

```
Client A (Web)                 Broker                  Client A (App)
      │                           │                           │
      │ 登录                       │                           │
      │──────────────────────────►│                           │
      │                           │                           │
      │                           │                           │ 登录
      │                           │◄──────────────────────────│
      │                           │                           │
      │ B 发消息给 A              │                           │
      │◄──────────────────────────│                           │
      │                           │                           │
      │                           │── 同样消息转发 ───────────►│
      │                           │                           │
      │                           │                           │ 收到
      │                           │                           │ (两客户端同步)
```

> 因 `clientId` 含 `deviceId`,两个端有不同 session,但都订阅 `im/p2p/{myId}/from/{otherId}`,broker 会 fan-out 到两端。

---

## 6. 可靠性保证

### 6.1 消息不丢失

| 层级 | 措施 |
|---|---|
| MQTT QoS | QoS 1(至少一次) |
| Broker 持久化 | mica-mqtt 默认开启消息持久化 |
| 数据库写入 | listener 同步写 MySQL,失败重试 3 次 |
| 离线兜底 | 写 `sys_user_message`,上线时拉取 |
| 客户端去重 | `client_msg_id` 字段,服务端 + 客户端双重去重 |

### 6.2 消息顺序

- **同一会话内**:服务端按 `server_received_at` 排序,客户端按该字段展示
- **跨会话**:无顺序保证(无意义)

### 6.3 服务端消息存储

```sql
-- im_message 按月分区(可选)
ALTER TABLE im_message PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
  PARTITION p202606 VALUES LESS THAN (202607),
  PARTITION p202607 VALUES LESS THAN (202608),
  -- ...
);
```

> Phase 1 不做分区,Phase 1.1 后根据数据量评估。

### 6.4 客户端断线重连

```typescript
// App 端 mqtt-client 配置
const client = mqtt.connect('mqtt://server:1883', {
  clientId: `user-${userId}-${deviceId}-${random}`,
  username: jwtToken,
  reconnectPeriod: 5000,        // 5s 重连
  clean: false,                  // 保留 session,断线期间的消息不丢
  keepalive: 30
});

client.on('connect', () => {
  // 重连后重新订阅所有 topic
  subscribeAll()
})
```

---

## 7. 数据流关键路径

### 7.1 客户端订阅流程

```
1. 用户登录 mica-admin → 拿到 JWT
2. 建立 MQTT 连接
   - CONNECT (clientId, username=JWT)
   - 鉴权拦截器校验 JWT
   - 成功 → CONNACK
3. HTTP 拉取"我加入的群" /api/im/my/groups
4. 订阅 topic 集合:
   - im/p2p/{myId}/from/+         单聊接收
   - im/group/{groupId}/inbox      群消息
   - im/sys/{myId}/system          系统消息
5. PUBLISH im/status/{myId}/state = online (retained)
```

### 7.2 客户端发送消息流程

```
1. 用户在 UI 输入文本,点击发送
2. 客户端生成 client_msg_id = UUID
3. 立即在 UI 渲染(sending 状态)
4. PUBLISH im/p2p/{myId}/to/{otherId}
   payload={"client_msg_id":"...","type":"text","content":"...","server_msg_id":null,"created_at":null}
   QoS=1
5. 等 PUBACK:
   - 成功 → 状态改为 sent
   - 失败 → 状态改为 failed,允许重发
6. 后续通过 WebSocket/MQTT 收到 server_msg_id 后:
   - 更新本地消息的 server_msg_id
   - 用于去重和确认
```

### 7.3 服务端消息处理流程

```java
@Component
@RequiredArgsConstructor
public class MqttMessageListener implements IMqttMessageListener {

    private final ImMessageService messageService;
    private final ImConversationService conversationService;
    private final SysUserMessageService sysUserMessageService;
    private final RedisTemplate<String, Object> redis;

    @Override
    @Transactional
    public void onMessage(ChannelContext ctx, String clientId, MqttPublishMessage msg) {
        String topic = msg.getTopic();
        Long senderId = MqttSessionManager.getUserId(clientId);
        String payload = msg.getPayload().toString(StandardCharsets.UTF_8);

        // 1. 解析
        ImMessageDTO dto = JsonUtil.parse(payload, ImMessageDTO.class);

        // 2. 路由 + 处理
        if (topic.startsWith("im/p2p/")) {
            handleP2P(topic, senderId, dto);
        } else if (topic.startsWith("im/group/")) {
            handleGroup(topic, senderId, dto);
        }
    }

    private void handleP2P(String topic, Long senderId, ImMessageDTO dto) {
        Long receiverId = extractReceiverId(topic);

        // 1. 获取/创建会话
        ImConversation conversation = conversationService.getOrCreateP2P(senderId, receiverId);

        // 2. 持久化
        ImMessagePO entity = messageService.save(senderId, conversation.getId(), dto);

        // 3. 更新会话最后消息
        conversationService.updateLastMessage(conversation.getId(), entity);

        // 4. 增加接收方未读
        redis.incr("im:unread:" + receiverId);

        // 5. 离线兜底
        if (!isUserOnline(receiverId)) {
            sysUserMessageService.push(receiverId, "im_message", entity.getId(),
                dto.getContent().substring(0, Math.min(50, dto.getContent().length())));
        }
    }
}
```

---

## 8. 在线状态

### 8.1 在线判定

```
im/status/{userId}/state 值为 "online" 或 "offline"
- retained=true,broker 保存最新值
- 新订阅者立即收到当前状态
- 客户端断线 → LWT 触发 → broker 更新为 offline
```

### 8.2 客户端实现

```typescript
// 上线时
client.publish(
  `im/status/${userId}/state`,
  'online',
  { qos: 1, retain: true }
)

// 客户端 LWT 配置
mqttClient = mqtt.connect(url, {
  will: {
    topic: `im/status/${userId}/state`,
    payload: 'offline',
    qos: 1,
    retain: true
  }
})
```

### 8.3 服务端辅助

- **心跳超时**:心跳超过 60s 未收到,broker 主动断开,LWT 触发
- **踢人**:管理员通过 `DELETE /api/auth/token` 踢出后,服务端主动断开 MQTT 连接

---

## 9. 与 mica-admin 现有能力集成

### 9.1 sys_user / sys_dept 复用

```java
// IM 模块直接使用 SysUserService
@Service
@RequiredArgsConstructor
public class ImUserService {

    private final ISysUserService sysUserService;

    public UserBriefVO getBrief(Long userId) {
        SysUser user = sysUserService.getById(userId);
        return new UserBriefVO(user.getUserId(), user.getNickname(), user.getAvatar());
    }
}
```

### 9.2 部门群自动同步

```java
@Component
@RequiredArgsConstructor
public class DeptChangeListener {

    private final ImGroupService groupService;

    @EventListener
    public void onDeptCreated(SysDeptCreatedEvent event) {
        // 部门创建 → 自动建群
        Long groupId = groupService.createDeptGroup(event.getDeptId());
    }

    @EventListener
    public void onDeptUserChanged(SysDeptUserChangedEvent event) {
        // 部门成员变更 → 同步群成员
        groupService.syncDeptGroupMembers(event.getDeptId());
    }
}
```

### 9.3 sys_message 推送(Phase 0 重点)

```java
@Service
@RequiredArgsConstructor
public class SysMessageServiceImpl implements ISysMessageService {

    private final MqttTemplate mqttTemplate;  // mica-mqtt 提供的

    @Override
    @Transactional
    public void publish(Long messageId, List<Long> userIds, List<Long> deptIds) {
        // ... 现有逻辑 ...

        // 新增:Phase 0 推送通道
        SysMessage msg = this.getById(messageId);
        for (Long userId : targetUserIds) {
            mqttTemplate.send(
                "/im/sys/" + userId + "/system",
                JsonUtil.toJson(Map.of(
                    "id", msg.getId(),
                    "title", msg.getTitle(),
                    "content", msg.getContent(),
                    "category", msg.getCategory(),
                    "created_at", msg.getCreatedAt()
                ))
            );
        }
    }
}
```

---

## 10. 安全考虑

| 威胁 | 措施 |
|---|---|
| 伪造他人发消息 | CONNECT 时 JWT 校验 + topic 路径与 userId 一致性校验 |
| 窃听 | mTLS(可选,生产环境推荐) |
| 重放攻击 | client_msg_id 去重 + JWT 短期有效 |
| 离线消息爆炸 | 限制单用户离线消息数(默认 1000) |
| 垃圾消息 | 频率限制(单用户 60 msg/min) |
| 群成员越权 | 群操作(踢人/解散)权限校验 |

---

## 11. 可观测性

### 11.1 关键日志

```java
log.info("IM 连接: clientId={}, userId={}", clientId, userId);
log.info("IM 断开: clientId={}, userId={}, reason={}", clientId, userId, reason);
log.info("IM 订阅: clientId={}, topic={}", clientId, topic);
log.info("IM 消息: from={}, to={}, type={}, len={}", from, to, type, content.length());
log.warn("IM 非法 topic: clientId={}, topic={}", clientId, topic);
log.error("IM 消息持久化失败: msg={}", msg, e);
```

### 11.2 监控指标(可选,Phase 1.1+ 接入)

- `im_connection_count` — 当前连接数(Micrometer Gauge)
- `im_message_throughput` — 每秒消息数
- `im_message_latency` — 消息延迟(P50/P99)
- `im_message_persist_failure` — 持久化失败计数

### 11.3 管理端界面

在 mica-admin Web 后台加一个"IM 监控"页:

- 当前连接列表
- 最近消息流
- 离线消息队列
- 踢人(强制断开 MQTT 连接)

---

## 12. 性能与扩展

### 12.1 Phase 1.1 性能基线

| 指标 | 目标 |
|---|---|
| 并发在线 | 100 用户 |
| 消息吞吐 | 10 msg/s(总) |
| 消息延迟 | < 1s(局域网) |
| 单群规模 | 200 人 |
| MySQL 存储 | 100 万条消息 |

### 12.2 性能瓶颈分析

| 瓶颈 | 应对 |
|---|---|
| MySQL 写 | 短期:连接池调优;长期:分库分表 / 异步写 |
| Redis 未读 | 单 key incr,无瓶颈 |
| 群消息 fan-out | 200 人 × 1 msg/s = 200 msg/s,OK;超过需分层 |
| mica-mqtt broker | 单实例支持 10K 连接,小团队够用 |

### 12.3 后续扩展

- **WebSocket 集群**:Phase 2.0+ 评估
- **Kafka 解耦**:Phase 2.0+ 把消息写入异步化
- **多 broker**:Phase 3.0+ 用 mica-mqtt 集群模式

---

## 13. 与 App 端集成

参考 [docs/app/](../app/README.md) 中 `extension/im/` 模块:

```
src/modules/extension/im/
├── api/                  # HTTP REST 客户端
├── mqtt/                 # MQTT 客户端封装
│   ├── client.ts         # 连接/重连
│   ├── subscribe.ts      # 订阅管理
│   └── publish.ts        # 发送消息
├── store/                # Pinia store
│   ├── conversations.ts  # 会话列表
│   ├── messages.ts       # 消息缓存
│   └── unread.ts         # 未读数
├── views/
│   ├── conversation-list.vue
│   ├── chat-p2p.vue
│   ├── chat-group.vue
│   ├── group-list.vue
│   └── group-detail.vue
└── components/
    ├── message-bubble.vue
    ├── message-input.vue
    └── conversation-item.vue
```

**App 端 MQTT 库选择**:

| 平台 | 推荐库 |
|---|---|
| iOS | MQTT-Client-Framework / CocoaMQTT |
| Android | Paho Android / HiveMQ MQTT Client |
| 微信小程序 | **不支持**,降级 HTTP + 微信模板消息 |
| Web | mqtt.js (Eclipse Paho) |

---

## 14. 后续演进

| 阶段 | 演进 |
|---|---|
| Phase 2.0 | 文件/图片消息(走 x-file-storage) |
| Phase 2.1 | 已读回执、撤回、@、引用 |
| Phase 2.2 | 第三方推送(uniPush)解决小程序/弱网 |
| Phase 3.0 | 音视频(集成声网/即构 SDK) |
| Phase 3.1 | 多 broker 集群 |
| Phase 3.2 | 端到端加密(E2EE) |