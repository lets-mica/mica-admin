# mica-admin-uniapp/AGENTS.md

## 代码风格

- **Composition API + `<script setup lang="ts">`**(与 Vue 3 一致)。
- **TypeScript 严格模式**:避免 `any`,所有 props/emits 显式声明。
- **状态管理**:Pinia 优先,只有真正需要跨页面共享的状态才放进 store。
- **响应式**:小颗粒 UI 状态用 `ref`,复杂对象用 `reactive`,派生数据用 `computed`。

## 路径约定

- `@/` → `src/`
- 静态资源: `static/`(由 uniapp 原生支持,**不要**放 `src/assets/`)

## 构建命令

```bash
pnpm install
pnpm dev:h5           # H5 开发服务器
pnpm typecheck        # vue-tsc --noEmit
pnpm build:h5         # H5 生产构建
pnpm build:app        # App 构建(需 HBuilderX 真机/发行)
pnpm build:mp-weixin  # 微信小程序构建
```

## 测试

1. 启动 mica-admin-server(后端)
2. `pnpm dev:h5`
3. 浏览器打开 `http://localhost:5889`
4. 测试登录(RSA + 算术验证码)
5. 进入「消息」Tab 验证「公告/系统消息」两个子 Tab