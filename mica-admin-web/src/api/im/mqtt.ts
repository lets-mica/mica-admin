/**
 * IM MQTT 客户端（基于 mqtt.js + Pinia）
 *
 * 连接参数：
 *   url: ws://host:8083/mqtt
 *   username: JWT token
 *   clientId: user-{userId}-{randomSuffix}
 *
 * Topic 约定（与后端 ImMqttConfig 一致）：
 *   上行：im/p2p/{senderId}/{receiverId}        （客户端发布）
 *   下行：im/p2p/{conversationId}/out            （客户端订阅，conversationId = min_max）
 *
 * 下行消息 payload（JSON）：
 *   {
 *     msgId: "uuid",           // 消息唯一 id
 *     msgType: "p2p",          // 消息类型
 *     conversationId: "a_b",   // 会话 id
 *     senderId: 123,           // 发送方 userId
 *     content: "xxx",          // 正文
 *     extra: "...",            // 扩展 JSON（可选）
 *     serverTime: "2025-..."   // 服务端时间
 *     eventType?: "read"|"recall",   // 事件型消息（非内容消息）
 *     eventMsgId?: 12345       // 事件关联消息 id
 *   }
 */
import { defineStore } from 'pinia';

import type { MqttClient, IClientOptions } from 'mqtt';

import { useAccessStore, useUserStore } from '@vben/stores';

import { ref } from 'vue';

import type { MessageVO } from './conversation';

// 下行消息类型（从 broker 收到）
export interface ImPushPayload {
  msgId?: string;
  msgType?: 'p2p' | 'group';
  conversationId: string;
  senderId?: number;
  receiverId?: number;
  content?: string;
  extra?: string;
  serverTime?: string;
  eventType?: 'read' | 'recall';
  eventMsgId?: number;
}

// 未读数更新：收到新消息时自动 +1，mark-read 清零
interface UnreadMap {
  [conversationId: string]: number;
}

/** 拼接 conversationId（p2p）：按 userId 升序拼接 */
function conversationId(a: number, b: number): string {
  const [x, y] = a < b ? [a, b] : [b, a];
  return `${x}_${y}`;
}

function p2pOutTopic(convId: string): string {
  return `im/p2p/${convId}/out`;
}

function p2pSendTopic(senderId: number, receiverId: number): string {
  return `im/p2p/${senderId}/${receiverId}`;
}

// ---------- Store ----------

export const useImMqttStore = defineStore('im-mqtt', () => {
  const connected = ref(false);
  const connecting = ref(false);
  const error = ref<string | null>(null);

  // 未读数（Redis 拉取后初始化，新消息 +1，mark-read 清零）
  const unreadMap = ref<UnreadMap>({});

  // 新消息到达时触发的事件（供页面订阅更新 UI）
  const newMessages: MessageVO[] = [];
  const eventMessages: ImPushPayload[] = [];

  let client: MqttClient | null = null;
  // 记录已订阅的 topic set（避免重复 subscribe）
  const subscribedTopics = new Set<string>();

  function me(): { id: number; token: string } {
    const userStore = useUserStore();
    const accessStore = useAccessStore();
    return {
      id: userStore.userInfo?.id ?? 0,
      token: accessStore.accessToken ?? '',
    };
  }

  /**
   * 连接 MQTT broker。同一用户 + 同一页面仅需连接一次。
   * @param wsUrl WebSocket 连接地址，如 `ws://localhost:8083/mqtt`
   */
  function connect(wsUrl: string): Promise<void> {
    const { id, token } = me();
    if (!id || !token) {
      error.value = '尚未登录，无法连接 IM';
      return Promise.reject(new Error(error.value));
    }
    if (client || connecting.value) return Promise.resolve();

    connecting.value = true;
    error.value = null;

    console.log('[IM] Connecting to MQTT:', wsUrl, 'userId:', id);

        // 动态 import，避免 SSR / 首屏时加载非必需依赖
        return import('mqtt')
          .then((mqtt) => {
            const options: IClientOptions = {
              clientId: `web-${id}-${Math.random().toString(36).slice(2, 10)}`,
              username: token, // 服务端 MqttAuthInterceptor 校验 JWT
              password: '',
              clean: true,
              reconnectPeriod: 5000,
              connectTimeout: 10_000,
              keepalive: 60,
            };
            client = mqtt.default.connect(wsUrl, options);

        client.on('connect', () => {
          console.log('[IM] MQTT connected');
          connected.value = true;
          connecting.value = false;
          // 连接成功后，把已经打开过的会话 topic 重新订阅
          for (const topic of subscribedTopics) {
            client?.subscribe(topic, { qos: 1 });
          }
        });

        client.on('reconnect', () => {
          console.log('[IM] MQTT reconnecting...');
          connecting.value = true;
        });

        client.on('close', () => {
          console.log('[IM] MQTT connection closed');
          connected.value = false;
          connecting.value = false;
        });

        client.on('error', (err) => {
          error.value = err?.message ?? String(err);
          connecting.value = false;
          console.error('[IM] MQTT error:', err);
        });

        client.on('message', (topic: string, payload: Buffer) => {
          try {
            const msg: ImPushPayload = JSON.parse(payload.toString('utf-8'));
            handleIncoming(topic, msg);
          } catch (e) {
            console.warn('[IM] MQTT payload parse error:', e);
          }
        });

        return Promise.resolve();
      })
      .catch((err) => {
        connecting.value = false;
        error.value = err?.message ?? String(err);
        console.error('[IM] MQTT connect failed:', err);
        return Promise.reject(err);
      });
  }

  function disconnect(): void {
    if (client) {
      try {
        client.end(true);
      } catch {
        /* ignore */
      }
      client = null;
    }
    connected.value = false;
    connecting.value = false;
    subscribedTopics.clear();
  }

  /**
   * 订阅某个会话的下行消息（进入会话详情时调用；
   * 会话列表也可通过订阅来实时更新未读数）。
   */
  function subscribeConversation(convId: string): void {
    if (!client) return;
    const topic = p2pOutTopic(convId);
    if (subscribedTopics.has(topic)) return;
    subscribedTopics.add(topic);
    if (connected.value) {
      client.subscribe(topic, { qos: 1 });
    }
  }

  function unsubscribeConversation(convId: string): void {
    const topic = p2pOutTopic(convId);
    if (!subscribedTopics.has(topic)) return;
    subscribedTopics.delete(topic);
    if (client && connected.value) {
      client.unsubscribe(topic);
    }
  }

  /**
   * 发送文本消息（上行 + 本地 UI 乐观插入）
   * 返回的临时消息对象带有本地生成 id（负数），服务端入库后会回推同内容消息，
   * 届时由页面根据 content + senderId 去重。
   */
  function sendTextMessage(receiverId: number, content: string): MessageVO | null {
    if (!client || !content) return null;
    const { id: senderId } = me();
    const convId = conversationId(senderId, receiverId);
    const payload = JSON.stringify({
      msgType: 'text',
      content,
      senderId,
      receiverId,
    });
    client.publish(p2pSendTopic(senderId, receiverId), payload, { qos: 1 });

    const temp: MessageVO = {
      id: -Date.now(), // 临时 id（负数），服务端回推的消息 id 为正数
      conversationId: convId,
      senderId,
      receiverId,
      msgType: 'text',
      content,
      status: 0, // 发送中
      serverReceivedAt: new Date().toISOString(),
    };
    newMessages.push(temp);
    return temp;
  }

  // ---------- 未读数维护 ----------

  function initUnread(map: UnreadMap): void {
    unreadMap.value = { ...map };
  }

  function incrUnread(convId: string, step = 1): void {
    unreadMap.value[convId] = (unreadMap.value[convId] ?? 0) + step;
  }

  function clearUnread(convId: string): void {
    unreadMap.value[convId] = 0;
  }

  function totalUnread(): number {
    return Object.values(unreadMap.value).reduce((s, v) => s + (v || 0), 0);
  }

  // ---------- 内部：接收处理 ----------

  function handleIncoming(_topic: string, payload: ImPushPayload): void {
    const { id: meId } = me();
    // 事件型消息（已读回传 / 撤回）
    if (payload.eventType) {
      eventMessages.push(payload);
      // 对端"已读" → 仅作 UI 标识更新，不增减未读数
      if (payload.eventType === 'read') {
        // 页面可订阅 eventMessages 来更新"对端已读到 xxx" 的状态
      }
      // 撤回事件 → 由页面根据 eventMsgId 找到对应消息并标记为撤回
      return;
    }

    // 常规内容消息
    const vo: MessageVO = {
      id: Math.floor(Math.random() * 1_000_000_000), // 这里会被服务端入库的真实消息覆盖（页面用时间排序去重）
      conversationId: payload.conversationId,
      senderId: payload.senderId ?? 0,
      receiverId: payload.receiverId,
      msgType: (payload as any).msgType === 'text' ? 'text' : 'text',
      content: payload.content,
      extra: payload.extra,
      status: 1,
      serverReceivedAt: payload.serverTime,
    };
    newMessages.push(vo);

    // 如果我是接收方 → 未读数 +1
    if (vo.senderId !== meId) {
      incrUnread(payload.conversationId, 1);
    }
  }

  return {
    // state
    connected,
    connecting,
    error,
    unreadMap,
    newMessages, // 仅作为"事件源"集合，页面自行 push/splice 管理
    eventMessages,
    // action
    connect,
    disconnect,
    subscribeConversation,
    unsubscribeConversation,
    sendTextMessage,
    // unread helper
    initUnread,
    incrUnread,
    clearUnread,
    totalUnread,
  };
});

export { conversationId };
