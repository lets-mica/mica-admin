---
name: mica-admin-web
description: Use when adding or modifying pages, API clients, routing, auth or styling in mica-admin-web (Vben Admin 5.x localized + Vue 3 + Naive UI + Vite + Tailwind 4). Covers the CRUD page recipe (api.ts + views/*/index.vue), parsePage pagination, useVbenModal, NDataTable conventions, v-access permission codes, backend-driven dynamic routes, and the vben/ localization pitfalls.
---

# mica-admin-web 前端开发

Vue 3.5 + Naive UI 2.44 + Vite 6 + TS 5.7 + Tailwind 4 + Pinia 3。Vben Admin 5.x 已**源码本地化**到 `vben/`，由 `vite.config.ts` 的 `vbenResolver()` 解析 `@vben/*`、`@vben-core/*` 别名。

## 目录约定

```
src/api/<module>/<res>.ts     # 接口层（system / monitor / oss / core）
src/views/<module>/<res>/index.vue   # 页面
src/store/                    # 业务 Pinia store（单文件直接 export）
src/adapter/                  # UI 适配层（naive discrete api、组件注册、form、vxe-table）
src/utils/                    # format-date、export-excel、rsa
src/locales/langs/{zh-CN,en-US}/
```

路径别名是 `#/` → `src/`（**不是** `@/`）。

完整可复制模板见 `references/crud-page.md`。

## 硬性约定

**响应处理** — `src/api/request.ts` 已配 `defaultResponseInterceptor({ successCode: 0, codeField: 'code', dataField: 'data' })`。后端**成功 code = 0**，成功时返回裸数据体。`api` 是共享实例，业务接口直接 `import { api } from '#/api/request'`。

**分页** — 后端返回 MyBatis-Plus `IPage`（`{ records, total }`），接口层必须用 `parsePage<T>()` 统一成 `{ list, total }`，页面只消费 `{ list, total }`：
```ts
const data = await api.get<any>('/api/system/banner', { params });
return parsePage<BannerItem>(data);
```

**错误处理** — 全局拦截器已处理 401/403/超时并弹 notification，`{ msg }` 已提升为 `Error.message`。页面里 `catch` 只需 `console.error`，**不要**重复弹错误提示。需要自己接管时请求 config 传 `{ skipErrorHandler: true }`。

**Excel 导出** — 用 `exportExcel({ api, url, filename, params })`（`#/utils/export-excel`），对应后端 `@ResponseExcel` 的 `download` 接口。

**弹窗** — 用 `useVbenModal`（`@vben-core/popup-ui`）：`const [Modal, modalApi] = useVbenModal({ onConfirm })`，`modalApi.setState({ title }).open()` / `.close()` / `.lock()` / `.unlock()`。确认框用 `dialog.warning()`（`#/adapter/naive`），**不要**用原生 `confirm()`。

**表格** — `NDataTable` + 独立 `NPagination`。选择用 `:checked-row-keys` + `@update:checked-row-keys`，**不要**用 `:selection` / `@update:selection-keys`。列渲染用 `h()`，状态列用 `NTag`，操作列 `fixed: 'right'`。

**权限** — 按钮用指令 `v-access:code="'system:banner:add'"`；`h()` 里渲染的操作按钮用 `useAccess()` 的 `hasAccessByCodes([...])`。权限码与后端 `sys_menu.perms` 完全一致，动作用 `list/query/add/edit/del/export`（**`:del` 不是 `:remove`**）。

**路由** — `accessMode = 'backend'`，动态路由由 `src/router/access.ts` 的 `generateAccessible()` 依据 `GET /api/auth/menus` + `import.meta.glob('../views/**/*.vue')` 生成。**新增页面不要写静态路由**（`src/router/routes/modules/` 已废弃），改为在后端 `sys_menu` 插入菜单行，`component` 填 `system/banner/index`。

**组件选型** — `NDataTable`/vxe-table、`VbenForm`、`VbenModal`、`NTree`/`VbenTree`、`NSelect`、`NInput`、`NPagination`；启用/禁用统一 `NSwitch`（不用字符串值 select）。图标用 `lucide-*` 名（如 `lucide:image`）。

**样式** — Tailwind 4 + Naive 主题，CSS 变量 `--foreground` / `--primary` / `--accent`。页面根容器惯例 `<div class="p-4">` + `<NCard :bordered="false" content-class="!p-4">`。

**i18n** — 页面提示文案走 `src/locales/langs/{zh-CN,en-US}/`；菜单 title 由后端直接返回字符串。

## 禁区

- **不要**改 `vben/` 内的包对包导入路径（别名由 `vbenResolver()` 接管，改了会破坏解析）。
- **不要**在 `vben/` 内用 Tailwind `@apply`（已由 `scripts/fix-apply.mjs` 批量转纯 CSS）。
- **不要**改 `src/api/core/auth.ts`（Vben 默认实现）；mica-admin 认证定制一律在 `auth-mica-admin.*` 与 `src/store/auth*.ts`。
- 登录链路固定为：`GET /api/auth/public-key` → `GET /api/auth/captcha` → `POST /api/session`（form-urlencoded，密码 RSA 加密）→ `GET /api/auth/info` → `GET /api/auth/menus`；登出 `GET /api/logout`。

## 新增页面 checklist

1. `src/api/<module>/<res>.ts`：类型 + list/add/edit/delete/export（模板见 references）。
2. `src/views/<module>/<res>/index.vue`：搜索区 + 工具栏 + 表格 + 分页 + Modal 表单。
3. 后端 `docs/database/mysql.sql` 追加 `sys_menu` 菜单行 + 按钮权限行，授权角色，重新登录。
4. 补 i18n 文案。
5. `pnpm typecheck && pnpm lint` 必须干净。

## 命令

```bash
pnpm dev          # 端口 5888，/api 代理到 http://localhost:8080（需先起后端）
pnpm typecheck    # vue-tsc --noEmit --skipLibCheck
pnpm lint         # eslint --fix
pnpm build        # typecheck + 生产构建
pnpm build:prod   # 跳过类型检查（Maven prod profile 调用的是这个）
```

**接口类型手写维护**：不再有 swagger 代码生成（`pnpm api` 脚本与 `src/api/Api.ts` 已移除）。新接口在 `src/api/<module>/<res>.ts` 里手写 `interface` + 请求函数，字段以后端实体/VO 为准；需要核对时看后端源码或 `/doc.html`。

`.vue/.ts/.json/.yml` 用 **2 空格**缩进，LF 行尾。
