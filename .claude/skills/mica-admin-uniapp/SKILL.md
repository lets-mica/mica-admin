---
name: mica-admin-uniapp
description: Use when adding or modifying pages, modules, API clients or stores in mica-admin-uniapp (uniapp + Vue 3 + Vite + Pinia, H5 / App / 微信小程序). Covers the module directory convention, pages.json registration, the uni.request http wrapper and mica-admin response contract, rpx/scss styling, workbench card registry for secondary development, and cross-platform pitfalls.
---

# mica-admin-uniapp 移动端开发

uniapp（`@dcloudio` 3.0）+ Vue 3.5 + Vite 6 + Pinia + TS。后端同 mica-admin-server，认证同为 RSA 加密密码 + 算术验证码 + JWT。

## 目录约定

```
src/
├── pages/                  # tabBar 页面，只有 3 个：index / message / profile
├── modules/<name>/         # 业务模块，按功能拆
│   ├── pages/*.vue
│   ├── stores/
│   └── api.ts
├── modules/extension/      # 二次开发专区（只新增不修改）
│   └── workbench/registry.ts   # 工作台卡片注册中心
├── api/                    # 通用接口层（auth / user / notice / message）
├── stores/auth.ts          # 认证 store
├── utils/                  # request / rsa / storage / format / uuid
├── config/env.ts           # 类型化环境变量 + isH5 / isMp / isApp
├── locales/langs/
└── pages.json              # 所有页面必须在此注册
static/                     # 静态资源放这里，不要放 src/assets/
```

- 别名 `@/` → `src/`（与 web 端的 `#/` 不同）。
- tabBar 页面放 `pages/`，其余页面一律放 `modules/<name>/pages/`。
- **新增页面必须同时在 `src/pages.json` 的 `pages` 数组注册**，否则跳转直接失败（小程序尤其）。

完整模板见 `references/module-page.md`。

## 网络层

统一用 `@/utils/request` 的 `http`，**不要**直接调 `uni.request`：

```ts
import { http } from '@/utils/request'
import type { PageResult } from '@/utils/request'

export function getBanners(params: { current?: number; size?: number; title?: string }) {
  return http.get<PageResult<BannerVo>>('/api/system/banner', params)
}
```

`http.get(url, params, options)` / `post|put|delete(url, data, options)`。约定：

- 成功返回**裸数据体**：分页是 MyBatis-Plus `IPage` 结构 `{ records, total, current, size, pages }`（注意：**移动端直接消费 `records`**，不像 web 端有 `parsePage` 转 `list`）。
- 失败是 R 包装 `{ code: 非0, msg }`；拦截层已自动 `uni.showToast` 并 reject，业务侧 `catch` 不要重复 toast。
- 401 已自动清 token 并 `uni.reLaunch('/modules/auth/pages/login')`。
- 需要静默失败传 `{ hideError: true }`；免鉴权接口传 `{ hideAuth: true }`。
- token 存储 key 为 `mica-admin-token`，走 `@/utils/storage`；用 `getToken/setToken/clearToken`，不要自己读缓存。

## 页面写法

- 一律 `<script setup lang="ts">` + Composition API，TS 严格模式，避免 `any`，props/emits 显式声明。
- 小颗粒状态 `ref`，复杂对象 `reactive`，派生值 `computed`。跨页面共享才进 Pinia store。
- 数据加载写成 `async function load()` + `loading` 标志 + `try/finally`，`onMounted` 调用。
- 下拉刷新：`pages.json` 里该页 `style.enablePullDownRefresh: true`，页面用 `onPullDownRefresh()`（`@dcloudio/uni-app` 导出）配 `uni.stopPullDownRefresh()`。
- 跳转 `uni.navigateTo({ url: '/modules/xxx/pages/detail?id=1' })`，参数在 `onLoad(query)` 里取；登录态切换用 `uni.reLaunch`；tabBar 用 `uni.switchTab`。
- 提示用 `uni.showToast({ icon: 'none' })`，确认用 `uni.showModal`。

## 样式

- SCSS + `scoped`，尺寸单位用 **rpx**（字号 22~30rpx，间距 16/24rpx，圆角 16rpx）。
- 惯用色：主色 `#18A37E`，次要文字 `#8f959e`，正文 `#555`，卡片白底 + `border-radius: 16rpx`。
- 布局用 `view` / `text`（不要用 `div` / `span`），列表项用 `view` + `@tap`（不是 `@click`）。
- 组件走 easycom：`uni-*` 自动解析到 `@dcloudio/uni-ui`，无需手动 import。

## 二次开发（工作台卡片）

不要改通用模块（`auth` / `profile` / `notice`）。扩展功能放 `src/modules/extension/<模块名>/`，并把首页卡片注册到 registry：

```ts
// src/modules/extension/workbench/index.ts
import { registerCard } from './registry'
import ApprovalTodo from './cards/approval-todo.vue'

registerCard({ id: 'approval-todo', title: '待我审批', order: 100, component: ApprovalTodo })
```

卡片组件自行拉数据；`order` 越大越靠前；同 id 重复注册会被忽略。

## 跨端注意

- 平台判断用 `config/env.ts` 的 `isH5` / `isMp` / `isApp`，或条件编译 `#ifdef MP-WEIXIN`。
- 小程序无 `window` / `document`；不要在通用代码里用 DOM API（web 端的 `export-excel` 这类实现不可复用）。
- `import.meta.env.VITE_*` 通过 `config/env.ts` 统一读取，不要散落各处。

## 命令

```bash
pnpm dev:h5           # H5 dev server，默认端口 5889（需先起后端）
pnpm dev:mp-weixin    # 微信小程序，产物用微信开发者工具打开 dist/dev/mp-weixin
pnpm typecheck        # vue-tsc --noEmit
pnpm lint             # eslint --fix
pnpm build:h5 / build:app / build:mp-weixin
```

## 自测流程

1. 起 `mica-admin-server`。
2. `pnpm dev:h5` → `http://localhost:5889`。
3. 登录（RSA + 算术验证码）。
4. 进「消息」Tab 验证「公告 / 系统消息」两个子 Tab。
5. 新增页面额外验证：`pages.json` 已注册、返回/下拉刷新正常、`pnpm typecheck` 干净。
