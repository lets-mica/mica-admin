# IM 模块数据模型

> 基于 [architecture.md](./architecture.md) 的分层架构,
> 定义 IM 模块的所有数据结构,包括 MySQL 表、Redis Key、MQTT Topic 详细规范。

## 1. MySQL 表设计

### 1.1 `im_conversation` — 会话

```sql
CREATE TABLE `im_conversation` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `type`            VARCHAR(16)  NOT NULL                COMMENT '会话类型:p2p/group',
    `target_id`       BIGINT       NOT NULL                COMMENT '关联目标:p2p=对方用户ID,group=群ID',
    `last_msg_id`     BIGINT       DEFAULT NULL            COMMENT '最后一条消息ID',
    `last_msg_at`     DATETIME     DEFAULT NULL            COMMENT '最后消息时间(用于排序)',
    `created_at`      DATETIME     NOT NULL                COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL                COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_target` (`type`, `target_id`),
    KEY `idx_last_msg_at` (`last_msg_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 会话';
```

> **设计说明**:会话本身不带 owner 概念,owner 通过 `im_conversation_member` 表达(单聊场景下有 2 个成员)。

#### Entity

```java
@Data
@TableName("im_conversation")
@EqualsAndHashCode(callSuper = true)
public class ImConversation extends BaseModel {
    /** 会话类型 */
    private String type;
    /** 关联目标:p2p=对方用户ID,group=群ID */
    private Long targetId;
    /** 最后一条消息ID */
    private Long lastMsgId;
    /** 最后消息时间 */
    private LocalDateTime lastMsgAt;
}
```

> 继承 `BaseModel` 复用 mica-admin 的审计字段(`id/created_by/created_at/updated_by/updated_at`)。

---

### 1.2 `im_conversation_member` — 会话成员

```sql
CREATE TABLE `im_conversation_member` (
    `conversation_id`  BIGINT      NOT NULL COMMENT '会话ID',
    `user_id`          BIGINT      NOT NULL COMMENT '用户ID',
    `role`             VARCHAR(16) DEFAULT 'member' COMMENT '成员角色:p2p无,group:owner/admin/member',
    `unread_count`     INT         DEFAULT 0      COMMENT '未读消息数',
    `last_read_msg_id` BIGINT      DEFAULT NULL   COMMENT '最后已读消息ID',
    `mute`             TINYINT(1)  DEFAULT 0      COMMENT '是否免打扰',
    `top`              TINYINT(1)  DEFAULT 0      COMMENT '是否置顶',
    `joined_at`        DATETIME    NOT NULL       COMMENT '加入时间',
    PRIMARY KEY (`conversation_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 会话成员';
```

#### Entity

```java
@Data
@TableName("im_conversation_member")
public class ImConversationMember {
    private Long conversationId;
    private Long userId;
    /** p2p: null;group: owner/admin/member */
    private String role;
    private Integer unreadCount;
    private Long lastReadMsgId;
    private Boolean mute;
    private Boolean top;
    private LocalDateTime joinedAt;
}
```

---

### 1.3 `im_message` — 消息

```sql
CREATE TABLE `im_message` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `conversation_id`  BIGINT        NOT NULL                COMMENT '会话ID',
    `sender_id`        BIGINT        NOT NULL                COMMENT '发送者用户ID',
    `type`             VARCHAR(16)   NOT NULL                COMMENT '消息类型:text/image/file/system',
    `content`          TEXT          NOT NULL                COMMENT '消息内容(text:文本;image:URL;file:URL+name)',
    `client_msg_id`    VARCHAR(64)   DEFAULT NULL            COMMENT '客户端消息ID(去重用)',
    `reply_to_id`      BIGINT        DEFAULT NULL            COMMENT '引用消息ID(Phase 2.0+)',
    `recalled`         TINYINT(1)    DEFAULT 0               COMMENT '是否撤回',
    `created_at`       DATETIME(3)   NOT NULL                COMMENT '发送时间(毫秒精度)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sender_client_msg` (`sender_id`, `client_msg_id`) COMMENT '发送者+客户端ID 唯一',
    KEY `idx_conv_created` (`conversation_id`, `created_at` DESC),
    KEY `idx_sender_created` (`sender_id`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 消息';
```

> **关键设计**:
> - `client_msg_id` 唯一索引 → **服务端去重**(同 client 重发只入库一次)
> - `(conversation_id, created_at)` 复合索引 → **会话历史分页查询**
> - 毫秒精度 → **同一会话内消息严格排序**

#### Entity

```java
@Data
@TableName("im_message")
@EqualsAndHashCode(callSuper = true)
public class ImMessage extends BaseModel {
    private Long conversationId;
    private Long senderId;
    /** text/image/file/system */
    private String type;
    private String content;
    private String clientMsgId;
    private Long replyToId;
    private Boolean recalled;
    private LocalDateTime createdAt;
}
```

---

### 1.4 `im_group` — 群

```sql
CREATE TABLE `im_group` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`         VARCHAR(100) NOT NULL                COMMENT '群名称',
    `avatar`       VARCHAR(255) DEFAULT NULL            COMMENT '群头像URL',
    `type`         VARCHAR(16)  NOT NULL                COMMENT '群类型:normal/department(部门群)',
    `owner_id`     BIGINT       NOT NULL                COMMENT '群主用户ID',
    `dept_id`      BIGINT       DEFAULT NULL            COMMENT '关联部门ID(部门群专用)',
    `announcement` TEXT         DEFAULT NULL            COMMENT '群公告',
    `member_count` INT          DEFAULT 0               COMMENT '成员数(冗余字段,加速列表查询)',
    `max_members`  INT          DEFAULT 200             COMMENT '最大成员数',
    `created_at`   DATETIME     NOT NULL,
    `updated_at`   DATETIME     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_dept_id` (`dept_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 群';
```

#### Entity

```java
@Data
@TableName("im_group")
@EqualsAndHashCode(callSuper = true)
public class ImGroup extends BaseModel {
    private String name;
    private String avatar;
    /** normal/department */
    private String type;
    private Long ownerId;
    private Long deptId;
    private String announcement;
    private Integer memberCount;
    private Integer maxMembers;
}
```

---

### 1.5 `im_group_member` — 群成员(冗余存储)

```sql
CREATE TABLE `im_group_member` (
    `group_id`    BIGINT      NOT NULL COMMENT '群ID',
    `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
    `role`        VARCHAR(16) DEFAULT 'member' COMMENT '角色:owner/admin/member',
    `nickname`    VARCHAR(64) DEFAULT NULL     COMMENT '群内昵称(可选)',
    `joined_at`   DATETIME    NOT NULL,
    PRIMARY KEY (`group_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 群成员';
```

> **为什么单独建表**?`im_conversation_member.role` 也存了 role,但语义不同:
> - `im_conversation_member.role`:用于消息显示(`sender_role` 渲染)
> - `im_group_member`:用于群管理操作(踢人/解散/角色设置)
>
> 群场景下两份数据保持一致(写入时同步)。

#### Entity

```java
@Data
@TableName("im_group_member")
public class ImGroupMember {
    private Long groupId;
    private Long userId;
    private String role;
    private String nickname;
    private LocalDateTime joinedAt;
}
```

---

### 1.6 `im_user_conversation` — 用户×会话索引(冗余,加速)

```sql
CREATE TABLE `im_user_conversation` (
    `user_id`          BIGINT   NOT NULL,
    `conversation_id`  BIGINT   NOT NULL,
    `sort_time`        DATETIME NOT NULL COMMENT '排序时间(冗余会话最后消息时间,避免JOIN)',
    `top`              TINYINT(1) DEFAULT 0,
    `mute`             TINYINT(1) DEFAULT 0,
    PRIMARY KEY (`user_id`, `conversation_id`),
    KEY `idx_user_sort` (`user_id`, `sort_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会话索引(加速列表查询)';
```

> 这个表是性能优化用的。也可不建,直接通过 `im_conversation_member` 查。
> 数据量大时建议建。

---

### 1.7 数据库初始化 SQL

完整 SQL 见 `docs/database/im-schema.sql`(Phase 0 实施时创建),包含:

- 上述 5 张表(选 6 张)
- 索引
- 注释

---

## 2. Redis Key 设计

### 2.1 在线状态

```
Key:    im:online:{userId}
Type:   String
Value:  "online" 或 "offline"
TTL:    无(显式设置)
```

> **与 MQTT retained 双保险**:
> - MQTT `im/status/{userId}/state` retained → 主要数据源
> - Redis 镜像 → 用于快速 HTTP 查询

```java
// 上线时
redis.opsForValue().set("im:online:" + userId, "online");
// 离线时
redis.delete("im:online:" + userId);

// 查询是否在线
Boolean online = redis.hasKey("im:online:" + userId);
```

### 2.2 未读消息计数

```
Key:    im:unread:{userId}
Type:   Integer (使用 INCR)
Value:  未读消息总数
TTL:    无
```

```java
// 收到消息时
redis.opsForValue().increment("im:unread:" + receiverId);

// 标记已读时
redis.opsForValue().set("im:unread:" + userId, 0);

// 查询
Integer count = (Integer) redis.opsForValue().get("im:unread:" + userId);
```

> **会话级未读**:存在 MySQL `im_conversation_member.unread_count`,HTTP 拉取时返回。
> **总未读**:Redis 缓存,仅用于 App 底部 Tab 徽标。

### 2.3 MQTT Session 映射

```
Key:    im:mqtt:client:{clientId}
Type:   String
Value:  userId
TTL:    heartbeat * 3(自动过期清理)
```

```java
// 鉴权成功时
redis.opsForValue().set("im:mqtt:client:" + clientId, String.valueOf(userId),
    Duration.ofMinutes(3));

// 发送消息时查 userId
String userIdStr = redis.opsForValue().get("im:mqtt:client:" + clientId);
```

### 2.4 在线客户端列表(用于多端)

```
Key:    im:user:clients:{userId}
Type:   Set
Value:  [clientId1, clientId2, ...]
TTL:    无
```

```java
// 用户连接时
redis.opsForSet().add("im:user:clients:" + userId, clientId);

// 断开时
redis.opsForSet().remove("im:user:clients:" + userId, clientId);

// 判断用户是否在线(任何一端在线)
Boolean online = redis.opsForSet().size("im:user:clients:" + userId) > 0;
```

### 2.5 离线消息队列(可选)

```
Key:    im:offline:{userId}
Type:   List
Value:  [messageId1, messageId2, ...]
TTL:    7 天
```

> **Phase 1 可选**:Phase 0/1 直接写 `sys_user_message` 兜底,此 Key 仅在离线消息量大时启用。

---

## 3. MQTT Topic 详细设计

### 3.1 Topic 命名总览

| Topic | 方向 | 谁订阅 | 谁发布 |
|---|---|---|---|
| `im/p2p/{fromId}/to/{toId}` | 单聊发送 | (Broker 内部路由) | 发送方 |
| `im/p2p/{toId}/from/{fromId}` | 单聊接收 | 接收方 | (Broker 转发) |
| `im/group/{groupId}/inbox` | 群消息 | 群成员 | 群成员 |
| `im/sys/{userId}/system` | 系统消息 | 目标用户 | 服务端 |
| `im/status/{userId}/state` | 在线状态 | 任何登录用户 | 本人 |

### 3.2 Topic 模板(便于订阅)

| 客户端订阅 | 通配符 | 说明 |
|---|---|---|
| `im/p2p/{myId}/from/+` | `+` | 接收所有给我发消息的人 |
| `im/p2p/+/to/{myId}` | `+` | 接收所有发给我的(更宽松,但需校验) |
| `im/group/{groupId}/inbox` | (明确) | 群消息 |
| `im/sys/{myId}/system` | (明确) | 系统消息 |
| `im/status/+/state` | `+` | 所有用户的状态(用于通讯录显示) |

### 3.3 Topic 示例

```bash
# 单聊:张三(1) → 李四(2)
PUBLISH im/p2p/1/to/2
payload: {"client_msg_id":"abc-123","type":"text","content":"在吗?","created_at":1719024000000}

# 接收方订阅
SUBSCRIBE im/p2p/2/from/1

# 群聊:群 ID = 100,张三发消息
PUBLISH im/group/100/inbox
payload: {"sender_id":1,"sender_name":"张三","client_msg_id":"def-456","type":"text","content":"开会啦","created_at":1719024000000}

# 群成员订阅
SUBSCRIBE im/group/100/inbox

# 系统消息:通知张三被加入群
PUBLISH im/sys/1/system
payload: {"type":"group_invite","group_id":100,"group_name":"研发一组","inviter_id":2}

# 在线状态:李四上线
PUBLISH im/status/2/state = "online" (retained)

# 状态订阅
SUBSCRIBE im/status/2/state
```

---

## 4. Payload 格式规范

### 4.1 单聊消息

```typescript
interface ImMessagePayload {
  /** 客户端消息ID(UUID),用于去重 */
  client_msg_id: string
  /** 消息类型(Phase 1 仅 text) */
  type: 'text' | 'image' | 'file' | 'system'
  /** 消息内容 */
  content: string
  /** 客户端发送时间(毫秒时间戳) */
  created_at: number
  /** Phase 2.0+: 引用消息ID */
  reply_to_id?: number
}
```

**示例**:

```json
{
  "client_msg_id": "550e8400-e29b-41d4-a716-446655440000",
  "type": "text",
  "content": "在吗?有个事请教",
  "created_at": 1719024000000
}
```

### 4.2 群聊消息(在单聊基础上加 sender)

```typescript
interface ImGroupMessagePayload extends ImMessagePayload {
  sender_id: number
  sender_name: string
  sender_avatar?: string
}
```

**示例**:

```json
{
  "sender_id": 1,
  "sender_name": "张三",
  "sender_avatar": "https://...",
  "client_msg_id": "550e8400-e29b-41d4-a716-446655440001",
  "type": "text",
  "content": "@所有人 下午 3 点开会",
  "created_at": 1719024000000
}
```

### 4.3 服务端回执(client 用以更新状态)

当服务端写入数据库后,通过同一 topic 回发,带 `server_msg_id`:

```json
{
  "server_msg_id": 12345,
  "client_msg_id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "delivered",
  "conversation_id": 100
}
```

### 4.4 系统消息

```typescript
interface ImSystemMessagePayload {
  type: 'group_invite' | 'group_kick' | 'group_dismiss' | 'group_member_join' | 'group_member_leave'
  group_id?: number
  group_name?: string
  inviter_id?: number
  inviter_name?: string
  operator_id?: number
  operator_name?: string
  user_id?: number
  user_name?: string
}
```

---

## 5. ER 图

```
                            ┌──────────────────────────┐
                            │      sys_user            │
                            │  (mica-admin 现有)        │
                            └─────────┬────────────────┘
                                      │ user_id
                                      │
              ┌───────────────────────┼─────────────────────────┐
              │                       │                         │
              ▼                       ▼                         ▼
┌──────────────────────┐  ┌──────────────────────────┐  ┌──────────────────┐
│ im_conversation      │  │ im_conversation_member   │  │ im_group_member  │
│ - id                 │◄─┤ - conversation_id        │  │ - group_id       │
│ - type (p2p/group)   │  │ - user_id                │  │ - user_id        │
│ - target_id          │  │ - role                   │  │ - role           │
│ - last_msg_id        │  │ - unread_count           │  │ - nickname       │
│ - last_msg_at        │  │ - last_read_msg_id       │  │ - joined_at      │
└──────────┬───────────┘  │ - mute / top             │  └────────┬─────────┘
           │              │ - joined_at              │           │
           │              └──────────────────────────┘           │
           │                                                     │
           │ 1:N                                                 │
           ▼                                                     │
┌──────────────────────┐                                         │
│ im_message           │                                         │
│ - id                 │                                         │
│ - conversation_id    │                                         │
│ - sender_id          │                                         │
│ - type               │                                         │
│ - content            │                                         │
│ - client_msg_id      │                                         │
│ - created_at         │                                         │
└──────────────────────┘                                         │
                                                                  │
            ┌─────────────────────────────────────────────────────┘
            │
            ▼
    ┌──────────────────────┐
    │ im_group             │
    │ - id                 │
    │ - name               │
    │ - type (normal/dept) │
    │ - owner_id           │──── sys_user.user_id
    │ - dept_id            │──── sys_dept.dept_id
    │ - member_count       │
    └──────────────────────┘
            │
            │ 1:N
            ▼
    ┌──────────────────────┐
    │ im_group_member      │  (冗余,便于管理操作)
    │ - group_id           │
    │ - user_id            │
    │ - role               │
    └──────────────────────┘
```

---

## 6. 关键业务规则

### 6.1 会话创建

| 场景 | 规则 |
|---|---|
| 单聊 | 首次发消息时,如 `im_conversation(type=p2p, target=对方)` 不存在 → 创建;同时建 2 条 `im_conversation_member`(自己 + 对方) |
| 群聊 | 创建群时建 1 条 `im_group`,1 条 `im_conversation(type=group)`,N 条 `im_conversation_member`(群成员) |
| 部门群 | mica-admin 创建 `sys_dept` 时同步触发 |

### 6.2 消息存储顺序

```java
@Transactional
public ImMessagePO save(Long senderId, Long conversationId, ImMessageDTO dto) {
    // 1. 唯一性校验(去重)
    ImMessagePO exist = messageMapper.selectByClientMsg(senderId, dto.getClientMsgId());
    if (exist != null) {
        return exist;  // 已存在,直接返回
    }

    // 2. 插入
    ImMessagePO entity = new ImMessagePO();
    BeanUtils.copyProperties(dto, entity);
    entity.setSenderId(senderId);
    entity.setConversationId(conversationId);
    entity.setCreatedAt(LocalDateTime.now());
    messageMapper.insert(entity);

    // 3. 更新会话最后消息
    conversationMapper.updateLastMessage(conversationId, entity.getId(), entity.getCreatedAt());

    return entity;
}
```

### 6.3 未读数计算

| 操作 | 变化 |
|---|---|
| 接收消息(对方在线) | `im:unread:{userId}` INCR 1,`im_conversation_member.unread_count` +1 |
| 接收消息(对方离线) | 写 `sys_user_message` + INCR 1 |
| 进入会话 | `im_conversation_member.unread_count` = 0,`last_read_msg_id` = 最后消息ID,`im:unread:{userId}` -= N(防并发:用 Lua 脚本或乐观锁) |
| 退出会话(App 切后台) | 不清未读 |

### 6.4 部门群同步

```java
@EventListener
public void onDeptCreated(SysDeptCreatedEvent event) {
    ImGroup group = new ImGroup();
    group.setName(event.getDept().getName());
    group.setType("department");
    group.setOwnerId(event.getDept().getLeaderId());
    group.setDeptId(event.getDept().getId());
    groupMapper.insert(group);

    // 加群主入群
    addMember(group.getId(), event.getDept().getLeaderId(), "owner");

    // 加部门所有成员入群
    List<Long> userIds = sysUserService.listUserIdsByDept(event.getDept().getId());
    userIds.forEach(uid -> addMember(group.getId(), uid, "member"));
}

@EventListener
public void onDeptUserAdded(SysDeptUserAddedEvent event) {
    Optional<ImGroup> group = groupMapper.findByDeptId(event.getDeptId());
    group.ifPresent(g -> addMember(g.getId(), event.getUserId(), "member"));
}

@EventListener
public void onDeptUserRemoved(SysDeptUserRemovedEvent event) {
    Optional<ImGroup> group = groupMapper.findByDeptId(event.getDeptId());
    group.ifPresent(g -> removeMember(g.getId(), event.getUserId()));
}
```

> mica-admin 当前可能未发布这些事件。**实施时需在 SysDeptServiceImpl 增补事件发布**。

### 6.5 群成员退出 / 被踢

```java
public void removeMember(Long groupId, Long userId) {
    // 1. 删 im_group_member
    groupMemberMapper.delete(groupId, userId);
    // 2. 删 im_conversation_member
    conversationMemberMapper.deleteByGroupAndUser(groupId, userId);
    // 3. 减 member_count
    groupMapper.decrMemberCount(groupId);
    // 4. 发系统消息给被踢者
    sendSystemMessage(userId, new ImSystemPayload("group_kick", groupId));
}
```

---

## 7. 数据迁移

### 7.1 Phase 0(无新增业务表)

只新增依赖,不改 schema。

### 7.2 Phase 1

新建 `im_conversation`、`im_conversation_member`、`im_message` 三张表。

### 7.3 Phase 1.1

新建 `im_group`、`im_group_member` 两张表。

### 7.4 迁移脚本

```sql
-- docs/database/im-schema-phase-1.sql
-- Phase 1 增量
SOURCE im-schema-phase-1.sql;

-- docs/database/im-schema-phase-1-1.sql
-- Phase 1.1 增量
SOURCE im-schema-phase-1-1.sql;
```

---

## 8. 性能考虑

### 8.1 索引

| 表 | 索引 | 用途 |
|---|---|---|
| `im_conversation` | `(type, target_id)` UNIQUE | 查找/创建会话 |
| `im_conversation_member` | `(user_id)` | 用户的会话列表 |
| `im_message` | `(conversation_id, created_at)` | 会话历史分页 |
| `im_message` | `(sender_id, client_msg_id)` UNIQUE | 去重 |
| `im_group` | `(dept_id)` | 部门群查找 |

### 8.2 大表优化(数据量大时启用)

```sql
-- im_message 按月分区
ALTER TABLE im_message
PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
  PARTITION p202606 VALUES LESS THAN (202607),
  PARTITION p202607 VALUES LESS THAN (202608),
  PARTITION p202608 VALUES LESS THAN (202609),
  PARTITION p202609 VALUES LESS THAN (202610),
  PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- 定期清理旧消息
DELETE FROM im_message WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 YEAR);
```

### 8.3 慢查询预案

| 慢查询 | 应对 |
|---|---|
| 用户的会话列表 | 已通过 `im_user_conversation` 优化,或直接 JOIN `im_conversation_member` |
| 会话历史分页 | `(conversation_id, created_at DESC)` 索引 |
| 未读总数 | Redis 缓存 |

---

## 9. 与 mica-admin 现有数据模型的关系

| mica-admin 表 | IM 模块使用方式 |
|---|---|
| `sys_user` | IM 用户身份、昵称、头像 |
| `sys_dept` | 部门群自动同步 |
| `sys_role` | IM 权限校验(如"是否可发消息") |
| `sys_message` | Phase 0 推送通道 |
| `sys_user_message` | 离线 IM 消息兜底 |
| `sys_file` | Phase 2.0+ 文件消息存储 |

**不重复造用户、组织、权限、文件相关表**。所有 `sys_*` 表的访问通过 mica-admin 现有 `ISys*Service`。

---

## 10. 数据字典

```sql
-- IM 角色
INSERT INTO sys_dict (type, description) VALUES ('im_role', 'IM 角色');
INSERT INTO sys_dict_info (type, label, value) VALUES
  ('im_role', '群主', 'owner'),
  ('im_role', '管理员', 'admin'),
  ('im_role', '成员', 'member');

-- IM 会话类型
INSERT INTO sys_dict (type, description) VALUES ('im_conversation_type', 'IM 会话类型');
INSERT INTO sys_dict_info (type, label, value) VALUES
  ('im_conversation_type', '单聊', 'p2p'),
  ('im_conversation_type', '群聊', 'group');

-- IM 消息类型
INSERT INTO sys_dict (type, description) VALUES ('im_message_type', 'IM 消息类型');
INSERT INTO sys_dict_info (type, label, value) VALUES
  ('im_message_type', '文本', 'text'),
  ('im_message_type', '图片', 'image'),
  ('im_message_type', '文件', 'file'),
  ('im_message_type', '系统', 'system');
```

---

## 11. 后续扩展(预留字段)

| 表 | 字段 | Phase | 说明 |
|---|---|---|---|
| `im_message` | `reply_to_id` | 2.0 | 引用消息 |
| `im_message` | `at_user_ids` | 2.0 | @提及用户(JSON 数组) |
| `im_conversation_member` | `mention_unread` | 2.0 | @我未读数 |
| `im_group` | `invite_confirm` | 2.0 | 加群是否需审核 |
| `im_group` | `mute_all` | 2.0 | 全员禁言 |

> 这些字段 Phase 1 不创建,Phase 2.0 通过 ALTER TABLE 添加,避免一开始过度设计。