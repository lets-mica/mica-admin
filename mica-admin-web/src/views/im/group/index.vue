<script setup lang="ts">
/**
 * IM 群管理页面（PR-1.1.2）
 *
 * 功能：
 *   - 列表：我加入的群（owner / admin / member 角色标识）
 *   - 创建群
 *   - 邀请成员入群（owner / admin）
 *   - 移除成员（owner / admin）
 *   - 主动退群（member）
 *   - 解散群（owner）
 */
import { computed, h, onMounted, ref } from 'vue';

import {
  NAvatar,
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NPopconfirm,
  NSpace,
  NStatistic,
  NTabPane,
  NTabs,
  NTag,
  NText,
  useMessage,
} from 'naive-ui';
import { Plus, Users, MessageCircle, Trash2, UserPlus } from '@lucide/vue';

import type { GroupVO, GroupMemberVO } from '#/api/im/group';
import {
  getMyGroups,
  getGroupMembers,
  createGroup,
  addGroupMembers,
  removeGroupMember,
  quitGroup,
  dismissGroup,
} from '#/api/im/group';
import { useUserStore } from '@vben/stores';

import UserPicker from '../components/UserPicker.vue';

defineOptions({ name: 'ImGroupIndex' });

const userStore = useUserStore();
const myUserId = computed(() => userStore.userInfo?.id ?? 0);
const message = useMessage();

// ---------- 群列表 ----------
const loadingGroups = ref(false);
const groups = ref<GroupVO[]>([]);

async function loadGroups() {
  loadingGroups.value = true;
  try {
    groups.value = await getMyGroups();
  } catch (e: any) {
    message.error('加载群列表失败：' + (e?.message ?? ''));
  } finally {
    loadingGroups.value = false;
  }
}

onMounted(() => loadGroups());

// ---------- 选中群 -> 成员列表 ----------
const selectedGroupId = ref<number | null>(null);
const selectedGroup = computed(() => groups.value.find((g) => g.id === selectedGroupId.value) ?? null);
const loadingMembers = ref(false);
const members = ref<GroupMemberVO[]>([]);

const memberColumns = [
  {
    title: '成员',
    key: 'userId',
    width: 180,
    render(row: GroupMemberVO) {
      const name = row.userNickName || row.userName || `用户 #${row.userId}`;
      const initials = name.slice(0, 1).toUpperCase();
      return h('div', { class: 'flex items-center gap-2' }, [
        h(NAvatar, { round: true, size: 'small', src: row.avatar || undefined }, { default: () => initials }),
        h('span', {}, name),
      ]);
    },
  },
  {
    title: '角色',
    key: 'role',
    width: 80,
    render(row: GroupMemberVO) {
      const type = row.role === 'owner' ? 'error' : row.role === 'admin' ? 'warning' : 'default';
      const label = row.role === 'owner' ? '群主' : row.role === 'admin' ? '管理员' : '成员';
      return h(NTag, { type, size: 'small', round: true }, { default: () => label });
    },
  },
  {
    title: '群内昵称',
    key: 'nickname',
    render(row: GroupMemberVO) {
      return h('span', { class: 'text-gray-500' }, row.nickname || '-');
    },
  },
  {
    title: '加入时间',
    key: 'joinedAt',
    render(row: GroupMemberVO) {
      return row.joinedAt ? row.joinedAt.slice(0, 16) : '-';
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render(row: GroupMemberVO) {
      const isMe = row.userId === myUserId.value;
      const isOwner = selectedGroup.value?.ownerId === myUserId.value;
      const isAdmin = selectedGroup.value?.role === 'admin' || isOwner;
      if (row.role === 'owner') return null;
      if (isMe) {
        return h(NPopconfirm, {
          onPositiveClick: () => onQuitGroup(selectedGroupId.value!),
        }, {
          trigger: () => h(NButton, { size: 'small', quaternary: true, type: 'error' }, { default: () => '退群' }),
          default: () => '确定退出该群？',
        });
      }
      if (isAdmin) {
        return h(NPopconfirm, {
          onPositiveClick: () => onRemoveMember(selectedGroupId.value!, row.userId),
        }, {
          trigger: () => h(NButton, { size: 'small', quaternary: true, type: 'error' }, { default: () => '移出' }),
          default: () => '确定移除该成员？',
        });
      }
      return null;
    },
  },
];

async function onSelectGroup(groupId: number) {
  selectedGroupId.value = groupId;
  loadingMembers.value = true;
  try {
    const res = await getGroupMembers(groupId);
    members.value = res.list ?? [];
  } catch (e: any) {
    message.error('加载成员失败：' + (e?.message ?? ''));
  } finally {
    loadingMembers.value = false;
  }
}

// ---------- 创建群 ----------
const showCreateModal = ref(false);
const createForm = ref({ name: '', avatar: '', type: 'normal', announcement: '', memberIds: [] as number[] });
const createLoading = ref(false);

function resetCreateForm() {
  createForm.value = { name: '', avatar: '', type: 'normal', announcement: '', memberIds: [] };
}

function openCreateModal() {
  resetCreateForm();
  showCreateModal.value = true;
}

async function onCreateGroup() {
  if (!createForm.value.name.trim()) {
    message.warning('请填写群名称');
    return;
  }
  if (!createForm.value.memberIds.length) {
    message.warning('请至少选择 1 个成员');
    return;
  }
  createLoading.value = true;
  try {
    await createGroup(createForm.value);
    message.success('群创建成功');
    showCreateModal.value = false;
    resetCreateForm();
    await loadGroups();
  } catch (e: any) {
    message.error('创建失败：' + (e?.message ?? ''));
  } finally {
    createLoading.value = false;
  }
}

// ---------- 邀请成员 ----------
const showInviteModal = ref(false);
const inviteSelectedIds = ref<number[]>([]);
const inviteLoading = ref(false);

function openInviteModal() {
  inviteSelectedIds.value = [];
  showInviteModal.value = true;
}

const inviteExcludedIds = computed<number[]>(() =>
  members.value.map((m) => m.userId),
);

async function onAddMembers() {
  if (!inviteSelectedIds.value.length) {
    message.warning('请至少选择 1 个用户');
    return;
  }
  inviteLoading.value = true;
  try {
    const res = await addGroupMembers(selectedGroupId.value!, { userIds: inviteSelectedIds.value });
    message.success(`已邀请 ${res.added} 名成员`);
    showInviteModal.value = false;
    inviteSelectedIds.value = [];
    await onSelectGroup(selectedGroupId.value!);
    await loadGroups();
  } catch (e: any) {
    message.error('邀请失败：' + (e?.message ?? ''));
  } finally {
    inviteLoading.value = false;
  }
}

// ---------- 移除成员 ----------
async function onRemoveMember(groupId: number, userId: number) {
  try {
    await removeGroupMember(groupId, userId);
    message.success('已移除成员');
    await onSelectGroup(groupId);
    await loadGroups();
  } catch (e: any) {
    message.error('移除失败：' + (e?.message ?? ''));
  }
}

// ---------- 退群 ----------
async function onQuitGroup(groupId: number) {
  try {
    await quitGroup(groupId);
    message.success('已退出群聊');
    selectedGroupId.value = null;
    members.value = [];
    await loadGroups();
  } catch (e: any) {
    message.error('退群失败：' + (e?.message ?? ''));
  }
}

// ---------- 解散群 ----------
async function onDismissGroup(groupId: number) {
  try {
    await dismissGroup(groupId);
    message.success('群已解散');
    selectedGroupId.value = null;
    members.value = [];
    await loadGroups();
  } catch (e: any) {
    message.error('解散失败：' + (e?.message ?? ''));
  }
}

// ---------- 辅助 ----------
function roleTagType(role?: string): 'error' | 'warning' | 'info' {
  if (role === 'owner') return 'error';
  if (role === 'admin') return 'warning';
  return 'info';
}

function roleTagLabel(role?: string): string {
  if (role === 'owner') return '群主';
  if (role === 'admin') return '管理员';
  return '成员';
}

function canIManage(group: GroupVO): boolean {
  return group.ownerId === myUserId.value || group.role === 'admin';
}

function canIDismiss(group: GroupVO): boolean {
  return group.ownerId === myUserId.value;
}
</script>

<template>
  <div>
    <div class="flex h-[calc(100vh-120px)] gap-4 p-4">
      <!-- 左侧：群列表 -->
      <div class="w-[340px] shrink-0">
        <NCard title="我的群聊" :bordered="true" size="small">
          <template #header-extra>
            <NButton size="small" type="primary" @click="openCreateModal">
              <template #icon><Plus class="h-4 w-4" /></template>
              创建群
            </NButton>
          </template>

        <div v-if="loadingGroups" class="p-4 text-center text-gray-400">加载中...</div>
        <div v-else-if="groups.length === 0" class="p-4 text-center text-gray-400">
          暂未加入任何群聊
        </div>
        <div v-else class="space-y-1">
          <div
            v-for="g in groups"
            :key="g.id"
            @click="onSelectGroup(g.id)"
            class="flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2 transition-colors"
            :class="selectedGroupId === g.id ? 'bg-blue-50' : 'hover:bg-gray-50'"
          >
            <NAvatar round size="small" :src="g.avatar || undefined">
              {{ (g.name || '群').slice(0, 1).toUpperCase() }}
            </NAvatar>
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2">
                <span class="truncate text-sm font-medium">{{ g.name }}</span>
                <NTag :type="roleTagType(g.role)" size="tiny" round>
                  {{ roleTagLabel(g.role) }}
                </NTag>
              </div>
              <div class="flex items-center gap-1 text-xs text-gray-400">
                <Users class="h-3 w-3" />
                {{ g.memberCount }} 人
              </div>
            </div>
          </div>
        </div>
      </NCard>
    </div>

    <!-- 右侧：群详情 + 成员 -->
    <div class="flex-1">
      <NCard v-if="selectedGroup" :title="'群详情：' + selectedGroup.name" :bordered="true" size="small">
        <template #header-extra>
          <NSpace>
            <NButton
              v-if="canIManage(selectedGroup)"
              size="small"
              type="primary"
              @click="openInviteModal"
            >
              <template #icon><UserPlus class="h-4 w-4" /></template>
              邀请成员
            </NButton>
            <NPopconfirm
              v-if="canIDismiss(selectedGroup)"
              @positive-click="onDismissGroup(selectedGroup.id)"
            >
              <template #trigger>
                <NButton size="small" type="error" quaternary>
                  <template #icon><Trash2 class="h-4 w-4" /></template>
                  解散群
                </NButton>
              </template>
              确定解散该群？解散后所有消息记录将被清空！
            </NPopconfirm>
          </NSpace>
        </template>

        <!-- 统计 -->
        <div class="mb-4 grid grid-cols-3 gap-4">
          <NStatistic label="群主 ID" :value="String(selectedGroup.ownerId)" />
          <NStatistic label="成员数" :value="String(selectedGroup.memberCount)" />
          <NStatistic label="最大人数" :value="String(selectedGroup.maxMembers)" />
        </div>

        <NTabs type="line">
          <NTabPane name="members" tab="成员列表">
            <NDataTable
              :columns="memberColumns"
              :data="members"
              :loading="loadingMembers"
              :bordered="false"
              size="small"
              class="mt-2"
            />
          </NTabPane>
          <NTabPane name="info" tab="群信息">
            <div class="mt-3 space-y-3">
              <div><NText depth="3">群名称：</NText><NText>{{ selectedGroup.name }}</NText></div>
              <div><NText depth="3">群类型：</NText><NText>{{ selectedGroup.type === 'department' ? '部门群' : '普通群' }}</NText></div>
              <div v-if="selectedGroup.announcement">
                <NText depth="3">群公告：</NText>
                <NText class="block mt-1 text-gray-600">{{ selectedGroup.announcement }}</NText>
              </div>
            </div>
          </NTabPane>
        </NTabs>
      </NCard>

      <!-- 未选中群 -->
      <NCard v-else :bordered="true" size="small">
        <div class="flex h-64 flex-col items-center justify-center text-gray-400">
          <MessageCircle class="mb-2 h-10 w-10" />
          <span>请从左侧选择一个群聊</span>
        </div>
      </NCard>
    </div>
  </div>

  <!-- 创建群弹窗 -->
  <NModal v-model:show="showCreateModal" preset="card" title="创建群聊" style="width: 600px;">
    <NForm label-placement="left" label-width="80">
      <NFormItem label="群名称" required>
        <NInput v-model:value="createForm.name" placeholder="请输入群名称" />
      </NFormItem>
      <NFormItem label="群类型">
        <NTabs v-model:value="createForm.type" type="line">
          <NTabPane name="normal" tab="普通群" />
          <NTabPane name="department" tab="部门群" />
        </NTabs>
      </NFormItem>
      <NFormItem label="群公告">
        <NInput
          v-model:value="createForm.announcement"
          type="textarea"
          :rows="3"
          placeholder="选填，可填写群公告"
        />
      </NFormItem>
      <NFormItem label="初始成员" required>
        <UserPicker v-model="createForm.memberIds" :multiple="true" placeholder="搜索用户加入群聊" />
      </NFormItem>
    </NForm>
    <template #footer>
      <NSpace justify="end">
        <NButton @click="showCreateModal = false">取消</NButton>
        <NButton type="primary" :loading="createLoading" @click="onCreateGroup">创建</NButton>
      </NSpace>
    </template>
  </NModal>

  <!-- 邀请成员弹窗 -->
  <NModal v-model:show="showInviteModal" preset="card" title="邀请成员入群" style="width: 600px;">
    <NText depth="3" class="mb-3 block">
      选择要加入本群的用户（已在群内的成员会自动排除）：
    </NText>
    <UserPicker
      v-model="inviteSelectedIds"
      :multiple="true"
      :exclude-ids="inviteExcludedIds"
      placeholder="搜索用户名或昵称"
    />
    <template #footer>
      <NSpace justify="end">
        <NButton @click="showInviteModal = false">取消</NButton>
        <NButton type="primary" :loading="inviteLoading" :disabled="!inviteSelectedIds.length" @click="onAddMembers">邀请</NButton>
      </NSpace>
    </template>
  </NModal>
  </div>
</template>
