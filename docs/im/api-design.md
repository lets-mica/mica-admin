# IM 模块接口设计

> 基于 [architecture.md](./architecture.md) 和 [data-model.md](./data-model.md),
> 定义 IM 模块对外的 HTTP REST API + MQTT Topic 协议。

## 全局约定

### 基础地址

```
开发环境:
  HTTP:   http://localhost:8080/api/im
  MQTT:   tcp://localhost:1883
  MQTT-WS: ws://localhost:9001/mqtt

生产环境:
  HTTP:   /api/im  (经 nginx 反代)
  MQTT:   mqtt://your-domain.com:1883
  MQTT-WS: wss://your-domain.com/mqtt  (经 nginx WebSocket upgrade)
```

### HTTP 通用约定

复用 mica-admin 现有规则(详见 [docs/app/api-mapping.md §全局约定](../app/api-mapping.md#全局约定)):

- 响应格式:`{ code: 0, msg: 'ok', data: T }`(成功 code = 0)
- 鉴权:`Authorization: Bearer {JWT}`
- 401 → 清 token → 跳登录页
- 通用分页入参:`current`, `size`

### MQTT 通用约定

- 鉴权:CONNECT 时 `username=JWT`
- QoS:消息默认 QoS 1(至少一次)
- Retained:仅 `im/status/{userId}/state` 用 retained
- ClientId 规范:`user-{userId}-{deviceType}-{deviceId}-{random}`
  - 示例:`user-1-mobile-a8b3c-7f9d`
  - 用于服务端识别 userId + 设备类型

### 错误码(IM 专属)

| Code | 含义 |
|---|---|
| 0 | 成功 |
| 5000 | IM 模块通用错误 |
| 5001 | 会话不存在 |
| 5002 | 消息发送失败 |
| 5003 | 不是会话成员 |
| 5004 | 不是群成员 |
| 5005 | 群已满 |
| 5006 | 仅群主可操作 |
| 5007 | 仅管理员可操作 |
| 5008 | MQTT 连接失败 |
| 5009 | 频率超限 |
| 5010 | 内容非法(空/超长) |

---

## Phase 0 API(推送通道)

### MQTT Topic

| Topic | Payload | 方向 |
|---|---|---|
| `im/status/{userId}/state` | `"online"` / `"offline"` | 客户端 → 服务端 |
| `im/sys/{userId}/system` | `ImSystemMessagePayload` | 服务端 → 客户端 |

### HTTP API(可选,主要用于调试)

```http
GET /api/im/status/{userId}
```

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "userId": 1,
    "online": true,
    "lastActiveAt": "2026-06-22T10:00:00"
  }
}
```

### Payload:ImSystemMessagePayload

```typescript
interface ImSystemMessagePayload {
  /** 系统消息来源类型 */
  source: 'sys_message' | 'im_message' | 'admin'
  /** 来源 ID(如 sys_message.id) */
  sourceId: number
  /** 标题 */
  title: string
  /** 内容 */
  content: string
  /** 分类(可选) */
  category?: string
  /** 跳转 URL(可选) */
  url?: string
  /** 服务端时间戳(毫秒) */
  serverTime: number
}
```

---

## Phase 1 API(单聊 MVP)

### 1.1 HTTP API 总览

| Method | Path | 说明 |
|---|---|---|
| GET | `/api/im/conversations` | 当前用户的会话列表 |
| GET | `/api/im/conversations/p2p/{userId}` | 获取/创建与某用户的单聊会话 |
| GET | `/api/im/conversations/{id}` | 会话详情 |
| DELETE | `/api/im/conversations/{id}` | 删除会话(仅本地视角) |
| PUT | `/api/im/conversations/{id}/read` | 标记会话已读 |
| GET | `/api/im/conversations/{id}/messages` | 拉取会话历史消息 |
| GET | `/api/im/unread/count` | 当前用户总未读数 |

### 1.2 GET /api/im/conversations

**Request**:

```http
GET /api/im/conversations?current=1&size=20
Authorization: Bearer {JWT}
```

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "records": [
      {
        "id": 100,
        "type": "p2p",
        "targetId": 2,
        "targetBrief": {
          "userId": 2,
          "nickname": "李四",
          "avatar": "https://...",
          "online": true
        },
        "lastMessage": {
          "id": 1234,
          "senderId": 2,
          "type": "text",
          "content": "在吗?",
          "createdAt": "2026-06-22T10:00:00"
        },
        "unreadCount": 3,
        "mute": false,
        "top": false,
        "lastMsgAt": "2026-06-22T10:00:00"
      },
      {
        "id": 101,
        "type": "group",
        "targetId": 50,
        "targetBrief": {
          "groupId": 50,
          "name": "研发一组",
          "avatar": "https://...",
          "memberCount": 12
        },
        "lastMessage": {
          "id": 1235,
          "senderId": 1,
          "senderName": "张三",
          "type": "text",
          "content": "@所有人 开会啦",
          "createdAt": "2026-06-22T09:50:00"
        },
        "unreadCount": 0,
        "mute": false,
        "top": false,
        "lastMsgAt": "2026-06-22T09:50:00"
      }
    ],
    "total": 15,
    "current": 1,
    "size": 20
  }
}
```

### 1.3 GET /api/im/conversations/p2p/{userId}

获取或创建与某用户的单聊会话。

**Request**:

```http
GET /api/im/conversations/p2p/2
Authorization: Bearer {JWT}
```

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "id": 100,
    "type": "p2p",
    "targetId": 2,
    "targetBrief": {
      "userId": 2,
      "nickname": "李四",
      "avatar": "https://...",
      "online": true
    },
    "lastMessage": null,
    "unreadCount": 0,
    "createdAt": "2026-06-22T09:00:00"
  }
}
```

### 1.4 GET /api/im/conversations/{id}/messages

**Request**:

```http
GET /api/im/conversations/100/messages?current=1&size=20
Authorization: Bearer {JWT}
```

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "records": [
      {
        "id": 1234,
        "conversationId": 100,
        "senderId": 2,
        "senderName": "李四",
        "senderAvatar": "https://...",
        "type": "text",
        "content": "在吗?",
        "clientMsgId": "uuid-abc",
        "replyToId": null,
        "recalled": false,
        "createdAt": "2026-06-22T10:00:00"
      }
    ],
    "total": 50,
    "current": 1,
    "size": 20
  }
}
```

**Query 参数**:

| 参数 | 类型 | 默认 | 说明 |
|---|---|---|---|
| current | int | 1 | 当前页 |
| size | int | 20 | 每页大小(最大 100) |
| beforeId | long | null | 拉取该 ID 之前的消息(用于上拉加载) |

### 1.5 PUT /api/im/conversations/{id}/read

标记会话已读。

**Request**:

```http
PUT /api/im/conversations/100/read
Authorization: Bearer {JWT}
```

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": null
}
```

### 1.6 GET /api/im/unread/count

获取当前用户总未读数。

**Request**:

```http
GET /api/im/unread/count
Authorization: Bearer {JWT}
```

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "total": 8,
    "breakdown": [
      { "conversationId": 100, "unreadCount": 3 },
      { "conversationId": 101, "unreadCount": 5 }
    ]
  }
}
```

### 1.7 DELETE /api/im/conversations/{id}

删除会话(仅本地视角,从该用户的会话列表移除)。

**Request**:

```http
DELETE /api/im/conversations/100
Authorization: Bearer {JWT}
```

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": null
}
```

> **注意**:删除会话不影响消息历史,对方仍可看到会话。

### 1.8 MQTT Topic:发送单聊消息

**Topic**: `im/p2p/{fromUserId}/to/{toUserId}`

**Payload**: `ImMessagePayload`

```json
{
  "client_msg_id": "550e8400-e29b-41d4-a716-446655440000",
  "type": "text",
  "content": "在吗?有个事请教",
  "created_at": 1719024000000
}
```

### 1.9 MQTT Topic:接收单聊消息

**Topic**: `im/p2p/{toUserId}/from/{fromUserId}`

**Payload**: `ImMessageVO`(服务端持久化后的完整消息)

```json
{
  "id": 1234,
  "conversationId": 100,
  "senderId": 1,
  "senderName": "张三",
  "senderAvatar": "https://...",
  "type": "text",
  "content": "在吗?有个事请教",
  "clientMsgId": "550e8400-e29b-41d4-a716-446655440000",
  "recalled": false,
  "createdAt": "2026-06-22T10:00:00"
}
```

### 1.10 MQTT Topic:订阅模板

```typescript
// 客户端订阅集合
const subscribeTopics = [
  // 接收单聊消息
  `im/p2p/${myUserId}/from/+`,
  // 系统消息
  `im/sys/${myUserId}/system`,
  // 关注的用户状态
  `im/status/${myUserId}/state`,        // 自己的状态
  `im/status/+/state`                    // 所有人的状态
]
```

---

## Phase 1.1 API(群聊)

### 2.1 群管理 HTTP API

| Method | Path | 说明 |
|---|---|---|
| POST | `/api/im/groups` | 创建群 |
| GET | `/api/im/groups/{id}` | 群详情 |
| PUT | `/api/im/groups/{id}` | 修改群信息 |
| DELETE | `/api/im/groups/{id}` | 解散群 |
| GET | `/api/im/groups/{id}/members` | 成员列表 |
| POST | `/api/im/groups/{id}/members` | 邀请成员 |
| DELETE | `/api/im/groups/{id}/members/{userId}` | 踢出成员 |
| PUT | `/api/im/groups/{id}/members/{userId}` | 设置成员角色 |
| POST | `/api/im/groups/{id}/quit` | 主动退群 |
| GET | `/api/im/my/groups` | 我加入的群列表 |

### 2.2 POST /api/im/groups

创建群。

**Request**:

```http
POST /api/im/groups
Authorization: Bearer {JWT}
Content-Type: application/json

{
  "name": "项目讨论组",
  "avatar": "https://...",     // 可选
  "memberIds": [2, 3, 4, 5]    // 初始成员(创建者自动加入)
}
```

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "id": 200,
    "name": "项目讨论组",
    "avatar": "https://...",
    "type": "normal",
    "ownerId": 1,
    "ownerName": "张三",
    "memberCount": 5,
    "maxMembers": 200,
    "announcement": null,
    "createdAt": "2026-06-22T10:00:00"
  }
}
```

### 2.3 GET /api/im/groups/{id}

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "id": 200,
    "name": "项目讨论组",
    "avatar": "https://...",
    "type": "normal",
    "ownerId": 1,
    "ownerName": "张三",
    "deptId": null,
    "memberCount": 5,
    "maxMembers": 200,
    "announcement": "欢迎大家!",
    "createdAt": "2026-06-22T10:00:00",
    "myRole": "owner"
  }
}
```

### 2.4 GET /api/im/groups/{id}/members

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "records": [
      {
        "userId": 1,
        "nickname": "张三",
        "avatar": "https://...",
        "role": "owner",
        "online": true,
        "joinedAt": "2026-06-22T10:00:00"
      },
      {
        "userId": 2,
        "nickname": "李四",
        "avatar": "https://...",
        "role": "admin",
        "online": false,
        "joinedAt": "2026-06-22T10:01:00"
      }
    ],
    "total": 5
  }
}
```

### 2.5 POST /api/im/groups/{id}/members

邀请成员(批量)。

**Request**:

```json
{
  "userIds": [6, 7, 8]
}
```

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "added": [6, 7, 8],
    "skipped": []
  }
}
```

> 邀请后,系统会给新成员发 `im/sys/{userId}/system` 消息。

### 2.6 DELETE /api/im/groups/{id}/members/{userId}

踢出成员。

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": null
}
```

### 2.7 PUT /api/im/groups/{id}

修改群信息。

**Request**:

```json
{
  "name": "项目讨论组(新)",
  "announcement": "新公告",
  "avatar": "https://..."
}
```

### 2.8 DELETE /api/im/groups/{id}

解散群(仅群主)。

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": null
}
```

> 解散后,所有成员收到 `im/sys/{userId}/system` 通知。

### 2.9 POST /api/im/groups/{id}/quit

主动退群。

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": null
}
```

> 群主退群前需先转让群主(Phase 2.0+)。

### 2.10 GET /api/im/my/groups

我加入的群列表。

**Response**:

```json
{
  "code": 0,
  "msg": "ok",
  "data": [
    {
      "id": 200,
      "name": "项目讨论组",
      "avatar": "https://...",
      "type": "normal",
      "ownerId": 1,
      "memberCount": 5,
      "myRole": "owner",
      "joinedAt": "2026-06-22T10:00:00"
    }
  ]
}
```

### 2.11 MQTT Topic:群消息

**Topic**: `im/group/{groupId}/inbox`

**发送 Payload**(客户端 → 服务端):

```json
{
  "sender_id": 1,
  "sender_name": "张三",
  "client_msg_id": "uuid-abc",
  "type": "text",
  "content": "@所有人 开会啦",
  "created_at": 1719024000000
}
```

**接收 Payload**(服务端 → 客户端,广播):

```json
{
  "id": 1235,
  "conversationId": 200,
  "groupId": 200,
  "senderId": 1,
  "senderName": "张三",
  "senderAvatar": "https://...",
  "type": "text",
  "content": "@所有人 开会啦",
  "clientMsgId": "uuid-abc",
  "recalled": false,
  "createdAt": "2026-06-22T10:00:00"
}
```

### 2.12 群成员订阅管理

| 事件 | 客户端操作 |
|---|---|
| 加入群 | `SUBSCRIBE im/group/{groupId}/inbox` |
| 退群/被踢 | `UNSUBSCRIBE im/group/{groupId}/inbox` |
| 创建群(自己) | 同上 |
| 被邀请入群 | 收到 `im/sys/{myId}/system` 后,订阅对应 topic |

> **客户端维护订阅集合**:Pinia store 存 `subscribedGroups: number[]`,启动 / 加退群时同步。

---

## 实体定义汇总

### Java(后端)

```java
// ============== 请求 VO ==============

@Data
public class CreateGroupReq {
    @NotBlank
    private String name;
    private String avatar;
    @NotEmpty
    private List<Long> memberIds;
}

@Data
public class UpdateGroupReq {
    private String name;
    private String avatar;
    private String announcement;
}

@Data
public class InviteMemberReq {
    @NotEmpty
    private List<Long> userIds;
}

@Data
public class SetMemberRoleReq {
    /** owner / admin / member */
    @NotBlank
    private String role;
}

// ============== 响应 VO ==============

@Data
public class ImConversationVO {
    private Long id;
    private String type;
    private Long targetId;
    private Object targetBrief;
    private ImMessageBriefVO lastMessage;
    private Integer unreadCount;
    private Boolean mute;
    private Boolean top;
    private LocalDateTime lastMsgAt;
}

@Data
public class ImMessageVO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String type;
    private String content;
    private String clientMsgId;
    private Long replyToId;
    private Boolean recalled;
    private LocalDateTime createdAt;
}

@Data
public class ImGroupVO {
    private Long id;
    private String name;
    private String avatar;
    private String type;
    private Long ownerId;
    private String ownerName;
    private Long deptId;
    private Integer memberCount;
    private Integer maxMembers;
    private String announcement;
    private LocalDateTime createdAt;
    private String myRole;
}

@Data
public class ImGroupMemberVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private String role;
    private Boolean online;
    private LocalDateTime joinedAt;
}

@Data
public class ImUnreadCountVO {
    private Integer total;
    private List<ConversationUnreadVO> breakdown;
}

@Data
public class ConversationUnreadVO {
    private Long conversationId;
    private Integer unreadCount;
}

// ============== DTO(internal) ==============

@Data
public class ImMessageDTO {
    private String clientMsgId;
    private String type;
    private String content;
    private Long replyToId;
    private Long createdAt;  // 毫秒时间戳
}
```

### TypeScript(前端)

```typescript
// ============== API 响应类型 ==============

interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

// ============== IM 类型 ==============

interface ImConversation {
  id: number
  type: 'p2p' | 'group'
  targetId: number
  targetBrief: P2pBrief | GroupBrief
  lastMessage: ImMessage | null
  unreadCount: number
  mute: boolean
  top: boolean
  lastMsgAt: string
}

interface P2pBrief {
  userId: number
  nickname: string
  avatar?: string
  online: boolean
}

interface GroupBrief {
  groupId: number
  name: string
  avatar?: string
  memberCount: number
}

interface ImMessage {
  id: number
  conversationId: number
  senderId: number
  senderName?: string
  senderAvatar?: string
  type: 'text' | 'image' | 'file' | 'system'
  content: string
  clientMsgId: string
  replyToId?: number
  recalled: boolean
  createdAt: string
}

interface ImGroup {
  id: number
  name: string
  avatar?: string
  type: 'normal' | 'department'
  ownerId: number
  ownerName?: string
  deptId?: number
  memberCount: number
  maxMembers: number
  announcement?: string
  createdAt: string
  myRole: 'owner' | 'admin' | 'member'
}

interface ImGroupMember {
  userId: number
  nickname: string
  avatar?: string
  role: 'owner' | 'admin' | 'member'
  online: boolean
  joinedAt: string
}

interface ImUnreadCount {
  total: number
  breakdown: Array<{ conversationId: number; unreadCount: number }>
}

// ============== MQTT Payload ==============

interface MqttImMessagePayload {
  client_msg_id: string
  type: 'text' | 'image' | 'file'
  content: string
  created_at: number
  reply_to_id?: number
}

interface MqttGroupMessagePayload extends MqttImMessagePayload {
  sender_id: number
  sender_name: string
  sender_avatar?: string
}

interface MqttSystemMessagePayload {
  source: 'sys_message' | 'im_message' | 'admin'
  sourceId: number
  title: string
  content: string
  category?: string
  url?: string
  serverTime: number
}
```

---

## 错误处理

### HTTP 错误响应

```json
{
  "code": 5003,
  "msg": "不是会话成员",
  "data": null
}
```

### 客户端处理

```typescript
// utils/request.ts
import { useAuthStore } from '@/stores/auth'

axios.interceptors.response.use(
  (res) => {
    const { code, msg, data } = res.data
    if (code === 0) return data
    if (code === 401) {
      useAuthStore().logout()
      uni.reLaunch({ url: '/pages/login/index' })
      return Promise.reject(new Error('未登录'))
    }
    if (code >= 5000 && code < 6000) {
      // IM 错误
      uni.showToast({ title: msg, icon: 'none' })
    }
    return Promise.reject(new Error(msg))
  }
)
```

### MQTT 错误处理

```typescript
mqttClient.on('error', (err) => {
  console.error('MQTT 错误:', err)
  // 重连由 reconnectPeriod 自动处理
})

mqttClient.on('close', () => {
  console.log('MQTT 连接关闭')
  // 重连由 reconnectPeriod 自动处理
})

// 发送失败(本地判)
if (!await sendPromise) {
  // 状态改为 failed,允许重发
  message.status = 'failed'
}
```

---

## 鉴权细节补充

### HTTP 鉴权

所有 `/api/im/**` 接口要求 `@PreAuthorize("@sec.isAuthenticated()")`,复用 mica-admin 现有 JWT 鉴权。

### MQTT 鉴权

详见 [architecture.md §3](./architecture.md#3-鉴权设计),关键点:

- CONNECT 时 `username = JWT`
- 服务端 `MqttAuthInterceptor` 校验
- 校验通过后绑定 `clientId → userId`
- SUBSCRIBE 时 `MqttTopicFilter` 校验 topic 权限
- PUBLISH 时 listener 校验 topic 中的 `fromId == sessionUserId`

### 跨设备登录

- 同一账号在多设备登录 → 多个 `clientId`(userId 相同但 device 不同)
- 消息 fan-out 到所有设备
- 其中一个设备踢出(主动登出)→ 仅踢该 clientId

---

## 性能基准

| 接口 | P99 延迟 | 备注 |
|---|---|---|
| `GET /api/im/conversations` | < 200ms | 20 条会话,MySQL JOIN |
| `GET /api/im/conversations/{id}/messages` | < 300ms | 20 条消息 |
| `PUT /api/im/conversations/{id}/read` | < 100ms | 1 次 SQL UPDATE |
| MQTT 消息端到端 | < 1s | 局域网 |
| 离线消息兜底 | < 2s | 写 sys_user_message |

---

## 后续 API(Phase 2.0+,预留)

- `POST /api/im/messages/{id}/recall` — 消息撤回
- `POST /api/im/upload` — 文件上传(IM 消息专用)
- `POST /api/im/conversations/{id}/forward` — 转发消息
- `GET /api/im/messages/search?keyword=` — 消息搜索
- `POST /api/im/groups/{id}/transfer-owner` — 转让群主