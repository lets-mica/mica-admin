# mica-admin-uniapp/AGENTS.md

## 代码风格

- **Composition API + `<script setup lang="ts">`**(与 Vue 3 一致)。
- **TypeScript 严格模式**:避免 `any`,所有 props/emits 显式声明。
- **状态管理**:Pinia 优先,只有真正需要跨页面共享的状态才放进 store。
- **响应式**:小颗粒 UI 状态用 `ref`,复杂对象用 `reactive`,派生数据用 `computed`。

## MQTT 客户端

- 单例: `src/modules/im/mqtt-client.ts` 全局只保留一个连接。
- 鉴权: CONNECT 包的 `username` 必须是 JWT,不是用户名。
- 重连: mqtt.js 自动重连,重连成功需调用 `resubscribe()` 重新订阅。
- 消息顺序: 同会话严格按 `serverReceivedAt` 升序,客户端不要重排。

## 跨端注意事项

| 平台 | MQTT 长连接 | 推送通道 |
|---|---|---|
| H5 | ✅ WS 8083 | uniPush(可选) |
| iOS / Android App | ✅ WS 8083(原生插件) | APNs / 厂商通道 |
| 微信小程序 | ❌ 不支持 | 微信模板消息 |

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
5. 进入「消息」Tab 验证 IM(MQTT 需要后端 broker 已启)