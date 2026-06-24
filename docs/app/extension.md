# 二次开发扩展点

> mica-admin 是一个**通用框架**,实际业务能力由二次开发方按需扩展。
> 本文列出 App 用户**预期有但 mica-admin 当前不支持**的能力,
> 并给出落地点(后端改什么 + App 端怎么接入)。

## 扩展点速览

| # | 扩展点 | App 预期表现 | 后端工作量 | App 接入工作量 |
|---|---|---|---|---|
| 1 | 消息跳转业务单据 | 点击消息跳到对应单据 | 0.5 天 | 0.5 天 |
| 2 | 原生应用中心 | 菜单点开是 uniapp 原生页 | 0(纯前端) | 按菜单数 |
| 3 | 通讯录打电话 | 一键拨号 | 0(原生 `uni.makePhoneCall`) | 0.5 天 |
| 3.1 | 音视频通话 | App 内 VoIP | 1-2 周(SDK) | 2-3 天 |
| 4 | 审批/工作流 | App 端审批中心 | 1-2 周 | 1 周 |
| 5 | 考勤打卡 | 上下班打卡 | 2-3 天 | 1-2 天 |
| 6 | 系统级推送(APNs/华为/小米) | App 后台推送 | 0.5 天 | 1 天 |
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

## 3. 通讯录打电话与音视频通话

> 本节拆分 2 个子项:**打电话**(原生即可)、**音视频通话**(需 SDK)。

### 3.1 通讯录"打电话"(原生即可)

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
> 如需 VoIP 网络电话(类似钉钉),见 §3.2。

### 3.2 音视频通话(需 SDK)

#### 现状

mica-admin 不包含音视频能力。

#### 推荐方案

- 集成声网 Agora / 腾讯云 TRTC / 环信音视频 SDK
- App 端通过自定义信令协议拉起原生音视频界面

#### 后端改造(1-2 周)

- 接入音视频服务端 SDK
- 实现通话信令(CALL_INVITE / CALL_ACCEPT / CALL_REJECT / CALL_HANGUP)
- 可在业务消息中携带通话邀请

#### App 端接入(2-3 天)

- 集成 SDK 原生插件
- 通话 UI(全屏、来电浮窗)
- 后台/前台切换时 SDK 状态保存

> 此扩展点投入较大,通常 v1.1+ 评估。

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

## 6. 系统级推送(APNs / 华为 / 小米)

### 6.1 现状

- App 切到后台,iOS/Android 系统会 **杀进程**
- 此时新消息无法实时推送,只能等 App 回到前台后拉取 `/api/system/user/message/unread`
- iOS 用户看到的"通知"由系统级通道(APNs)提供;Android 由厂商通道(华为/小米/OPPO/VIVO)提供

### 6.2 推荐方案:uniPush(个推通道)

- App 端接入 uniPush(个推通道),`manifest.json` 配 push 模块
- 后端在检测到用户离线时,调用 uniPush REST API

### 6.3 后端改造(0.5 天)

```java
// 系统消息发送后,检测用户在线状态
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

### 6.4 App 端接入(1 天)

- 申请 uniPush 账号 + 配置 `manifest.json`
- 处理 push 回调(`uni.onPushMessage`)
- 点击通知 → 调 `getLaunchOptionsSync()` 取 `payload` → 跳业务单据

### 6.5 限制

- **微信小程序不支持 MQTT 长连接**,小程序端只能走 HTTP 轮询 + 微信模板消息
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
| 0 后端改造 | App **8** 个开箱即用模块(公告/系统消息/通讯录/文件/通知公告等) |
| 0.5 天 | 消息跳业务单据 / 系统级推送通道 |
| 1 天 | 通讯录"打电话"(原生拨号) |
| 1 周 | 工资条类小功能 + 数据看板 |
| 1-2 周 | 完整审批中心 |
| 2-3 周 | 考勤 + 系统级推送 + 数据看板 + 二次开发模块 |
| 4 周+ | 全功能企业 App(含音视频通话) |

按需投入,不要一上来就"大而全"。**App 1.0 + 1 个扩展点**通常就能满足 80% 内部办公需求。
**mica-admin 不内置即时通讯**,App 端如需 IM 走第三方 SDK 或自建(见第三方文档)。