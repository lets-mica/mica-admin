# 二次开发扩展点

> mica-admin 是一个**通用框架**,实际业务能力由二次开发方按需扩展。
> 本文列出 App 用户**预期有但 mica-admin 当前不支持**的能力,
> 并给出落地点(后端改什么 + App 端怎么接入)。

## 扩展点速览

| # | 扩展点 | App 预期表现 | 后端工作量 | App 接入工作量 |
|---|---|---|---|---|
| 1 | 消息跳转业务单据 | 点击消息跳到对应单据 | 0.5 天 | 0.5 天 |
| 2 | 原生应用中心 | 菜单点开是 uniapp 原生页 | 按菜单数 | 按菜单数 |
| 3 | 通讯录拨打/IM | 一键拨号、发消息 | 1 周 | 2-3 天 |
| 4 | 审批/工作流 | App 端审批中心 | 1-2 周 | 1 周 |
| 5 | 考勤打卡 | 上下班打卡 | 2-3 天 | 1-2 天 |
| 6 | 实时推送 | 消息秒级触达 | 2-3 天 | 1 天 |
| 7 | 数据看板(图表) | 工作台数据卡片 | 1 天 | 1-2 天 |

---

## 1. 消息跳转业务单据

### 现状

`sys_user_message` 表只记录 `message_id` + `user_id` + `read_flag`,**没有 `biz_type/biz_id/url` 字段**。
App 端点击消息**只能标记已读或弹详情**,无法跳到具体业务单据。

### 后端改造(0.5 天)

#### 1.1 数据库迁移

```sql
-- 新增字段
ALTER TABLE sys_user_message
  ADD COLUMN biz_type VARCHAR(32) DEFAULT NULL COMMENT '业务类型',
  ADD COLUMN biz_id   VARCHAR(64) DEFAULT NULL COMMENT '业务单据ID',
  ADD COLUMN biz_url  VARCHAR(255) DEFAULT NULL COMMENT '跳转URL(H5/App路由)';
```

#### 1.2 Entity 修改

```java
// SysUserMessage.java 新增字段
private String bizType;
private String bizId;
private String bizUrl;
```

#### 1.3 Service 层

`SysMessageServiceImpl.publish()` 中推送消息时,把 `bizType/bizId/bizUrl` 写入 `sys_user_message`。

#### 1.4 Controller 暴露

`SysUserMessageController` 的 `UserMessageVo` 中返回新字段,供 App 端解析。

### App 端接入(0.5 天)

#### 1.5 UserMessageVo 类型扩展

```typescript
interface UserMessageVo {
  // ... 现有字段
  bizType?: 'leave' | 'expense' | 'order' | 'meeting' | string
  bizId?: string
  bizUrl?: string         // App 路由 或 H5 链接
}
```

#### 1.6 点击消息路由逻辑

```typescript
// message/detail.vue
function onMessageClick(msg: UserMessageVo) {
  // 1. 标记已读
  markRead(msg.id)
  // 2. 有 bizUrl → 跳转
  if (msg.bizUrl) {
    if (msg.bizUrl.startsWith('http')) {
      // H5 链接 → WebView
      uni.navigateTo({ url: `/pages/webview/index?url=${encodeURIComponent(msg.bizUrl)}` })
    } else {
      // App 原生路由
      uni.navigateTo({ url: msg.bizUrl })
    }
  } else {
    // 无 bizUrl → 弹详情
    uni.navigateTo({ url: `/pages/message/detail?id=${msg.id}` })
  }
}
```

---

## 2. 原生应用中心

### 现状

mica-admin Web 端菜单组件(`vben/`)采用 Vben Admin 框架,**很难直接搬到 uniapp**。
App 1.0 策略:**业务菜单点击统一走 WebView**,打开 mica-admin Web 对应路径。

### 二次开发目标

为 mica-admin 的每个业务菜单提供 **uniapp 原生页面**,让 App 用户有更好的移动体验。

### 后端改造(无)

纯前端工作。后端 `/api/auth/menus` 已能正确返回菜单元数据。

### App 端接入(按菜单数)

#### 2.1 建立菜单 → 原生页面映射表

```typescript
// src/modules/menu/routeMap.ts
export const nativeRouteMap: Record<string, string> = {
  // path(后端菜单) → App 原生路由
  '/system/user': '/pages-native/system/user-list',
  '/system/role': '/pages-native/system/role-list',
  '/system/dept': '/pages-native/system/dept-tree',
  '/system/dict': '/pages-native/system/dict-list',
  '/monitor/server': '/pages-native/monitor/server',
  '/monitor/log': '/pages-native/monitor/log',
  // ...按需扩展
}
```

#### 2.2 菜单点击路由逻辑

```typescript
function onMenuClick(menu: MenuVo) {
  const nativeRoute = nativeRouteMap[menu.path || '']
  if (nativeRoute) {
    // 已原生化 → 跳原生页
    uni.navigateTo({ url: nativeRoute })
  } else {
    // 未原生化 → 走 WebView 兜底
    uni.navigateTo({
      url: `/pages/webview/index?path=${encodeURIComponent(menu.path)}`
    })
  }
}
```

#### 2.3 icon 映射

```typescript
// src/modules/menu/iconMap.ts
export const iconMap: Record<string, string> = {
  'icon-park:user': 'icon-user',
  'icon-park:setting-config': 'icon-config',
  // ...mica-admin-web icon → uniapp icon
}
```

---

## 3. 通讯录拨打/IM

### 现状

App 1.0 通讯录用户详情页"拨号"、"发消息"按钮**置灰占位**(详见 [wireframes.md §6.3](./wireframes.md#63-用户详情))。

### 二次开发目标

- 一键拨号(原生 `uni.makePhoneCall`)
- 一键发消息(集成 IM SDK:环信/腾讯云/融云)

### 推荐方案:复用 mica-im 模块

**mica-admin 官方 IM 模块**([docs/im/](../im/README.md))基于 mica-mqtt 实现,
覆盖单聊 + 群聊 + 部门群,**直接对接即可**,无需引入第三方 IM SDK。

#### 接入步骤

1. **后端**:按 [docs/im/roadmap.md](../im/roadmap.md) 实施
   - IM 代码位于 `mica-admin-server/src/main/java/net/dreamlu/mica/admin/im/`(子包,**非独立模块**)
   - 引入 `mica-mqtt-spring-boot-starter` 依赖(见 `docs/im/architecture.md §2.1`)
   - 数据库迁移脚本在 `docs/database/im-schema-phase-1*.sql`
2. **App 端**:按 [docs/im/architecture.md §13](../im/architecture.md#13-与-app-端集成) 集成:
   - 安装 mqtt-client 库
   - 封装 `src/modules/extension/im/mqtt/`
   - 替换占位 UI
3. 通讯录"发消息"按钮启用,跳到 `pages/extension/im/chat-p2p`

#### 后端新增接口(IM 模块提供)

- `GET /api/im/conversations/p2p/{userId}` — 获取/创建与某用户的会话
- `GET /api/im/conversations/{id}/messages` — 历史消息
- 详见 [docs/im/api-design.md](../im/api-design.md)

### 替代方案:第三方 IM SDK

若选择环信/腾讯云/融云:

#### 后端改造(1 周)

- 接入 IM 服务端 SDK
- 后端提供 IM token 签发接口 `/api/im/token`(供 App 端登录 IM)

#### App 端接入(2-3 天)

```typescript
// contact/detail.vue
function callPhone(phone: string) {
  uni.makePhoneCall({ phoneNumber: phone })
}

function sendIM(userId: number) {
  // 集成 IM SDK(以环信为例)
  const conversation = EMClient.getInstance().chatManager
    .getConversation(userId.toString(), EMConversationType.Chat, true)
  uni.navigateTo({ url: `/pages-im/chat?userId=${userId}` })
}
```

---

## 4. 审批/工作流

### 现状

`grep -r "flowable\|activiti\|camunda" mica-admin-server` **全部 0 命中**。
mica-admin **没有工作流引擎**。

### 二次开发目标

App 端审批中心,支持请假/报销/出差/采购等单据的提交、审批、通知。

### 后端改造(1-2 周)

#### 4.1 数据库设计

```sql
-- 审批单主表
CREATE TABLE sys_approval (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  biz_type      VARCHAR(32)  NOT NULL COMMENT '业务类型:leave/expense/...',
  biz_id        VARCHAR(64)  NOT NULL COMMENT '业务单据ID',
  applicant_id  BIGINT       NOT NULL COMMENT '申请人',
  title         VARCHAR(200) NOT NULL,
  status        VARCHAR(16)  NOT NULL COMMENT 'pending/approved/rejected/withdrawn',
  current_node  VARCHAR(32)  COMMENT '当前节点',
  form_data     JSON         COMMENT '表单数据',
  remark        VARCHAR(500),
  created_at    DATETIME,
  updated_at    DATETIME,
  finished_at   DATETIME,
  KEY idx_applicant (applicant_id),
  KEY idx_status (status)
);

-- 审批流转日志
CREATE TABLE sys_approval_log (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  approval_id  BIGINT       NOT NULL,
  node_code    VARCHAR(32)  NOT NULL COMMENT '节点编码',
  approver_id  BIGINT       NOT NULL,
  action       VARCHAR(16)  NOT NULL COMMENT 'approve/reject/transfer/withdraw',
  comment      VARCHAR(500),
  created_at   DATETIME,
  KEY idx_approval (approval_id),
  KEY idx_approver (approver_id)
);
```

#### 4.2 实体、Service、Controller

按 mica-admin 现有模式:

- `entity/SysApproval.java`
- `entity/SysApprovalLog.java`
- `service/ISysApprovalService.java` + `impl/SysApprovalServiceImpl.java`
- `controller/SysApprovalController.java`

#### 4.3 关键接口

```java
@RestController
@RequestMapping("/api/system/approval")
public class SysApprovalController {

  // 我发起的
  @GetMapping("/submitted")
  public Page<SysApproval> submitted(Page page, AuthUser user) { ... }

  // 待我审批的
  @GetMapping("/pending")
  public Page<SysApproval> pending(Page page, AuthUser user) { ... }

  // 我已审批的
  @GetMapping("/handled")
  public Page<SysApproval> handled(Page page, AuthUser user) { ... }

  // 详情
  @GetMapping("/{id}")
  public SysApprovalVo getInfo(@PathVariable Long id) { ... }

  // 提交申请
  @PostMapping
  public void submit(@RequestBody SysApproval entity) { ... }

  // 审批动作
  @PostMapping("/{id}/action")
  public void action(@PathVariable Long id, @RequestBody ApprovalActionVo vo) { ... }
}
```

#### 4.4 业务单据

每种业务单据(请假/报销/...)单独建表,通过 `biz_type` + `biz_id` 关联 `sys_approval`。

### App 端接入(1 周)

新增 `src/modules/extension/approval/` 目录:

```
approval/
├── list-pending.vue        # 待我审批
├── list-submitted.vue      # 我发起的
├── list-handled.vue        # 我已审批的
├── detail.vue              # 审批详情
├── submit-leave.vue        # 提交请假
├── submit-expense.vue      # 提交报销
└── api.ts                  # API 封装
```

工作台"待办"卡片可直接显示审批数,通过 `/api/system/approval/pending/count` 查询。

### 二次开发优先级建议

- **P0**:请假(最常见,流程简单)
- **P1**:报销、出差
- **P2**:采购、用车、加班

---

## 5. 考勤打卡

### 现状

mica-admin **无 `sys_attendance` 表**。

### 二次开发目标

App 端上下班打卡、请假、外出、补卡。

### 后端改造(2-3 天)

#### 5.1 数据库

```sql
CREATE TABLE sys_attendance (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT NOT NULL,
  type        VARCHAR(16) NOT NULL COMMENT 'check_in/check_out/leave/out/back',
  clock_time  DATETIME NOT NULL,
  location    VARCHAR(200) COMMENT '地理位置',
  latitude    DECIMAL(10,6),
  longitude   DECIMAL(10,6),
  remark      VARCHAR(500),
  created_at  DATETIME,
  KEY idx_user (user_id),
  KEY idx_time (clock_time)
);

-- 排班表(可选)
CREATE TABLE sys_attendance_shift (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT NOT NULL,
  work_date   DATE NOT NULL,
  start_time  TIME,
  end_time    TIME,
  UNIQUE KEY uk_user_date (user_id, work_date)
);
```

#### 5.2 接口

```java
@RestController
@RequestMapping("/api/system/attendance")
public class SysAttendanceController {

  @PostMapping("/clock")        // 打卡
  public void clock(@RequestBody ClockVo vo) { ... }

  @GetMapping("/today")         // 今日打卡记录
  public List<SysAttendance> today(AuthUser user) { ... }

  @GetMapping("/calendar")      // 月历视图
  public Map<String, Object> calendar(@RequestParam String month, AuthUser user) { ... }
}
```

### App 端接入(1-2 天)

```
extension/attendance/
├── clock.vue                 # 打卡主页
├── calendar.vue              # 月历
├── apply-leave.vue           # 请假申请
└── api.ts
```

调用 `uni.getLocation` 获取 GPS,提交打卡。

---

## 6. 实时推送

### 现状

mica-mqtt **在 mica-admin-server 的 pom 里根本没引**,代码层完全没用。
当前 App 端只能**轮询** `/api/system/user/message/unread`(1 分钟一次)。

### 二次开发目标

App 进入前台/后台时,**实时接收消息推送**,秒级触达。

### 方案 A:App 端 SSE/WebSocket 轮询增强(轻量)

不接 mqtt,改用更短的轮询间隔(10-30s)+ 后台静默推送(若 App 支持)。

- 后端改造:**无**
- App 改造:1 天
- 适用场景:**MVP 阶段够用**

### 方案 B:接入 mica-mqtt(中等)

#### 后端改造(2-3 天)

1. **引入依赖**:

```xml
<dependency>
  <groupId>org.dromara.mica-mqtt</groupId>
  <artifactId>mica-mqtt-spring-boot-starter</artifactId>
  <version>2.4.x</version>
</dependency>
```

2. **配置** (`application.yml`):

```yaml
mica:
  mqtt:
    enabled: true
    server:
      host: 0.0.0.0
      port: 1883
    client:
      enabled: true
      server-host: mqtt-broker.local
      server-port: 1883
```

3. **`SysMessageServiceImpl.publish()` 增加推送**:

```java
@Service
@RequiredArgsConstructor
public class SysMessageServiceImpl implements ISysMessageService {

    private final MqttTemplate mqttTemplate;  // mica-mqtt 提供的

    @Override
    @Transactional
    public void publish(Long messageId, List<Long> userIds, List<Long> deptIds) {
        // ... 现有逻辑:写 sys_user_message ...

        // 新增:mqtt 推送
        SysMessage msg = this.getById(messageId);
        for (Long userId : targetUserIds) {
            mqttTemplate.send(
                "/mica/user/" + userId,
                JsonUtil.toJson(Map.of(
                    "id", msg.getId(),
                    "title", msg.getTitle(),
                    "content", msg.getContent(),
                    "category", msg.getCategory()
                ))
            );
        }
    }
}
```

#### App 端改造(1 天)

- **原生 App(iOS/Android)**:用 `mqtt.miniprogram` 或原生 MQTT 客户端订阅
- **H5**:用 WebSocket over MQTT
- **小程序**:**不工作**(小程序无长连接),需退化为方案 A

> 小程序限制:只能 30s 后台 → 实时推送在小程序端无效。

### 方案 C:第三方推送(推荐)

跳过 mica-mqtt,直接用:

- App 端接入 uniPush(个推通道)
- 后端调用 uniPush REST API 推送

- 后端改造:0.5 天
- App 改造:1 天
- **支持 App + 小程序两端**

### 推荐

**App 1.0 用方案 A(轮询),1.1 用方案 C(uniPush),2.0 才考虑 mica-mqtt**。

---

## 7. 数据看板(图表)

### 现状

mica-admin Web 端已有"工作台"数据卡片,但**App 端工作台无图表**。

### 二次开发目标

App 工作台"数据概览"区域显示关键指标图表(管理员可见)。

### 后端改造(1 天)

利用 mica-admin 已有的 **magic-api 低代码平台** 编写数据聚合接口,**无需新增 Java 代码**:

```sql
-- magic-api 表 magic_api_file 中新增一个 API
-- /api/app/dashboard/summary
-- 返回今日访问量、订单数、用户数等
```

或新增 Java 接口 `/api/app/dashboard/summary`。

### App 端接入(1-2 天)

- 引入 uCharts 或 F2 图表库
- 工作台"数据概览"卡片展示折线图/柱状图
- 仅管理员可见

---

## 通用二次开发流程

任何扩展点都按以下流程接入:

```
1. 后端新增表/接口
   ↓
2. 后端启动验证(Swagger /doc.html 可看)
   ↓
3. App 端 pnpm api 重新生成 swagger 客户端
   ↓
4. App 端 src/modules/extension/{name}/ 写实现
   ↓
5. 在 pages.json 注册路由
   ↓
6. 替换占位 UI(若有)
   ↓
7. 测试(本地 + dev 环境)
```

> App 通用模块(`auth/`、`profile/`、`message/`、`menu/`、`contacts/`、`notice/`)**不应被修改**。
> 二次开发**只新增**,不修改。

---

## 二次开发示例:加一个"工资条"功能

作为快速参考,演示一个完整扩展点。

### 步骤

#### 1. 后端

```sql
-- 加表
CREATE TABLE sys_payslip (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT NOT NULL,
  month       VARCHAR(7) NOT NULL,  -- '2026-06'
  amount      DECIMAL(10,2),
  detail      JSON,
  created_at  DATETIME,
  UNIQUE KEY uk_user_month (user_id, month)
);
```

```java
// controller/SysPayslipController.java
@RestController
@RequestMapping("/api/system/payslip")
public class SysPayslipController extends BaseController {
    @GetMapping("/my")
    public List<SysPayslip> my(AuthUser user) {
        return payslipService.listByUserId(user.getUserId());
    }
}
```

#### 2. App 端

```
src/modules/extension/payslip/
├── list.vue       # 我的工资条列表
├── detail.vue     # 单月详情
└── api.ts
```

`pages.json`:
```json
{
  "path": "pages/extension/payslip/list",
  "style": { "navigationBarTitleText": "工资条" }
}
```

#### 3. 替换占位

把"工资条"图标加入工作台快捷入口 / 应用中心菜单(通过 `/api/auth/menus` 配置)。

---

## 总结

| 投入 | 能拿到 |
|---|---|
| 0 后端改造 | App 11 个开箱即用模块 |
| 0.5-1 天 | 消息跳业务单据 |
| 1 周 | 通讯录 IM + 工资条类小功能 |
| 1-2 周 | 完整审批中心 |
| 2-3 周 | 考勤 + 实时推送 + 数据看板 |
| 4 周+ | 全功能企业 App |

按需投入,不要一上来就"大而全"。**App 1.0 + 1 个扩展点**通常就能满足 80% 内部办公需求。