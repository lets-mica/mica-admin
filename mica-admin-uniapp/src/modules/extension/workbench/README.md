# 工作台扩展点

App 工作台首页(`pages/index/index.vue`)在 v1.0 故意保持最小可用:
仅展示用户信息 + 时段问候。**业务卡片(待办、审批、看板、销售漏斗 …)
由二次开发方按需挂载**。

## 目录结构

```
src/modules/extension/workbench/
├── registry.ts        # 注册中心 API (registerCard / getRegisteredCards)
├── index.ts           # 入口文件,二次开发方在此 import + registerCard
├── README.md          # 本文档
└── cards/             # (建议) 卡片组件目录
    ├── approval-todo.vue
    └── ...
```

## 二次开发步骤

### 1. 新增卡片组件

在 `cards/` 目录下新建 `my-card.vue`:

```vue
<!-- src/modules/extension/workbench/cards/approval-todo.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'

const list = ref<{ id: number; title: string }[]>([])

onMounted(async () => {
  const res = await fetch('/api/approval/todo', {
    headers: { Authorization: `Bearer ${uni.getStorageSync('token')}` }
  })
  list.value = await res.json()
})

function onMore() {
  uni.navigateTo({ url: '/pages/approval/list' })
}
</script>

<template>
  <view class="card">
    <view class="card-header">
      <text class="title">待我审批 ({{ list.length }})</text>
      <text class="more" @tap="onMore">更多 ›</text>
    </view>
    <view v-for="i in list" :key="i.id" class="row">
      <text>{{ i.title }}</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}
.card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.title { font-size: 30rpx; font-weight: 600; }
.more { color: #8f959e; font-size: 24rpx; }
.row { padding: 16rpx 0; border-bottom: 1rpx solid #f0f0f0; }
</style>
```

### 2. 在入口注册

```ts
// src/modules/extension/workbench/index.ts
import { registerCard } from './registry'
import ApprovalTodo from './cards/approval-todo.vue'

registerCard({
  id: 'approval-todo',   // 唯一,重复会被忽略
  title: '待我审批',      // 兜底展示文案
  order: 100,            // 越大越靠前
  component: ApprovalTodo
})
```

### 3. 验证

启动 App → 工作台 Tab → 看到 "待我审批" 卡片。

## API 参考

### `WorkbenchCard`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | `string` | ✅ | 唯一标识,建议用模块名 |
| `title` | `string` | ✅ | 卡片标题(空状态展示) |
| `order` | `number` | ❌ | 排序权重,默认 0,越大越靠前 |
| `component` | `Component` | ✅ | Vue 组件 |

### `registerCard(card)`

注册一张卡片。重复 `id` 自动忽略并 `console.warn`。

### `getRegisteredCards(): WorkbenchCard[]`

按 `order` 倒序返回已注册卡片(供工作台页面渲染)。

## 设计原则

1. **卡片自包含**:每个卡片组件自己拉数据、自己处理 loading/error,
   工作台页面只负责布局。这让二次开发方可以独立调试。
2. **不修改通用模块**:不要去碰 `pages/index/index.vue` 或 `stores/`;
   一律在 `extension/workbench/` 内新增。
3. **卡片粒度小**:一张卡片只做一个业务(如"待我审批"、"我的 KPI"),
   避免一个卡片塞满所有内容,后续易维护。
4. **可空状态**:卡片内部用 `v-if="list.length"` 控制空展示文案,
   工作台整体不感知。

## 不在 v1.0 范围

- ❌ 卡片拖拽排序(后续迭代)
- ❌ 卡片配置中心(后续迭代)
- ❌ 卡片权限控制(后续迭代)
