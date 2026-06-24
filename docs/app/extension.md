# 二次开发扩展点

> mica-admin 是一个**通用框架**,实际业务能力由二次开发方按需扩展。
> 本文列出 App 用户**预期有但 mica-admin 当前不支持**的能力,
> 并给出落地点(后端改什么 + App 端怎么接入)。

## 扩展点速览

| # | 扩展点 | App 预期表现 | 后端工作量 | App 接入工作量 |
|---|---|---|---|---|
| 1 | 消息跳转业务单据 | 点击消息跳到对应单据 | 0.5 天 | 0.5 天 |
| 2 | 原生应用中心 | 菜单点开是 uniapp 原生页 | 0(纯前端) | 按菜单数 |
| 3 | **通讯录"发消息"** | 一键发起单聊 | **0(已内置 IM 模块)** | 1 天 |
| 3.1 | 通讯录打电话 | 一键拨号 | 0(原生 `uni.makePhoneCall`) | 0.5 天 |
| 3.2 | 音视频通话 | 单聊/群聊内 VoIP | 1-2 周(SDK) | 2-3 天 |
| 4 | 审批/工作流 | App 端审批中心 | 1-2 周 | 1 周 |
| 5 | 考勤打卡 | 上下班打卡 | 2-3 天 | 1-2 天 |
| 6 | **系统消息实时推送** | App 前台秒级触达 | **0(已内置 IM sys topic)** | 0.5 天 |
| 6.1 | 系统级推送(APNs/华为/小米) | App 后台推送 | 0.5 天 | 1 天 |
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

> 本节拆分 3 个子项:**发消息**(已就绪)、**打电话**(原生即可)、**音视频通话**(需 SDK)。

### 3.1 通讯录"发消息"(已就绪)

#### 现状

mica-admin IM 模块已完整实现并内嵌 broker。
通讯录用户详情页"发消息"按钮 **1.0 已可用**,无需任何后端改造。

#### App 端接入(1 天)

复用 mica-im 模块的 HTTP + MQTT 通道(详见 [api-mapping.md §12](./api-mapping.md#模块-12-im-即时通讯)
与 [docs/im/api-design.md](../im/api-design.md)):

```typescript
// contact/detail.vue
async function onSendMessageClick(peerUserId: number) {
  // 1. 获取/创建单聊会话
  const { conversation } = await createP2pConversation({ peerUserId })
  // 2. 跳单聊窗口
  uni.navigateTo({
    url: `/modules/im/chat-window?convId=${conversation.id}&type=p2p&peerId=${peerUserId}`
  })
}
```

注意要点:

- App 启动时已建立 MQTT 长连接 + 订阅 inbox(见 [features.md §12](./features.md#12-im-即时通讯-))
- "发消息"按钮需做防抖,避免重复点击产生多个会话
- 单聊窗口从 `chat-window` 复用,不要新建独立页面

### 3.2 通讯录"打电话"(原生即可)

#### 现状

uniapp 提供原生拨号 API,App 1.0 即可启用。

#### 后端改造

无。

#### App 端接入(0.5 天)

```typescript
// contact/detail.vue
function callPhone(phone: string) {
  if (!phone) {
    uni.showToast({ title: '该用户未留电话', icon: 'none' })
    return
  }
  uni.makePhoneCall({
    phoneNumber: phone,
    fail: (err) => {
      // 用户取消或权限被拒
      console.warn('拨号失败', err)
    }
  })
}
```

> App 端仅触发系统拨号器,实际通话由运营商承载,无需集成第三方电话 SDK。
> 如需 VoIP 网络电话(类似钉钉),见 §3.3。

### 3.3 音视频通话(需 SDK)

#### 现状

mica-im 仅文本/图片/文件消息,**不包含 VoIP 能力**。

#### 推荐方案

- 集成声网 Agora / 腾讯云 TRTC / 环信音视频 SDK
- 通话建立信令走 IM MQTT topic(如 `im/p2p/{userId}/inbox` 携带 `type: 'CALL_INVITE'`)
- App 端监听该 topic → 拉起原生音视频界面

#### 后端改造(1-2 周)

- 接入音视频服务端 SDK
- 实现通话信令(CALL_INVITE / CALL_ACCEPT / CALL_REJECT / CALL_HANGUP)
- 可复用 mica-im 群聊做多人通话

#### App 端接入(2-3 天)

- 集成 SDK 原生插件
- 通话 UI(全屏、来电浮窗)
- 后台/前台切换时 SDK 状态保存

> 此扩展点投入较大,通常 v1.1+ 评估。

### 3.4 查看 TA 的群(后端小补)

若要在用户详情页展示"该用户参与的群列表",后端需新增:

```
GET /api/im/users/{userId}/groups
→ GroupVo[]
权限:已登录
```

1-2 小时工作量。1.0 可省略,二次开发按需补。

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

> mica-mqtt 已内嵌,App 1.0 即可享受 **前台实时推送**;**后台推送**仍需厂商通道。

### 6.1 App 前台实时推送(IM sys topic,已就绪)

#### 现状

mica-admin `SysMessageServiceImpl.publish()` 已通过 `ImPushService` 把系统消息
推送到 `im/sys/{userId}/system` MQTT topic。App 前台时 MQTT 连接保持,
可即时收到通知,工作台角标 + Tab 角标实时刷新。

#### App 端接入(0.5 天)

App 启动时订阅 `im/sys/{userId}/system`,收到消息后:

```typescript
// src/modules/im/mqtt-client.ts (已内置)
client.on('message', (topic, payload) => {
  if (topic === `im/sys/${userId}/system`) {
    const notice = JSON.parse(payload.toString())
    // 1. 弹通知
    uni.showNotification({
      title: notice.title,
      content: notice.content,
      payload: { bizType: notice.bizType, bizId: notice.bizId }
    })
    // 2. 刷新工作台未读
    sysUnreadStore.refresh()
    // 3. 离线消息兜底(下次 App 启动时拉 sys_user_message)
  }
})
```

#### 离线兜底

App 后台/断网期间,`ImPushService` 同步把消息写入 `sys_user_message` 表。
App 重新前台时调 `GET /api/system/user/message/unread` 兜底拉取,**消息不丢**。

### 6.2 系统级推送(APNs / 华为 / 小米,需厂商通道)

#### 现状

- App 切到后台,iOS/Android 系统会 **杀进程** → MQTT 长连接断开
- 此时新消息无法通过 `im/sys/...` 实时推送,只能等 App 回到前台后兜底拉取
- iOS 用户看到的"通知"由系统级通道(APNs)提供;Android 由厂商通道(华为/小米/OPPO/VIVO)提供

#### 推荐方案:uniPush(个推通道)

- App 端接入 uniPush(个推通道),`manifest.json` 配 push 模块
- 后端在 `ImPushService` 检测到用户离线时,调用 uniPush REST API

#### 后端改造(0.5 天)

```java
// ImPushServiceImpl.pushSystemMessage() 中追加
if (!userIsOnline(userId)) {
    uniPushClient.sendToSingle(userId, Map.of(
        "title", msg.getTitle(),
        "content", msg.getContent(),
        "payload", JsonUtil.toJson(Map.of(
            "bizType", msg.getCategory(),
            "bizId", msg.getId()
        ))
    ));
}
```

#### App 端接入(1 天)

- 申请 uniPush 账号 + 配置 `manifest.json`
- 处理 push 回调(`uni.onPushMessage`)
- 点击通知 → 调 `getLaunchOptionsSync()` 取 `payload` → 跳业务单据

#### 限制

- **微信小程序不支持 MQTT 长连接**,IM 模块在小程序端走 HTTP 轮询 + 微信模板消息
  (具体方案见 [docs/im/architecture.md §1.1](../im/architecture.md#11-与-app-端集成))
- 厂商通道需各厂商审核,首次接入周期 1-2 周

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
| 0 后端改造 | App **12** 个开箱即用模块(含 IM 即时通讯) |
| 0.5 天 | 消息跳业务单据 / 系统级推送通道 |
| 1 天 | 通讯录"打电话"(原生拨号) |
| 1-2 天 | 通讯录"发消息"(已内置 IM,只需 App UI) |
| 1 周 | 工资条类小功能 + 数据看板 |
| 1-2 周 | 完整审批中心 |
| 2-3 周 | 考勤 + 实时推送(已内置前台)+ 数据看板 + 二次开发模块 |
| 4 周+ | 全功能企业 App(含音视频通话) |

按需投入,不要一上来就"大而全"。**App 1.0 + 1 个扩展点**通常就能满足 80% 内部办公需求。
**通讯录 IM 与系统消息推送已在 1.0 范围内**(mica-mqtt 已内嵌),无需任何扩展点投入。