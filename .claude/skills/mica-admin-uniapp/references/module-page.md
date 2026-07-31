# 模块 + 页面模板

以「轮播图」为例新增 `src/modules/banner/`。参照实现：`src/modules/notice/pages/list.vue`、`src/api/notice.ts`。

## 1. 接口层 `src/modules/banner/api.ts`

```ts
/**
 * 轮播图 API
 */
import { http } from '@/utils/request'
import type { PageResult } from '@/utils/request'

export interface BannerVo {
  id: number
  title: string
  imageUrl?: string
  seq?: number
  enabled?: number
  remark?: string
  createdAt: string
}

export function getBanners(params: { current?: number; size?: number; title?: string }) {
  return http.get<PageResult<BannerVo>>('/api/system/banner', params)
}

export function getBannerDetail(id: number) {
  return http.get<BannerVo>(`/api/system/banner/${id}`)
}
```

通用领域（auth / user / notice / message）的接口放 `src/api/<name>.ts`；模块专属接口放 `src/modules/<name>/api.ts`。

## 2. 列表页 `src/modules/banner/pages/list.vue`

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getBanners } from '../api'
import type { BannerVo } from '../api'
import { formatDateTime } from '@/utils/format'

const list = ref<BannerVo[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const page = await getBanners({ current: 1, size: 50 })
    list.value = page.records || []
  } finally {
    loading.value = false
  }
}

function open(item: BannerVo) {
  uni.navigateTo({ url: `/modules/banner/pages/detail?id=${item.id}` })
}

onPullDownRefresh(async () => {
  await load()
  uni.stopPullDownRefresh()
})

onMounted(() => {
  load()
})
</script>

<template>
  <view class="page">
    <view v-if="list.length === 0 && !loading" class="empty">
      <text>暂无数据</text>
    </view>
    <view v-for="item in list" :key="item.id" class="row" @tap="open(item)">
      <view class="row-head">
        <text class="title text-ellipsis">{{ item.title }}</text>
        <text class="time">{{ formatDateTime(item.createdAt, 'YYYY-MM-DD') }}</text>
      </view>
      <text class="content text-ellipsis">{{ item.remark }}</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding: 24rpx;
}
.empty {
  padding: 80rpx 0;
  color: #8f959e;
  text-align: center;
}
.row {
  padding: 24rpx;
  margin-bottom: 16rpx;
  background: #fff;
  border-radius: 16rpx;
  .row-head {
    display: flex;
    justify-content: space-between;
    .title {
      flex: 1;
      font-size: 30rpx;
      font-weight: 500;
    }
    .time {
      margin-left: 20rpx;
      font-size: 22rpx;
      color: #8f959e;
    }
  }
  .content {
    display: block;
    margin-top: 8rpx;
    font-size: 26rpx;
    color: #555;
  }
}
</style>
```

## 3. 详情页取参 `src/modules/banner/pages/detail.vue`

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getBannerDetail } from '../api'
import type { BannerVo } from '../api'

const detail = ref<BannerVo | null>(null)

onLoad(async (query) => {
  const id = Number((query as { id?: string })?.id)
  if (!id) return
  detail.value = await getBannerDetail(id)
})
</script>
```

## 4. 注册路由 `src/pages.json`

在 `pages` 数组追加（顺序即栈内顺序，第一项是首页，不要插到最前）：

```json
{
  "path": "modules/banner/pages/list",
  "style": {
    "navigationBarTitleText": "轮播图",
    "enablePullDownRefresh": true
  }
},
{
  "path": "modules/banner/pages/detail",
  "style": {
    "navigationBarTitleText": "轮播图详情"
  }
}
```

`path` 不带前导 `/`，但 `uni.navigateTo` 的 url 要带（`/modules/banner/pages/list`）。

## 5. 模块 store（仅跨页共享才建）`src/modules/banner/stores/index.ts`

```ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getBanners } from '../api'
import type { BannerVo } from '../api'

export const useBannerStore = defineStore('banner', () => {
  const list = ref<BannerVo[]>([])

  async function refresh() {
    const page = await getBanners({ current: 1, size: 50 })
    list.value = page.records || []
  }

  return { list, refresh }
})
```

持久化用 `pinia-plugin-persistedstate`（参考 `src/stores/auth.ts` 的写法），namespace 取 `env.namespace`。

## 6. 工作台入口（可选）

想在首页露出入口，注册工作台卡片而不是改 `pages/index/index.vue`：

```ts
// src/modules/extension/workbench/index.ts
import { registerCard } from './registry'
import BannerCard from '@/modules/banner/cards/banner-card.vue'

registerCard({ id: 'banner', title: '轮播图', order: 50, component: BannerCard })
```
