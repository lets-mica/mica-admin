# App 功能详述

> 每个模块都标注:**后端依赖**、**App 端功能**、**交互细节**、**二次开发点**。

## 模块清单

| # | 模块 | 后端依赖 | 状态 |
|---|---|---|---|
| 1 | 登录/认证 | `/api/auth/*` + `/api/session` | 🟢 可做 |
| 2 | 工作台(首页) | 聚合现有接口(系统未读) | 🟢 可做 |
| 3 | 消息中心(公告 + 系统消息) | `/api/system/user/message/*` + `/api/system/notice/feed` | 🟢 可做 |
| 4 | 应用中心(动态菜单) | `/api/auth/menus` | 🟢 可做 |
| 5 | 我的(个人中心) | `/api/system/users/*` | 🟢 可做 |
| 6 | 通讯录 | `/api/system/users` + `/api/system/dept` | 🟢 可做 |
| 7 | 通知公告 | `/api/system/notice/feed` | 🟢 可做 |
| 8 | 文件中心 | `/api/upload/**`(x-file-storage) | 🟢 可做 |

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
| `GET /api/system/notice/feed?current=1&size=3` | 最新公告 |

### App 端功能

- **顶部问候**:用户头像、姓名、部门、今日日期
- **未读徽标**:右上角铃铛显示 **系统消息未读总数**;Tab 栏"消息"图标也叠加红点
- **待办摘要**:最新 3 条待办类消息(category=business)
- **公告摘要**:最新 3 条系统公告
- **快捷入口宫格**:通讯录、文件、我的(本地预置固定入口)
- **下拉刷新**:刷新所有模块

### 交互细节

- 工作台所有数据并发拉取,显示骨架屏
- 点击公告项 → 跳公告详情
- 点击快捷入口 → 跳对应模块(或应用中心)
- **角色化显示**:App 1.0 工作台入口对所有用户一致

### 二次开发点

- "今日待办"统计可对接二次开发的审批/工单模块(见 [extension.md](./extension.md))
- 自定义卡片:可在工作台 `extension/` 下注册自定义 Widget(见 [extension.md §7](./extension.md#7-自定义工作台卡片))

---

## 3. 消息中心(公告 + 系统消息)

### 后端依赖

| 端点 | 用途 |
|---|---|
| `GET /api/system/notice/feed?current=&size=&title=` | 公告 Feed(已发布) |
| `GET /api/system/notice/{id}` | 公告详情 |
| `GET /api/system/user/message/unread` | 未读消息(顶部红点) |
| `GET /api/system/user/message?page=&size=` | 我的消息分页 |
| `PUT /api/system/user/message/read/{id}` | 标记单条已读 |
| `PUT /api/system/user/message/read-all` | 全部已读 |

### App 端功能

- **顶部 Tab 切换**:**公告** / **系统消息**
- **公告 Tab**:
  - 公告列表:按发布时间倒序,显示标题、类型(通知/公告)、时间
  - 点击 → 跳公告详情(富文本渲染)
- **系统消息 Tab**:
  - Tab 子切换:全部 / 未读(可选)
  - 消息列表:分类徽标(system/business/security/activity)
  - 已读状态:已读灰色,未读粗体 + 红点
  - 批量操作:长按进入多选模式 → 批量已读
  - 搜索:按标题/内容模糊搜索

### 交互细节

- 列表左滑 → 标记已读 / 删除
- 点击消息 → 标记已读 + 弹详情
- 详情页展示消息标题、内容、时间、发送方
- 公告详情用 `uni-app` 的 `rich-text` 组件渲染 HTML

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

### App 端功能

- **部门树浏览**:树形展开/折叠,显示部门人数
- **部门下用户**:点击部门看部门成员
- **用户搜索**:按姓名/工号/手机号/邮箱模糊搜索(走 `/api/system/users`)
- **个人信息卡**:头像、姓名、部门、岗位、手机、邮箱
- **快捷操作**:
  - **打电话**(1.0 仍占位):需二次开发(见 [extension.md §3](./extension.md#3-通讯录拨打))

### 交互细节

- 搜索结果点击 → 跳用户详情
- 用户详情展示所有可见字段 + 操作按钮区
- 部门按层级缩进展示

### 二次开发点

- **打电话**:集成原生拨号或第三方电话 SDK(见 [extension.md §3](./extension.md#3-通讯录拨打))

---

## 7. 通知公告

### 后端依赖

| 端点 | 用途 |
|---|---|
| `GET /api/system/notice/feed?current=&size=&title=` | 公告 Feed(已登录用户可见) |
| `GET /api/system/notice/{id}` | 公告详情 |

### App 端功能

- **公告列表**:按发布时间倒序,显示标题、摘要、类型标签(通知/公告)、时间
- **公告详情**:富文本/HTML 内容渲染(需 HTML 解析库)
- **搜索**(可选):按标题模糊搜索
- **置顶**(可选):后端 `seq` 字段决定排序,App 端识别 `seq=100` 等高优先级置顶

### 交互细节

- 详情页用 `uni-app` 的 `rich-text` 组件渲染 HTML
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

## 不在 1.0 的功能(明确标记)

| 功能 | 原因 | 见 |
|---|---|---|
| 真正的审批/工作流 | mica-admin 无流程引擎 | [extension.md §4](./extension.md#4-审批工作流) |
| 考勤打卡 | 无 `sys_attendance` 表 | [extension.md §5](./extension.md#5-考勤打卡) |
| 视频会议 | mica-admin 不含音视频 SDK | [extension.md §3.2](./extension.md#3-音视频通话) |
| 即时通讯(IM) | mica-admin 定位为通用后台,需对接第三方 SDK | 第三方文档 |
| 系统级推送(APNs/华为/小米通道) | App 后台推送需要厂商通道 | [extension.md §6.2](./extension.md#62-系统级推送) |

> App 1.0 在 UI 上对这些功能 **预留占位**(灰色 + "即将上线"按钮),让二次开发方有明确目标。