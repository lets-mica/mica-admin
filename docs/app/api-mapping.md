# App 接口对接清单

> 本清单覆盖 App 1.0 全部 11 个模块所使用的后端接口。
> 所有接口**均已存在于 mica-admin-server 当前代码**,不需要后端改造。

## 全局约定

### 基础地址

```
开发环境:http://localhost:8080  (Vite proxy /api → 后端)
生产环境:/api  (与 Web 端共用部署)
```

### 响应格式

```json
{
  "code": 0,           // 0 = 成功(注意 mica-admin 成功 code = 0,非 200)
  "msg": "ok",
  "data": { ... }      // 业务数据
}
```

**App 端 request 拦截器必须配置**:

```typescript
defaultResponseInterceptor({
  successCode: 0,
  codeField: 'code',
  dataField: 'data',
  messageField: 'msg'
})
```

### 鉴权

- 除登录相关接口外,**所有接口必须带 JWT**
- 请求头:`Authorization: Bearer {token}`
- App 端需在 `request` 拦截器统一注入 token
- 401 → 清 token → 跳登录页

### 通用分页入参

```typescript
{
  current: number,   // 当前页,从 1 开始
  size: number       // 每页大小,默认 10
}
```

### 通用分页出参

```typescript
{
  records: T[],
  total: number,
  current: number,
  size: number,
  pages: number
}
```

### 通用模糊查询

```
?blurry=xxx        // 模糊匹配多个字段(具体字段由后端决定)
?createTime[0]=xxx // 时间范围(部分接口)
?createTime[1]=xxx
```

### 文件上传

```
POST /api/upload/upload
Content-Type: multipart/form-data
file: <binary>

→ { id, url, filename, size, contentType, ... }
```

---

## 模块 1:登录

| # | Method | Path | 入参 | 出参 | 权限 |
|---|---|---|---|---|---|
| 1.1 | GET | `/api/auth/public-key` | - | `string` (RSA 公钥 base64) | 放行 |
| 1.2 | GET | `/api/auth/captcha` | - | `{ captchaId, image: base64 }` | 放行 |
| 1.3 | POST | `/api/session` | form: `username`, `password`(RSA 加密), `captchaId`, `captchaCode` | `{ token, ... }` | 放行 |
| 1.4 | GET | `/api/logout` | - | - | 已登录 |
| 1.5 | GET | `/api/auth/info` | - | `{ userInfo: JwtUser, publicKey: string }` | 已登录 |

### 关键实体

```typescript
// JwtUser
interface JwtUser {
  userId: number
  username: string
  avatar?: string
  email?: string
  phone?: string
  nickname?: string
  isAdmin: boolean
  roleList: Array<{ id: number, code: string, name: string }>
  deptId?: number
  deptName?: string
  // ...
}
```

### 登录完整流程(前端代码)

```typescript
// 1. 取公钥
const pubKey = await getPublicKey()
// 2. 取验证码
const { captchaId, image } = await getCaptcha()
// 3. RSA 加密密码
const encryptedPwd = encryptRSA(password, pubKey)
// 4. 提交登录
const { token } = await login({
  username,
  password: encryptedPwd,
  captchaId,
  captchaCode
})
// 5. 存 token + 拉用户信息
storage.setToken(token)
const { userInfo, publicKey } = await getInfo()
storage.setUser(userInfo)
```

---

## 模块 2:工作台(首页)

工作台为前端聚合,本身不调用单一接口,而是并发拉取多个接口。

| # | Method | Path | 用途 | 权限 |
|---|---|---|---|---|
| 2.1 | GET | `/api/auth/info` | 当前用户 | 已登录 |
| 2.2 | GET | `/api/system/user/message/unread` | 未读消息数 | 已登录 |
| 2.3 | GET | `/api/system/notice?current=1&size=3` | 最新 3 条公告 | 已登录 |
| 2.4 | GET | `/api/auth/menus` | 应用中心数据(快捷入口) | 已登录 |

### 工作台前端聚合示例

```typescript
const [info, unread, notices, menus] = await Promise.all([
  getInfo(),
  getUnreadMessages(),
  getNotices({ current: 1, size: 3 }),
  getMenus()
])
```

---

## 模块 3:消息中心

| # | Method | Path | 入参 | 出参 | 权限 |
|---|---|---|---|---|---|
| 3.1 | GET | `/api/system/user/message/unread` | - | `UserMessageVo[]` | 已登录 |
| 3.2 | GET | `/api/system/user/message` | `current`, `size`, `blurry?`, `createTime[]?` | `Page<UserMessageVo>` | 已登录 |
| 3.3 | PUT | `/api/system/user/message/read/{id}` | - | - | 已登录 |
| 3.4 | PUT | `/api/system/user/message/read-all` | - | - | 已登录 |

### 关键实体

```typescript
// UserMessageVo
interface UserMessageVo {
  id: number                  // sys_user_message.id
  messageId: number           // 关联 sys_message.id
  title: string               // 消息标题
  content: string             // 消息内容
  category: 'system' | 'business' | 'security' | 'activity'
  readFlag: '0' | '1'         // 0=未读 1=已读
  createdAt: string           // ISO 8601
  // ...
}
```

> **二次开发点**:当前 `UserMessageVo` 无 `biz_type/biz_id/url`,无法跳具体业务。
> 详见 [extension.md §1](./extension.md#1-消息跳转业务单据)。

---

## 模块 4:应用中心(动态菜单)

| # | Method | Path | 入参 | 出参 | 权限 |
|---|---|---|---|---|---|
| 4.1 | GET | `/api/auth/menus` | - | `MenuVo[]` | 已登录 |

### 关键实体

```typescript
// MenuVo
interface MenuVo {
  id: number
  parentId: number
  title: string
  icon?: string          // icon 名称(需前端映射)
  path?: string          // 路由路径
  component?: string     // 前端组件
  type: 'MENU' | 'BUTTON' | 'DIR'
  permission?: string
  children?: MenuVo[]
}
```

### 二次开发点

菜单组件为 mica-admin-web 的 Vben 组件,**无法直接搬移到 uniapp**。
App 1.0 策略:

- **业务菜单**(用户/角色/部门等) → 点击打开 WebView,加载 mica-admin Web 对应路径
- **App 原生菜单**(消息/我的) → 内置,不走后端菜单

详见 [extension.md §2](./extension.md#2-原生应用中心)。

---

## 模块 5:我的(个人中心)

| # | Method | Path | 入参 | 出参 | 权限 |
|---|---|---|---|---|---|
| 5.1 | GET | `/api/auth/info` | - | `{ userInfo, publicKey }` | 已登录 |
| 5.2 | PUT | `/api/system/users/center` | `UserProfileForm` | - | 已登录 |
| 5.3 | POST | `/api/system/users/updatePass` | `UserPwdForm` | - | 已登录 |
| 5.4 | POST | `/api/system/users/avatar` | multipart: `file` | `{ url, ... }` | 已登录 |
| 5.5 | POST | `/api/system/users/updateEmail` | `EmailUpdateVo` | - | 已登录 |
| 5.6 | POST | `/api/system/code/resetEmail` | `EmailCodeVo` | - | 已登录 |
| 5.7 | GET | `/api/logout` | - | - | 已登录 |

### 关键入参

```typescript
// UserProfileForm - 个人资料
interface UserProfileForm {
  id: number
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  // 注意:具体字段参考后端 UserProfileForm.java
}

// UserPwdForm - 修改密码
interface UserPwdForm {
  oldPassword: string      // RSA 加密
  newPassword: string      // RSA 加密
}

// EmailUpdateVo - 修改邮箱
interface EmailUpdateVo {
  email: string
  code: string             // 邮件验证码
  password: string         // 当前密码(RSA 加密)
}

// EmailCodeVo - 发送邮件验证码
interface EmailCodeVo {
  email: string
}
```

---

## 模块 6:通讯录

| # | Method | Path | 入参 | 出参 | 权限 |
|---|---|---|---|---|---|
| 6.1 | GET | `/api/system/dept` | - | `SysDept[]` (树形) | 已登录 |
| 6.2 | GET | `/api/system/users` | `current`, `size`, `blurry?`, `deptId?` | `Page<UserVo>` | 已登录 |

### 关键实体

```typescript
// SysDept - 部门
interface SysDept {
  id: number
  parentId: number
  name: string
  sort: number
  children?: SysDept[]
}

// UserVo - 用户(通讯录用)
interface UserVo {
  userId: number
  username: string
  nickname: string
  avatar?: string
  email?: string
  phone?: string
  deptId?: number
  deptName?: string
  postName?: string
  // ...
}
```

### 通讯录搜索示例

```typescript
// 搜索 "zhang"
const { records } = await getUsers({
  current: 1,
  size: 20,
  blurry: 'zhang'   // 模糊匹配 username/nickname/email/phone
})
```

---

## 模块 7:通知公告

| # | Method | Path | 入参 | 出参 | 权限 |
|---|---|---|---|---|---|
| 7.1 | GET | `/api/system/notice` | `current`, `size` | `Page<SysNotice>` | `system:notice:list` |
| 7.2 | GET | `/api/system/notice/{id}` | - | `SysNotice` | `system:notice:query` |

### 关键实体

```typescript
// SysNotice - 通知公告
interface SysNotice {
  id: number
  title: string
  content: string        // HTML 格式,需 rich-text 渲染
  type?: string          // 类型字典
  seq?: number           // 排序,值越大越靠前(置顶)
  enabled: boolean       // 是否启用
  remark?: string
  createdBy: string
  createdAt: string
}
```

> **权限提示**:App 1.0 普通用户**无 `system:notice:list` 权限**,
> 只能通过工作台聚合接口(参见 §2)展示前 3 条。如需全列表,需后端
> 调整 `/api/system/notice` 的 `@PreAuthorize`,改为 `@sec.isAuthenticated()`。
>
> 或二次开发新增 `GET /api/app/notice` 接口供 App 专用。

---

## 模块 8:文件中心

| # | Method | Path | 入参 | 出参 | 权限 |
|---|---|---|---|---|---|
| 8.1 | POST | `/api/upload/upload` | multipart: `file` | `{ id, url, filename, size, ... }` | 已登录 |
| 8.2 | GET | `/api/upload/{platform}/{filename}` | - | binary | 已登录 |
| 8.3 | DELETE | `/api/upload/{id}` | - | - | 已登录 |

> 文件存储由 dromara **x-file-storage** 提供,后端 Controller 在 `SysFileStorageController`。

### 上传示例

```typescript
const uploadTask = uni.uploadFile({
  url: '/api/upload/upload',
  filePath: tempFilePath,
  name: 'file',
  header: { Authorization: `Bearer ${token}` },
  success: (res) => {
    const { data } = JSON.parse(res.data)
    // data.url = 可访问的直链
  }
})

uploadTask.onProgressUpdate((res) => {
  console.log(`上传进度:${res.progress}%`)
})
```

---

## 模块 9:Token 管理(管理员)

| # | Method | Path | 入参 | 出参 | 权限 |
|---|---|---|---|---|---|
| 9.1 | GET | `/api/auth/token` | `current`, `size`, `filter?` | `Page<TokenVo>` | 已登录 |
| 9.2 | DELETE | `/api/auth/token` | body: `string[]` (token keys) | - | `@sec.isAdmin()` |

### 关键实体

```typescript
// TokenVo - 认证 token
interface TokenVo {
  key: string              // token 唯一标识(用于踢出)
  username: string
  ip: string
  browser?: string
  os?: string
  loginTime: string
  lastActiveTime: string
  status: 'online' | 'offline'
}
```

---

## 模块 10:监控(简化版)

| # | Method | Path | 出参 | 权限 |
|---|---|---|---|---|
| 10.1 | GET | `/api/system/monitor/server` | `Map<String, Object>` | `system:monitor:servers` |
| 10.2 | GET | `/api/system/monitor/sql` | `Map[]` | `system:monitor:sql` |
| 10.3 | GET | `/api/system/monitor/redis` | `Map<String, Object>` | `system:monitor:redis` |

### 服务监控返回结构(简化示例)

```typescript
{
  cpu: { usage: 35, cores: 8 },
  memory: { total: 16777216, used: 10485760, free: 6291456 },
  jvm: { heapUsed: 536870912, heapMax: 1073741824, uptime: 1312000 },
  disk: { total: 500, used: 120 },
  system: { os: 'Linux 5.x', hostname: '...' }
}
```

> App 1.0 仅取 `server`,SQL/Redis 监控因数据量大、阅读体验差,暂不展示。

---

## 模块 11:字典查询

| # | Method | Path | 入参 | 出参 | 权限 |
|---|---|---|---|---|---|
| 11.1 | GET | `/api/system/dict` | `current`, `size` | `Page<SysDict>` | 已登录 |
| 11.2 | GET | `/api/system/dict-info` | `current`, `size`, `type?` | `Page<SysDictInfo>` | 已登录 |

### 关键实体

```typescript
// SysDict - 字典类型
interface SysDict {
  id: number
  type: string         // 字典 type 标识,如 'sys_user_status'
  description: string  // 字典描述
  remark?: string
}

// SysDictInfo - 字典项
interface SysDictInfo {
  id: number
  type: string         // 关联 SysDict.type
  label: string        // 显示值
  value: string        // 存储值
  cssClass?: string
  listClass?: string
  isDefault: boolean
  status: number       // 0=启用 1=停用
}
```

---

## 不在 App 1.0 内的接口(后续迭代参考)

| 接口 | 路径 | 说明 |
|---|---|---|
| 用户管理(管理员) | `/api/system/users/**` | 通讯录已经够用,管理员场景用 Web 端 |
| 角色/菜单/部门/岗位管理 | `/api/system/role/**` etc. | 同上,管理用 Web 端 |
| 日志查询 | `/api/system/log/**` | 同上 |
| 配置管理 | `/api/system/config/**` | 同上 |
| 消息管理(管理员) | `/api/system/message/**` | 同上 |
| Swagger 文档 | `/v3/api-docs`、`/doc.html` | 仅开发用,App 不集成 |

---

## 请求/响应示例(完整登录链路)

### Step 1: GET /api/auth/public-key

**Request**:
```
GET /api/auth/public-key
```

**Response**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA..."
}
```

### Step 2: GET /api/auth/captcha

**Response**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "captchaId": "uuid-xxxx",
    "image": "data:image/png;base64,iVBORw0KGgo..."
  }
}
```

### Step 3: POST /api/session

**Request** (form-urlencoded):
```
username=zhang.san
password=<RSA-encrypted-base64>
captchaId=uuid-xxxx
captchaCode=8
```

**Response**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": "eyJhbGciOiJIUzI1NiJ9...."  // JWT token
}
```

> 注意:实际返回结构依 mica-admin `BaseController` 封装,可能为
> `{ code: 0, msg: 'ok', data: { token: '...' } }` 或直接是 token 字符串。
> 需对照 mica-admin-web `auth-mica-admin.ts` 的解析逻辑保持一致。

### Step 4: GET /api/auth/info

**Request**:
```
GET /api/auth/info
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

**Response**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "userInfo": {
      "userId": 1,
      "username": "admin",
      "nickname": "超级管理员",
      "avatar": "https://...",
      "email": "admin@example.com",
      "phone": "138****8888",
      "isAdmin": true,
      "deptId": 1,
      "deptName": "总公司",
      "roleList": [
        { "id": 1, "code": "admin", "name": "超级管理员" }
      ]
    },
    "publicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8..."
  }
}
```

---

## 跨域 / 代理配置

### 开发环境(Vite)

```typescript
// vite.config.ts
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 生产环境

App 端 baseURL 设为 `/api`,与 Web 端共用 nginx 反代或 jar 部署。

---

## 版本兼容

| mica-admin 版本 | 接口稳定性 | App 兼容策略 |
|---|---|---|
| 当前 main 分支 | ✅ 已验证 | 完全兼容 |
| 后续小版本 | ⚠️ 关注变更 | 后端接口变更需同步更新本文件 |
| 重大重构 | ❌ | App 需配套升级 |