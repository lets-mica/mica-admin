# App 功能详述

> 每个模块都标注:**后端依赖**、**App 端功能**、**交互细节**、**二次开发点**。

## 模块清单

| # | 模块 | 后端依赖 | 状态 |
|---|---|---|---|
| 1 | 登录/认证 | `/api/auth/*` + `/api/session` | 🟢 可做 |
| 2 | 工作台(首页) | 聚合现有接口(含 IM 未读) | 🟢 可做 |
| 3 | 消息中心 | `/api/system/user/message/*` | 🟢 可做 |
| 4 | 应用中心(动态菜单) | `/api/auth/menus` | 🟢 可做 |
| 5 | 我的(个人中心) | `/api/system/users/*` | 🟢 可做 |
| 6 | 通讯录 | `/api/system/users` + `/api/system/dept` | 🟢 可做 |
| 7 | 通知公告 | `/api/system/notice` | 🟢 可做 |
| 8 | 文件中心 | `/api/upload/**`(x-file-storage) | 🟢 可做 |
| 9 | Token 管理(管理员) | `/api/auth/token` | 🟢 可做 |
| 10 | 监控(简化版) | `/api/system/monitor/server` | 🟢 可做 |
| 11 | 字典查询 | `/api/system/dict` + `/api/system/dict-info` | 🟢 可做 |
| 12 | **IM 即时通讯** | `/api/im/*` + MQTT `ws://host:8083/mqtt` | 🟢 可做 |

---

## 1. 登录/认证

### 后端依赖

| 端点 | 用途 |
|---|---|
| `GET /api/auth/public-key` | 取登录密码加密 RSA 公钥 |
| `GET /api/auth/captcha` | 取算术图形验证码(uuid + base64) |
| `POST /api/session` | 提交 form-urlencoded 登录 |
| `GET /api/logout` | 注销(清 token) |
| `GET /api/auth/info` | 当前用户信息 + 用户级 RSA 公钥 |

### App 端功能

- **登录页**:账号、密码、算术验证码、登录按钮、忘记密码入口
- **RSA 加密**:密码提交前用公钥加密(form-urlencoded)
- **多租户切换**:支持同一账号在不同租户登录(若启用)
- **登录态保持**:JWT token 存 `pinia-plugin-persistedstate` + `uni.setStorageSync`
- **自动登录**:App 启动检测 token 有效性,有效则跳过登录页

### 交互细节

- 验证码图片点击 → 调 `/api/auth/captcha` 换一题
- 登录失败 → Toast 提示(`code !== 0` 走拦截器统一处理)
- 401 → 清 token,跳登录页
- 登录成功 → 拉 `/api/auth/info` 缓存用户信息,再决定跳工作台

### 二次开发点

- 无。完全复用 mica-admin-web 的 `auth-mica-admin.ts` 逻辑。

---

## 2. 工作台(首页)

### 后端依赖

| 端点 | 用途 |
|---|---|
| `GET /api/auth/info` | 当前用户信息 |
| `GET /api/system/user/message/unread` | 系统消息未读数 |
| `GET /api/im/conversations/unread-total` | **IM 未读消息总数** |
| `GET /api/system/notice?page=1&size=5` | 最新公告 |

### App 端功能

- **顶部问候**:用户头像、姓名、部门、今日日期
- **未读徽标**:右上角铃铛显示 **系统消息 + IM 未读总数**;Tab 栏"消息"图标也叠加红点
- **待办摘要**:最新 3 条待办类消息(category=business)
- **公告摘要**:最新 3 条系统公告
- **快捷入口宫格**:通讯录、文件、Token、字典、日志、监控、**IM 会话**(按权限动态显隐)
- **下拉刷新**:刷新所有模块

### 交互细节

- 工作台所有数据并发拉取,显示骨架屏
- 点击公告项 → 跳公告详情
- 点击快捷入口 → 跳对应模块(或应用中心)
- **角色化显示**:普通员工不展示监控/日志/Token 等管理入口
- **会话快捷入口**直接拉起对应单聊/群聊窗口(传 `convId` 或 `groupId`)

### 二次开发点

- "今日待办"统计可对接二次开发的审批/工单模块(见 [extension.md](./extension.md))
- 自定义卡片:可在工作台 `extension/` 下注册自定义 Widget(见 [extension.md §7](./extension.md#7-自定义工作台卡片))

---

## 3. 消息中心

### 后端依赖

| 端点 | 用途 |
|---|---|
| `GET /api/system/user/message/unread` | 未读消息(顶部红点) |
| `GET /api/system/user/message?page=&size=` | 我的消息分页 |
| `PUT /api/system/user/message/read/{id}` | 标记单条已读 |
| `PUT /api/system/user/message/read-all` | 全部已读 |

### App 端功能

- **Tab 切换**:全部 / 未读(可选)
- **消息列表**:分类徽标(system/business/security/activity)
- **已读状态**:已读灰色,未读粗体 + 红点
- **批量操作**:长按进入多选模式 → 批量已读 / 删除(App 端仅批量已读,删除走 Web)
- **搜索**:按标题/内容模糊搜索

### 交互细节

- 列表左滑 → 标记已读 / 删除
- 点击消息 → 标记已读 + 弹详情
- 详情页展示消息标题、内容、时间、发送方

### 二次开发点

- **消息跳转业务单据**:`sys_user_message` 当前没有 `biz_type/biz_id`,**点击消息只能"标记已读"或弹详情**,无法跳具体业务。二次开发扩展点见 [extension.md §1](./extension.md#1-消息跳转业务单据)

---

## 4. 应用中心(动态菜单)

### 后端依赖

| 端点 | 用途 |
|---|---|
| `GET /api/auth/menus` | 当前用户菜单树 |

### App 端功能

- **菜单分组**:按父菜单分组展示
- **图标渲染**:后端 `MenuVo.icon` 字段决定图标(uniapp 需建立 icon 映射表)
- **权限显隐**:无权限的菜单自动不显示(由后端 `getNavByRoleIds` 控制)
- **点击行为**:
  - 内置菜单(消息/我的等)→ 跳对应 Tab 或子页面
  - 业务菜单(用户/角色/菜单等)→ App 1.0 **统一走 WebView**,打开 mica-admin Web 对应路径
  - 占位菜单(二次开发未完成)→ 友好提示"该功能即将上线"

### 交互细节

- 菜单加载失败 → 显示"菜单加载失败,下拉重试"
- WebView 打开后 → 支持后退手势返回 App
- 长按菜单 → 可"添加到工作台快捷入口"(本地缓存)

### 二次开发点

- **原生应用中心**:每个菜单写 uniapp 原生页面替换 WebView(见 [extension.md §2](./extension.md#2-原生应用中心))

### icon 映射方案

mica-admin 后端菜单 icon 字段是 Element Plus / Vben 图标名,uniapp 不直接支持。
建议在前端维护一份映射表:

```typescript
// src/modules/menu/iconMap.ts
export const iconMap: Record<string, string> = {
  'user': 'icon-user',
  'role': 'icon-role',
  'menu': 'icon-menu',
  'dept': 'icon-dept',
  'dict': 'icon-dict',
  // ...
}
```

未映射的 icon 显示默认占位图标。

---

## 5. 我的(个人中心)

### 后端依赖

| 端点 | 用途 |
|---|---|
| `GET /api/auth/info` | 当前用户信息(含头像/邮箱/手机) |
| `PUT /api/system/users/center` | 修改个人资料 |
| `POST /api/system/users/updatePass` | 修改密码 |
| `POST /api/system/users/avatar` | 修改头像(上传文件) |
| `POST /api/system/users/updateEmail` | 修改邮箱(走邮件验证码) |
| `POST /api/system/code/resetEmail` | 发送邮箱验证码 |
| `GET /api/logout` | 退出登录 |

### App 端功能

- **顶部信息卡**:头像、姓名、工号、部门
- **个人资料编辑**:昵称、手机号、邮箱(头像点击调相册选择)
- **修改密码**:旧密码 + 新密码 + 确认密码(走 RSA 加密)
- **修改邮箱**:邮箱 + 验证码 → 提交
- **语言切换**:中文 / English(本地 i18n,无需后端)
- **清除缓存**:清本地缓存(图片/列表数据)
- **版本信息**:App 版本号、构建号
- **退出登录**:二次确认 → 清 token → 跳登录页

### 交互细节

- 修改密码后强制重新登录
- 头像上传走 `uni.chooseImage` + `uni.uploadFile`
- 邮箱验证码 60s 倒计时

---

## 6. 通讯录

### 后端依赖

| 端点 | 用途 |
|---|---|
| `GET /api/system/dept` | 部门树 |
| `GET /api/system/users?blurry=xxx` | 用户搜索(全字段) |
| `GET /api/im/users/search?keyword=&limit=` | IM 轻量搜索(姓名/工号,排除自己+禁用账号) |
| `POST /api/im/conversations/p2p` | 获取/创建与某用户的单聊会话(供"发消息"按钮) |

### App 端功能

- **部门树浏览**:树形展开/折叠,显示部门人数
- **部门下用户**:点击部门看部门成员
- **用户搜索**:按姓名/工号/手机号/邮箱模糊搜索(走 `/api/system/users`)
- **个人信息卡**:头像、姓名、部门、岗位、手机、邮箱
- **快捷操作**:
  - **发消息**(1.0 已可用):点击 → 调 `/api/im/conversations/p2p` 获取/创建会话 → 拉起 IM 单聊窗口
  - **打电话**(1.0 仍占位):需二次开发(见 [extension.md §3](./extension.md#3-通讯录拨打im))

### 交互细节

- 搜索结果点击 → 跳用户详情
- 用户详情展示所有可见字段 + 操作按钮区
- 部门按层级缩进展示
- "发消息"按钮防抖(避免重复点击创建多次会话)

### 二次开发点

- **打电话**:集成原生拨号或第三方电话 SDK(见 [extension.md §3](./extension.md#3-通讯录拨打im))
- **查看 TA 的群**:用户详情可展示该用户参与的群列表(需后端补 `GET /api/im/users/{id}/groups` 端点,见 [extension.md §3.1](./extension.md#3-通讯录拨打im))

---

## 7. 通知公告

### 后端依赖

| 端点 | 用途 |
|---|---|
| `GET /api/system/notice?page=&size=` | 公告列表 |
| `GET /api/system/notice/{id}` | 公告详情 |

### App 端功能

- **公告列表**:按发布时间倒序,显示标题、摘要、时间
- **公告详情**:富文本/HTML 内容渲染(需 HTML 解析库)
- **搜索**(可选):按标题模糊搜索
- **置顶**(可选):后端 `seq` 字段决定排序,App 端识别 `seq=100` 等高优先级置顶

### 交互细节

- 详情页用 `uni-app` 的 rich-text 组件渲染 HTML
- 外链处理:HTML 中的链接可点击跳转

### 二次开发点

- **评论功能**:需后端新增 `sys_notice_comment` 表

---

## 8. 文件中心

### 后端依赖

| 端点 | 用途 |
|---|---|
| `POST /api/upload/upload` | 上传文件(由 x-file-storage 提供) |
| `GET /api/upload/{platform}/{filename}` | 文件直读 |
| `DELETE /api/upload/{id}` | 删除文件 |

### App 端功能

- **上传文件**:相册选择 / 拍照 → 上传 → 显示进度
- **文件列表**:本地记录的上传历史(本地缓存)
- **文件预览**:图片直接展示,PDF/Office 走 `uni.openDocument`
- **最近上传**:最近 20 条记录

### 交互细节

- 上传进度显示百分比
- 大文件(>10MB)提示用户
- 图片压缩后再上传(`uni.compressImage`)

### 二次开发点

- **文件协作**:需后端新增权限/分享/评论能力

---

## 9. Token 管理(管理员)

### 后端依赖

| 端点 | 用途 |
|---|---|
| `GET /api/auth/token?filter=` | token 列表 |
| `DELETE /api/auth/token` | 踢出用户 |

### App 端功能

- **在线用户列表**:用户名、最后活跃时间、token 状态
- **踢出用户**:选中 token → 二次确认 → 踢出
- **筛选**:按用户名模糊搜索

### 交互细节

- 仅 `isAdmin=true` 的用户可见此入口
- 踢出后该用户下次请求会 401,自动跳登录页

---

## 10. 监控(简化版)

### 后端依赖

| 端点 | 用途 |
|---|---|
| `GET /api/system/monitor/server` | 服务器监控(CPU/内存/磁盘/JVM) |
| `GET /admin/im/stats/online` | IM 在线用户数 |
| `GET /admin/im/stats/broker` | IM broker 状态 |

### App 端功能

- **服务器概览**:CPU 使用率、内存使用率、JVM 堆内存、磁盘空间
- **IM 概览**(管理员):在线用户数、broker 连接数、当前订阅 topic 数
- **简化展示**:App 端只展示关键指标,不做详细图表

### 交互细节

- 仅管理员可见
- 30s 自动刷新(可手动触发)
- 阈值告警(CPU > 80% 红字显示)

### 二次开发点

- **SQL 监控、Redis 监控**:App 端暂不展示(数据量大、阅读体验差)
- **阈值告警推送**:**已可通过 IM MQTT 实现** —— 后端监控线程检测到阈值越界,
  通过 `ImPushService` 推送 `im/sys/{userId}/system` topic,App 端订阅后弹通知;
  无需引入额外推送通道(详见 [extension.md §6](./extension.md#6-实时推送))

---

## 11. 字典查询

### 后端依赖

| 端点 | 用途 |
|---|---|
| `GET /api/system/dict` | 字典列表 |
| `GET /api/system/dict-info?type=` | 按 type 查字典项 |

### App 端功能

- **字典列表**:所有字典类型
- **字典项浏览**:查看某个 type 下的所有 label/value
- **搜索**:按 type 名称搜索

### 交互细节

- 仅管理员可见
- 主要用于排查问题,日常使用频率低

### 二次开发点

- 无。属于"通用工具"模块。

---

## 12. IM 即时通讯 ⭐

> mica-mqtt broker 已内嵌,App 1.0 可直接复用,无需后端改造。完整 HTTP 接口见
> [api-mapping.md §模块 12](./api-mapping.md#模块-12-im-即时通讯);
> MQTT Topic 协议见 [docs/im/api-design.md](../im/api-design.md)。

### 后端依赖

| 端点 / Topic | 用途 |
|---|---|
| `POST /api/im/conversations/p2p` | 创建/获取单聊会话 |
| `GET /api/im/conversations` | 会话列表(分页、按更新时间倒序) |
| `GET /api/im/conversations/{convId}/messages?page=&size=` | 加载历史消息(分页) |
| `POST /api/im/conversations/{convId}/mark-read` | 标记会话已读(推 `im/ack/...`) |
| `GET /api/im/conversations/unread-total` | 当前用户未读总数(工作台角标) |
| `POST /api/im/conversations/mark-all-read` | 全部已读 |
| `DELETE /api/im/conversations/messages/{messageId}` | 撤回消息(2 分钟内) |
| `GET /api/im/groups/my` | 当前用户参与的群列表 |
| `POST /api/im/groups` | 创建群(传 name + memberIds) |
| `GET /api/im/groups/{groupId}` | 群详情 |
| `PUT /api/im/groups/{groupId}` | 修改群名/公告 |
| `DELETE /api/im/groups/{groupId}` | 解散群(仅群主) |
| `GET /api/im/groups/{groupId}/members` | 成员列表 |
| `POST /api/im/groups/{groupId}/members` | 邀请成员 |
| `DELETE /api/im/groups/{groupId}/members/{userId}` | 踢出成员(仅管理员/群主) |
| `GET /api/im/users/search?keyword=&limit=` | IM 用户轻量搜索 |
| `GET /api/im/users/batch?ids=` | 批量查用户(头像/昵称) |
| `ws://host:8083/mqtt` | MQTT WebSocket 接入点 |
| topic `im/p2p/{fromId}/to/{toId}` | 收到单聊消息(发送方视角) |
| topic `im/p2p/{userId}/inbox` | 收到单聊消息(接收方视角,核心订阅) |
| topic `im/group/{groupId}/inbox` | 收到群聊消息 |
| topic `im/sys/{userId}/system` | 收到系统推送(通知/告警) |
| topic `im/status/{userId}/state` | 好友在线状态变化(可选订阅) |

### App 端功能

- **会话列表页**(Tab 二级页)
  - 顶部 Tab 切换:**消息**(系统消息) / **会话**(IM 会话)
  - 会话卡:头像、名称(对方昵称/群名)、最后一条消息摘要、未读徽标、时间
  - 长按 → 标记已读 / 删除会话
  - **发起聊天**:右上角 ➕ → 选用户 → 创建/进入单聊
- **单聊窗口**
  - 顶部:对方头像 + 昵称 + 在线状态(订阅 `im/status`)
  - 中部:消息流(自己右对齐 / 对方左对齐,支持文本/表情/图片/文件)
  - 底部:输入框 + 表情 + 图片 + 文件 + 发送
  - 收到消息 → 自动滚到底部 + 振动
  - 进入页面 → 拉历史消息分页 + 调 mark-read
- **群聊窗口**
  - 顶部:群名 + 成员数(点击 → 群详情)
  - 中部:消息流(显示发送者昵称)
  - 群管理入口:见 §群管理
- **群管理**
  - 创建群:弹群成员多选(复用 `UserPicker` 组件,走 `/api/im/users/search`)+ 输入群名
  - 群详情:群成员列表、群主标识(👑)、管理员标识(★)
  - 邀请成员:多选用户 → 调 `POST /api/im/groups/{id}/members`
  - 踢出成员:仅群主/管理员可见
  - 退出群:自己非群主时显示;群主退群 → 必须先转让(1.0 限制:先解散)
  - 修改群名/公告
- **通讯录"发消息"**:见 §6
- **消息推送**:App 在前台时订阅 `im/p2p/{userId}/inbox` + `im/group/{groupId}/inbox` + `im/sys/{userId}/system`;
  后台消息经 `ImPushService` 落入 `sys_user_message`(离线兜底)

### MQTT 客户端实现要点

- **库**:`mqtt@5.x`(注意 ESM 写法: `import mqtt from 'mqtt'`)
- **连接参数**:
  - `url`: `ws://localhost:8083/mqtt`(dev) / `wss://your-domain.com/mqtt`(prod)
  - `username`: **JWT token**(服务端 `MqttAuthInterceptor` 校验,**不要传用户名**)
  - `clientId`: `app-{userId}-{uuid}`(保证唯一,断线重连更换)
  - `clean`: true,`reconnectPeriod`: 3000
- **订阅集合**:登录后立即订阅
  - `im/p2p/{userId}/inbox`(单聊收件)
  - `im/group/{groupId}/inbox`(遍历 `/api/im/groups/my` 后逐个订阅)
  - `im/sys/{userId}/system`(系统消息)
  - `im/status/{userId}/state`(可选,在线状态)
- **断线重连**:断线后重连成功 → 重新订阅 + 调 `mark-all-read` 兜底
- **消息顺序**:同会话严格按 `server_received_at` 升序,客户端不要重排

### 交互细节

- 单聊窗口进入 → 显示骨架屏 + 拉第一页 → 向上滚动触顶加载更早消息
- 发送消息:本地先 optimistic insert(状态 sending) → MQTT 发到 `im/p2p/{userId}/to/{peerId}` →
  服务端 `ImP2pMessageHandler` 落库 + 推 inbox topic → 接收方收到 → 本地状态 → sent
- 失败重试:sending 超时 5s → 标记 failed,长按重发
- 在线状态:仅单聊窗口顶部显示(避免全量广播,服务端 `MqttTopicFilter` 限流)
- 撤回:长按自己消息 → "撤回"(2 分钟内有效,服务端 `ImApiCode.MSG_RECALL_TIMEOUT`)
- 输入中(typing):1.0 不实现,留 v1.1

### 二次开发点

- **图片/文件消息**:1.0 复用 `/api/upload` 上传后发 URL 文本,不做缩略图/进度
- **@提及**:群消息文本内 `@昵称` 解析 + 高亮 + 通知,1.0 仅做文本提示
- **已读回执**:1.0 不展示"对方已读",留 v1.1
- **音视频通话**:见 [extension.md §3.2](./extension.md#3-通讯录拨打im)
- **消息搜索**:全局搜索消息内容(需后端补 ES 索引,见 [extension.md §3.3](./extension.md#3-通讯录拨打im))

---

## 不在 1.0 的功能(明确标记)

| 功能 | 原因 | 见 |
|---|---|---|
| 真正的审批/工作流 | mica-admin 无流程引擎 | [extension.md §4](./extension.md#4-审批工作流) |
| 考勤打卡 | 无 `sys_attendance` 表 | [extension.md §5](./extension.md#5-考勤打卡) |
| 音视频通话 | mica-im 仅文本/图片/文件 | [extension.md §3.2](./extension.md#3-通讯录拨打im) |
| 系统级推送(APNs/华为/小米通道) | App 后台推送需要厂商通道 | [extension.md §6.2](./extension.md#6-实时推送) |

> App 1.0 在 UI 上对这些功能 **预留占位**(灰色 + "即将上线"按钮),让二次开发方有明确目标。
> 注:IM 基础能力(单聊/群聊/会话列表/群管理/系统消息推送) **已在 1.0 范围内**,无需占位。