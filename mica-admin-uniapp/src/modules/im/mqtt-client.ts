/**
 * mqtt.js 5.x 客户端封装(mica-admin IM 实时通道)
 *
 * 关键点:
 * 1. username 传 JWT(不是用户名)
 * 2. 断线重连后需重新订阅
 * 3. 同会话消息按 serverReceivedAt 升序
 */
import mqtt, { type MqttClient, type IClientOptions } from 'mqtt'
import {
  MQTT_CONFIG,
  buildClientId,
  buildSubscribeTopics,
  getMqttUsername
} from '@/utils/mqtt-config'

export type MqttStatus = 'disconnected' | 'connecting' | 'connected' | 'reconnecting'

export interface MqttMessageHandler {
  (topic: string, payload: any): void
}

class MqttClientManager {
  private client: MqttClient | null = null
  private handlers = new Set<MqttMessageHandler>()
  private subscribedTopics: string[] = []
  private userId: number = 0
  private myGroupIds: number[] = []
  private statusListeners = new Set<(s: MqttStatus) => void>()
  private _status: MqttStatus = 'disconnected'

  get status(): MqttStatus {
    return this._status
  }

  private setStatus(s: MqttStatus) {
    this._status = s
    this.statusListeners.forEach((cb) => cb(s))
  }

  onStatus(cb: (s: MqttStatus) => void): () => void {
    this.statusListeners.add(cb)
    cb(this._status)
    return () => this.statusListeners.delete(cb)
  }

  onMessage(handler: MqttMessageHandler): () => void {
    this.handlers.add(handler)
    return () => this.handlers.delete(handler)
  }

  connect(userId: number, myGroupIds: number[]) {
    if (this.client) return
    this.userId = userId
    this.myGroupIds = myGroupIds
    this.setStatus('connecting')

    const opts: IClientOptions = {
      ...MQTT_CONFIG.options,
      clientId: buildClientId(userId),
      username: getMqttUsername(),
      protocolVersion: 5
    }

    this.client = mqtt.connect(MQTT_CONFIG.fullUrl, opts)

    this.client.on('connect', () => {
      console.log('[mqtt] connected')
      this.setStatus('connected')
      this.resubscribe()
    })

    this.client.on('reconnect', () => {
      console.log('[mqtt] reconnecting')
      this.setStatus('reconnecting')
    })

    this.client.on('disconnect', () => {
      console.log('[mqtt] disconnected')
      this.setStatus('disconnected')
    })

    this.client.on('error', (err) => {
      console.error('[mqtt] error', err)
    })

    this.client.on('message', (topic, payload) => {
      try {
        const data = JSON.parse(payload.toString())
        this.handlers.forEach((h) => h(topic, data))
      } catch (e) {
        console.warn('[mqtt] message parse failed', e)
      }
    })
  }

  private resubscribe() {
    if (!this.client || !this.userId) return
    const topics = buildSubscribeTopics(this.userId, this.myGroupIds)
    this.subscribedTopics = topics
    this.client.subscribe(topics, { qos: 1 }, (err) => {
      if (err) console.warn('[mqtt] subscribe failed', err)
      else console.log('[mqtt] subscribed', topics)
    })
  }

  /** 加入新群后增量订阅 */
  subscribeGroupInbox(groupId: number) {
    if (!this.client || !this.client.connected) return
    const topic = `im/group/${groupId}/inbox`
    if (this.subscribedTopics.includes(topic)) return
    this.subscribedTopics.push(topic)
    this.client.subscribe(topic, { qos: 1 })
  }

  /** 退群/被踢后取消订阅 */
  unsubscribeGroupInbox(groupId: number) {
    if (!this.client || !this.client.connected) return
    const topic = `im/group/${groupId}/inbox`
    this.subscribedTopics = this.subscribedTopics.filter((t) => t !== topic)
    this.client.unsubscribe(topic)
  }

  publish(topic: string, payload: unknown, qos: 0 | 1 | 2 = 1) {
    if (!this.client || !this.client.connected) {
      throw new Error('MQTT 未连接')
    }
    this.client.publish(topic, JSON.stringify(payload), { qos })
  }

  disconnect() {
    this.client?.end(true)
    this.client = null
    this.subscribedTopics = []
    this.setStatus('disconnected')
  }
}

export const mqttClient = new MqttClientManager()