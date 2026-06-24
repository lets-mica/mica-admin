<script setup lang="ts">
/**
 * IM 即时通讯主页面（p2p）
 *
 * 布局：
 *   ┌────────────────────────────────────────────┐
 *   │ 左侧：会话列表              │  右侧：消息  │
 *   │ ┌─────────────────────┐    │  气泡 + 输入框│
 *   │ │ [头像] 张三  • 3    │    │               │
 *   │ │   你好，明天...     │    │               │
 *   │ └─────────────────────┘    │               │
 *   └────────────────────────────────────────────┘
 *
 * 数据流向：
 *   onMounted:
 *     1. loadConversations() 拉会话列表（含未读数从 Redis）
 *     2. mqttStore.connect(wsUrl) 连接 broker（异步，不阻塞 UI）
 *   点击某会话:
 *     3. 切换 activeConversationId
 *     4. loadMessages(convId) 拉消息历史（beforeId = null → 最新 20 条）
 *     5. markConversationRead(convId) 清未读数 + 向对端推送"已读回传"
 *   输入框发送:
 *     6. mqttStore.sendTextMessage(peerUserId, content) 上行 + 本地乐观插入气泡
 *     7. 服务端收到后通过下行 topic 回推 → mqttStore.newMessages 追加新消息
 */
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';

import {
  NAvatar,
  NBadge,
  NButton,
  NEmpty,
  NInput,
  NModal,
  NPopconfirm,
  NSpace,
  NTag,
} from 'naive-ui';
import { MessageCircle, Plus } from '@lucide/vue';

import { useAccessStore, useUserStore } from '@vben/stores';

import { notification } from '#/adapter/naive';

import {
  getConversationList,
  getMessageList,
  getOrCreateP2p,
  markAllRead,
  markConversationRead,
  recallMessage,
  type ConversationVO,
  type MessageVO,
} from '#/api/im/conversation';
import { useImMqttStore } from '#/api/im/mqtt';

import UserPicker from './components/UserPicker.vue';

defineOptions({ name: 'ImIndex' });

// ---------- Store ----------
const userStore = useUserStore();
const accessStore = useAccessStore();
const mqttStore = useImMqttStore();

const myUserId = computed(() => userStore.userInfo?.id ?? 0);

// ---------- 会话列表 ----------
const loadingList = ref(false);
const conversations = ref<ConversationVO[]>([]);
const activeConversationId = ref<string | null>(null);

async function loadConversations() {
  loadingList.value = true;
  try {
    const list = await getConversationList();
    conversations.value = list ?? [];
    // 初始化 mqttStore 的 unreadMap（页面内会再从 Redis 增量更新）
    const map: Record<string, number> = {};
    for (const c of list) {
      if (c.unreadCount && c.unreadCount > 0) map[c.id] = c.unreadCount;
    }
    mqttStore.initUnread(map);
  } catch (e: any) {
    notification.error({ content: '加载会话列表失败', description: e?.message });
  } finally {
    loadingList.value = false;
  }
}

// 总未读数（顶部导航红点使用）
const totalUnread = computed<number>(() => {
  const m = (mqttStore.unreadMap || {}) as Record<string, number>;
  return Object.values(m).reduce<number>((s, v) => s + (Number(v) || 0), 0);
});

// ---------- 消息区 ----------
const activeMessages = ref<MessageVO[]>([]);
const loadingMessages = ref(false);
const inputText = ref('');

const activeConversation = computed<ConversationVO | null>(() =>
  conversations.value.find((c) => c.id === activeConversationId.value) ?? null,
);

async function loadMessages(convId: string, beforeId?: number) {
  loadingMessages.value = true;
  try {
    const list = await getMessageList(convId, { beforeId, size: 20 });
    if (beforeId == null) {
      activeMessages.value = list ?? [];
    } else {
      // 向前翻页：老消息插入到开头
      activeMessages.value = [...(list ?? []), ...activeMessages.value];
    }
  } catch (e: any) {
    notification.error({ content: '加载消息失败', description: e?.message });
  } finally {
    loadingMessages.value = false;
  }
}

function selectConversation(conv: ConversationVO) {
  activeConversationId.value = conv.id;
  // 订阅该会话的下行（进入聊天窗口时需要实时看到新消息）
  mqttStore.subscribeConversation(conv.id);
  loadMessages(conv.id);
  // 标记已读（清未读数 + 向对端推送已读回传）
  if ((conv.unreadCount ?? 0) > 0) {
    markConversationRead(conv.id).then(() => {
      mqttStore.clearUnread(conv.id);
      // 更新本地列表中的未读数，让 UI 红点消失
      const target = conversations.value.find((c) => c.id === conv.id);
      if (target) target.unreadCount = 0;
    }).catch(() => {});
  }
}

async function onSendMessage() {
  const conv = activeConversation.value;
  if (!conv) return;
  const text = inputText.value.trim();
  if (!text) return;
  const peerId = conv.peerUserId;
  if (!peerId) {
    notification.error({ content: '无法获取对端用户 id' });
    return;
  }
  if (!mqttStore.connected) {
    notification.warning({ content: 'IM 连接尚未就绪，消息可能未实时送达' });
  }

  // 1. 本地乐观插入
  const temp = mqttStore.sendTextMessage(peerId, text);
  if (temp) {
    activeMessages.value.push(temp);
  }
  inputText.value = '';

  // 2. 滚动到底部（通过 watch scrollRef 处理）
  scrollToBottom();
}

async function onRecallMessage(msg: MessageVO) {
  if (!msg || msg.senderId !== myUserId.value) return;
  try {
    await recallMessage(msg.id);
    const target = activeMessages.value.find((m) => m.id === msg.id);
    if (target) target.status = 2;
  } catch (e: any) {
    notification.error({ content: '撤回失败', description: e?.message });
  }
}

async function onMarkAllRead() {
  try {
    await markAllRead();
    for (const c of conversations.value) c.unreadCount = 0;
    mqttStore.initUnread({});
    notification.success({ content: '已全部标记为已读' });
  } catch (e: any) {
    notification.error({ content: '标记失败', description: e?.message });
  }
}

// ---------- 发起单聊 ----------
const showP2pModal = ref(false);
const p2pTargetId = ref<number | null>(null);
const p2pLoading = ref(false);

function openP2pModal() {
  p2pTargetId.value = null;
  showP2pModal.value = true;
}

async function onStartP2p() {
  if (p2pTargetId.value == null) {
    notification.warning({ content: '请先选择一个聊天对象' });
    return;
  }
  p2pLoading.value = true;
  try {
    const { conversation } = await getOrCreateP2p(p2pTargetId.value);
    showP2pModal.value = false;
    await loadConversations();
    // 选中刚刚创建的会话
    const found = conversations.value.find((c) => c.id === conversation.id);
    if (found) {
      selectConversation(found);
    } else if (conversation) {
      // 兜底：把后端返回的会话插到列表头并选中
      conversations.value.unshift(conversation);
      selectConversation(conversation);
    }
    notification.success({ content: '聊天已建立' });
  } catch (e: any) {
    notification.error({ content: '创建会话失败', description: e?.message });
  } finally {
    p2pLoading.value = false;
  }
}

// ---------- MQTT 新消息实时合并 ----------
// 简单方式：轮询 mqttStore.newMessages，把消息合并到对应会话 / 会话列表
// 更优雅方式可用 watch + deep，但这里用 setInterval 足够简单稳定
let pollTimer: number | null = null;

function startPolling() {
  pollTimer = window.setInterval(() => {
    const newMsgs = mqttStore.newMessages;
    if (newMsgs.length === 0) return;
    // 拷贝并清空 store 中的临时集合（页面是事件消费方，不把状态堆积在 store 中）
    const batch = newMsgs.splice(0, newMsgs.length);
    for (const m of batch) mergeIncoming(m);

    // 事件消息（撤回 / 已读回传）
    const evtBatch = mqttStore.eventMessages.splice(0, mqttStore.eventMessages.length);
    for (const evt of evtBatch) mergeEvent(evt);
  }, 500);
}

function mergeIncoming(m: MessageVO) {
  // 1. 合并到消息列表（如果当前会话就是目标会话）
  if (activeConversationId.value === m.conversationId) {
    // 去重：同一 senderId + content + 时间相近 视为同一消息（避免服务端回推 + 本地乐观插入重复）
    const dup = activeMessages.value.some((existing) => {
      if (existing.id === m.id) return true;
      return (
        existing.senderId === m.senderId &&
        existing.content === m.content &&
        m.id > 0 // 服务端消息 id 为正数时，替换 id < 0 的临时消息
      );
    });
    if (!dup) {
      // 如果存在 id 为负的临时消息（内容一致），替换为正式消息（保留 id/status）
      const tempIdx = activeMessages.value.findIndex(
        (e) => e.id < 0 && e.senderId === m.senderId && e.content === m.content,
      );
      if (tempIdx >= 0) {
        activeMessages.value.splice(tempIdx, 1, m);
      } else {
        activeMessages.value.push(m);
      }
      scrollToBottom();
    }
  }
  // 2. 更新会话列表：把该会话置顶 / 更新最后一条消息预览
  const idx = conversations.value.findIndex((c) => c.id === m.conversationId);
  if (idx >= 0) {
    const conv = conversations.value[idx];
    conv.lastMsgPreview = m.content ?? '';
    conv.lastMsgTime = m.serverReceivedAt ?? new Date().toISOString();
    // 如果消息是"我"发的，不增加未读数；否则 +1（MQTT store 已做增量，这里同步 UI 展示）
    if (m.senderId !== myUserId.value && activeConversationId.value !== m.conversationId) {
      conv.unreadCount = (conv.unreadCount ?? 0) + 1;
    }
    // 置顶（移动到数组第 0 位）
    conversations.value.splice(idx, 1);
    conversations.value.unshift(conv);
  }
}

function mergeEvent(evt: { eventType?: 'read' | 'recall'; conversationId: string; eventMsgId?: number; senderId?: number }) {
  if (evt.eventType === 'recall' && activeConversationId.value === evt.conversationId) {
    const target = activeMessages.value.find((m) => m.id === evt.eventMsgId);
    if (target) target.status = 2;
  }
}

// ---------- 滚动控制 ----------
const scrollRef = ref<HTMLElement | null>(null);

function scrollToBottom() {
  // 下一帧再滚动，确保 DOM 更新完毕
  requestAnimationFrame(() => {
    const el = scrollRef.value;
    if (el) el.scrollTop = el.scrollHeight;
  });
}

// 当前选中会话变化 → 滚动到底部
watch(activeConversationId, () => scrollToBottom());

// ---------- 生命周期 ----------
onMounted(async () => {
  await loadConversations();
  // 自动选择第一个会话
  if (conversations.value.length > 0) {
    selectConversation(conversations.value[0]);
  }

  // 连接 MQTT
  const mqttWsUrl = import.meta.env.VITE_MQTT_WS_URL || 'ws://localhost:8083/mqtt';
  try {
    await mqttStore.connect(mqttWsUrl);
  } catch (e: any) {
    notification.warning({ content: 'IM 连接失败，消息可能无法实时送达', description: e?.message });
  }

  // 订阅所有已知会话的下行（保证会话列表的新消息提示都能工作）
  for (const c of conversations.value) mqttStore.subscribeConversation(c.id);

  startPolling();
});

onBeforeUnmount(() => {
  if (pollTimer != null) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
  // 保留连接（其他页面可能还会用），不强行 disconnect
});

// ---------- 辅助 ----------
function formatTime(s?: string): string {
  if (!s) return '';
  const d = new Date(s);
  if (isNaN(d.getTime())) return s.slice(0, 5);
  const now = new Date();
  const sameDay =
    d.getFullYear() === now.getFullYear() &&
    d.getMonth() === now.getMonth() &&
    d.getDate() === now.getDate();
  const hh = String(d.getHours()).padStart(2, '0');
  const mm = String(d.getMinutes()).padStart(2, '0');
  if (sameDay) return `${hh}:${mm}`;
  return `${d.getMonth() + 1}/${d.getDate()} ${hh}:${mm}`;
}

function displayName(c: ConversationVO): string {
  return c.peerUserName || `用户 #${c.peerUserId ?? ''}`;
}

function avatarText(c: ConversationVO): string {
  const name = displayName(c);
  return name.slice(0, 1).toUpperCase();
}

// 是否是我发送的消息
function isMine(m: MessageVO): boolean {
  return m.senderId === myUserId.value;
}

// 撤回消息/系统消息的展示文案
function messageText(m: MessageVO): string {
  if (m.status === 2) return `${isMine(m) ? '你' : '对方'}撤回了一条消息`;
  return m.content ?? '';
}

// ---------- 顶部导航徽标暴露（可选） ----------
// 供 layout 读取全局红点：
//   const imStore = useImMqttStore(); imStore.totalUnread()
reactive({ totalUnread });
</script>

<template>
  <div>
    <div
      class="flex h-[calc(100vh-80px)] w-full flex-col rounded-2xl bg-white shadow-sm md:flex-row"
    >
    <!-- 左侧：会话列表 -->
    <div
      class="flex w-full shrink-0 flex-col border-r border-gray-100 bg-gray-50 md:w-[320px]"
    >
      <!-- 顶部标题 -->
      <div class="flex items-center justify-between px-4 py-3">
        <div class="flex items-center gap-2">
          <MessageCircle class="h-5 w-5 text-blue-500" />
          <span class="text-base font-semibold text-gray-800">消息</span>
          <NBadge v-if="totalUnread > 0" :value="Math.max(0, Number(totalUnread || 0))" :max="99" class="ml-1" />
        </div>
        <NSpace :size="4">
          <NButton size="small" type="primary" @click="openP2pModal">
            <template #icon><Plus class="h-4 w-4" /></template>
            发起聊天
          </NButton>
          <NButton size="small" quaternary @click="loadConversations" :loading="loadingList">
            刷新
          </NButton>
        </NSpace>
      </div>

      <!-- 快捷操作 -->
      <div class="flex items-center justify-between border-t border-gray-100 px-4 py-2 text-xs text-gray-500">
        <span>共 {{ conversations.length }} 个会话</span>
        <a v-if="totalUnread > 0" class="cursor-pointer text-blue-500 hover:underline" @click="onMarkAllRead">
          全部已读
        </a>
      </div>

      <!-- 列表 -->
      <div class="min-h-0 flex-1 overflow-y-auto">
        <div v-if="loadingList && conversations.length === 0" class="p-6 text-center text-sm text-gray-400">
          加载中...
        </div>
        <div v-else-if="conversations.length === 0" class="p-6 text-center">
          <NEmpty description="暂无会话" :show-icon="true" />
        </div>
        <div v-else>
          <div
            v-for="c in conversations"
            :key="c.id"
            @click="selectConversation(c)"
            class="flex cursor-pointer items-center gap-3 border-b border-gray-100 px-3 py-3 transition-colors hover:bg-white"
            :class="{
              'bg-white': activeConversationId === c.id,
              'hover:bg-white': activeConversationId !== c.id,
            }"
          >
            <!-- 头像 -->
            <div class="relative shrink-0">
              <NAvatar round size="small" :src="c.peerAvatar || undefined">
                {{ avatarText(c) }}
              </NAvatar>
            </div>
            <!-- 文本区 -->
            <div class="min-w-0 flex-1">
              <div class="flex items-center justify-between">
                <span class="truncate text-sm font-medium text-gray-800">{{ displayName(c) }}</span>
                <span class="ml-2 shrink-0 text-xs text-gray-400">{{ formatTime(c.lastMsgTime) }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="truncate text-xs text-gray-500">
                  {{ c.lastMsgPreview || '暂无消息' }}
                </span>
                <NBadge
                  v-if="(c.unreadCount || 0) > 0"
                  :value="Math.max(0, Number(c.unreadCount || 0))"
                  :max="99"
                  class="ml-2 !rounded-full"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 连接状态 -->
      <div class="border-t border-gray-100 px-4 py-2 text-xs">
        <span v-if="mqttStore.connected" class="text-green-600">● 已连接 IM</span>
        <span v-else-if="mqttStore.connecting" class="text-amber-500">○ 连接中...</span>
        <span v-else class="text-gray-400">● 未连接（刷新页面重试）</span>
      </div>
    </div>

    <!-- 右侧：消息 + 输入框 -->
    <div class="flex min-h-0 flex-1 flex-col">
      <!-- 未选中 -->
      <div v-if="!activeConversation" class="flex flex-1 items-center justify-center">
        <NEmpty description="选择左侧会话开始聊天" />
      </div>

      <!-- 已选中 -->
      <template v-else>
        <!-- 会话标题栏 -->
        <div class="flex items-center justify-between border-b border-gray-100 px-5 py-3">
          <div class="flex items-center gap-2">
            <NAvatar round size="small" :src="activeConversation.peerAvatar || undefined">
              {{ avatarText(activeConversation) }}
            </NAvatar>
            <span class="text-sm font-medium text-gray-800">{{ displayName(activeConversation) }}</span>
            <NTag v-if="activeConversation.mute" size="small" type="info" bordered round>
              免打扰
            </NTag>
          </div>
        </div>

        <!-- 消息气泡滚动区 -->
        <div ref="scrollRef" class="min-h-0 flex-1 overflow-y-auto px-5 py-4">
          <div v-if="loadingMessages && activeMessages.length === 0" class="py-6 text-center text-sm text-gray-400">
            加载消息中...
          </div>
          <div v-else-if="activeMessages.length === 0" class="py-12 text-center text-sm text-gray-400">
            还没有消息，发送第一条吧～
          </div>
          <template v-else>
            <div
              v-for="m in activeMessages"
              :key="m.id"
              class="mb-4 flex items-start gap-2"
              :class="isMine(m) ? 'flex-row-reverse' : 'flex-row'"
            >
              <!-- 头像 -->
              <NAvatar v-if="!isMine(m)" round size="small" :src="activeConversation.peerAvatar || undefined">
                {{ avatarText(activeConversation) }}
              </NAvatar>
              <NAvatar v-else round size="small">
                {{ (userStore.userInfo?.nickName || userStore.userInfo?.userName || 'Me').slice(0, 1).toUpperCase() }}
              </NAvatar>

              <!-- 气泡 + 时间 -->
              <div class="flex max-w-[65%] flex-col" :class="isMine(m) ? 'items-end' : 'items-start'">
                <div
                  class="break-words rounded-xl px-3 py-2 text-sm leading-6 shadow-sm"
                  :class="{
                    'bg-blue-500 text-white': isMine(m) && m.status !== 2,
                    'bg-white text-gray-800 ring-1 ring-gray-100': !isMine(m) && m.status !== 2,
                    'bg-gray-100 text-gray-500 italic': m.status === 2,
                  }"
                >
                  {{ messageText(m) }}
                </div>
                <div class="mt-1 flex items-center gap-2 text-[11px] text-gray-400">
                  <span>{{ formatTime(m.serverReceivedAt) }}</span>
                  <span v-if="m.status === 0" class="text-amber-500">发送中</span>
                  <span v-else-if="m.status === 3" class="text-red-500">发送失败</span>
                  <span v-if="isMine(m) && m.id > 0 && !m.status" class="text-green-600">已送达</span>
                  <NPopconfirm
                    v-if="isMine(m) && m.status !== 2 && m.id > 0"
                    positive-text="撤回"
                    negative-text="取消"
                    @positive-click="onRecallMessage(m)"
                  >
                    <a class="cursor-pointer text-gray-400 hover:text-blue-500">撤回</a>
                  </NPopconfirm>
                </div>
              </div>
            </div>
          </template>
        </div>

        <!-- 输入框 -->
        <div class="flex shrink-0 items-end gap-2 border-t border-gray-100 bg-white px-5 py-3">
          <NInput
            v-model:value="inputText"
            class="flex-1"
            placeholder="输入消息，回车发送"
            :disabled="!activeConversation"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 4 }"
            @keydown.enter.prevent.exact="onSendMessage"
          />
          <NButton
            type="primary"
            class="shrink-0"
            :disabled="!inputText.trim() || !activeConversation"
            @click="onSendMessage"
          >
            发送
          </NButton>
        </div>
      </template>
      </div>
    </div>

    <!-- 发起单聊弹窗 -->
    <NModal v-model:show="showP2pModal" preset="card" title="发起聊天" style="width: 520px;">
      <div class="mb-3 text-sm text-gray-500">
        请选择要发起聊天的对象：
      </div>
      <UserPicker v-model="p2pTargetId" :multiple="false" placeholder="搜索用户名或昵称" />
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showP2pModal = false">取消</NButton>
          <NButton type="primary" :loading="p2pLoading" :disabled="p2pTargetId == null" @click="onStartP2p">
            开始聊天
          </NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>
