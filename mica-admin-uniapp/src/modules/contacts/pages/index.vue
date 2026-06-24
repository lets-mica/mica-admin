<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getDepts, getUsers } from '@/api/user'
import type { SysDept, UserVo } from '@/api/user'

const keyword = ref('')
const depts = ref<SysDept[]>([])
const expanded = ref<Set<number>>(new Set())
const membersByDept = ref<Record<number, UserVo[]>>({})
const searchResults = ref<UserVo[]>([])
const loading = ref(false)

const isSearching = computed(() => !!keyword.value.trim())

async function load() {
  loading.value = true
  try {
    depts.value = await getDepts()
    // 默认展开前两级
    depts.value.forEach((d) => expanded.value.add(d.id))
  } finally {
    loading.value = false
  }
}

async function toggleDept(d: SysDept) {
  if (expanded.value.has(d.id)) {
    expanded.value.delete(d.id)
    return
  }
  expanded.value.add(d.id)
  if (!membersByDept.value[d.id]) {
    const p = await getUsers({ current: 1, size: 200, deptId: d.id })
    membersByDept.value[d.id] = p.records || []
  }
}

async function onSearch() {
  if (!keyword.value.trim()) {
    searchResults.value = []
    return
  }
  const p = await getUsers({ current: 1, size: 50, blurry: keyword.value })
  searchResults.value = p.records || []
}

function onUserTap(u: UserVo) {
  uni.navigateTo({ url: `/modules/contacts/pages/user-detail?id=${u.userId}` })
}

onMounted(() => {
  load()
})
</script>

<template>
  <view class="page">
    <view class="search">
      <uni-search-bar v-model="keyword" placeholder="搜索姓名/工号/手机" @confirm="onSearch" @input="onSearch" />
    </view>

    <!-- 搜索结果 -->
    <view v-if="isSearching" class="search-list">
      <view v-if="searchResults.length === 0" class="empty">
        <text>无匹配结果</text>
      </view>
      <view v-for="u in searchResults" :key="u.userId" class="user-row" @tap="onUserTap(u)">
        <image class="avatar" :src="u.avatar || '/static/default-avatar.png'" />
        <view class="info">
          <text class="name">{{ u.nickname }}</text>
          <text class="dept">{{ u.deptName }} · {{ u.postName || '' }}</text>
        </view>
        <text class="arrow">›</text>
      </view>
    </view>

    <!-- 部门树 -->
    <view v-else class="dept-tree">
      <view v-for="d in depts" :key="d.id" class="dept">
        <view class="dept-head" @tap="toggleDept(d)">
          <text class="caret">{{ expanded.has(d.id) ? '▼' : '▶' }}</text>
          <text class="name">{{ d.name }}</text>
          <text v-if="membersByDept[d.id]" class="cnt">({{ membersByDept[d.id].length }})</text>
        </view>
        <view v-if="expanded.has(d.id)" class="members">
          <view v-if="!membersByDept[d.id]" class="loading">加载中…</view>
          <view v-for="u in (membersByDept[d.id] || [])" :key="u.userId" class="user-row" @tap="onUserTap(u)">
            <image class="avatar" :src="u.avatar || '/static/default-avatar.png'" />
            <view class="info">
              <text class="name">{{ u.nickname }}</text>
              <text class="dept">{{ u.postName || '' }}</text>
            </view>
            <text class="arrow">›</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.page {
  padding-bottom: 40rpx;
}
.search {
  background: #fff;
  padding: 16rpx 24rpx;
  position: sticky;
  top: 0;
  z-index: 10;
}
.empty {
  text-align: center;
  color: #8f959e;
  padding: 80rpx 0;
}
.user-row {
  background: #fff;
  padding: 24rpx;
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid #f0f0f0;
  .avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: #e8e8e8;
  }
  .info {
    flex: 1;
    margin-left: 20rpx;
  }
  .name {
    display: block;
    font-size: 28rpx;
  }
  .dept {
    display: block;
    color: #8f959e;
    font-size: 24rpx;
    margin-top: 4rpx;
  }
  .arrow {
    color: #c0c4cc;
  }
}
.dept-tree {
  padding: 0 24rpx;
  .dept {
    margin-bottom: 16rpx;
  }
  .dept-head {
    background: #fff;
    padding: 20rpx;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    .caret {
      color: #8f959e;
      margin-right: 12rpx;
      font-size: 22rpx;
    }
    .name {
      font-size: 28rpx;
      font-weight: 500;
    }
    .cnt {
      color: #8f959e;
      margin-left: 6rpx;
      font-size: 24rpx;
    }
  }
  .members {
    background: #fff;
    border-radius: 12rpx;
    margin-top: 8rpx;
    overflow: hidden;
    .loading {
      padding: 20rpx;
      text-align: center;
      color: #8f959e;
    }
  }
}
</style>