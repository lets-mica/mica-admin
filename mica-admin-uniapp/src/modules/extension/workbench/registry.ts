import type { Component } from 'vue'

/**
 * 工作台扩展卡片注册中心。
 *
 * 二次开发方在此文件中 import 自己的卡片组件 + 调用 registerCard(),
 * 即可让卡片自动出现在 App 工作台首页。
 *
 * @example
 * ```ts
 * // src/modules/extension/workbench/index.ts
 * import { registerCard } from './registry'
 * import ApprovalTodo from './cards/approval-todo.vue'
 *
 * registerCard({
 *   id: 'approval-todo',
 *   title: '待我审批',
 *   order: 100,
 *   component: ApprovalTodo
 * })
 * ```
 */
export interface WorkbenchCard {
  /** 唯一标识,建议使用模块名 */
  id: string
  /** 卡片标题(用于"暂无卡片"等场景展示) */
  title: string
  /** 排序权重,数字越大越靠前;同 order 时按注册顺序 */
  order?: number
  /** Vue 组件,组件内部自行处理数据拉取 */
  component: Component
}

const cards: WorkbenchCard[] = []

/**
 * 注册一个工作台卡片。
 * 重复注册同一 id 将被忽略(避免覆盖)。
 */
export function registerCard(card: WorkbenchCard): void {
  if (cards.some((c) => c.id === card.id)) {
    console.warn(`[Workbench] 卡片 "${card.id}" 已存在,忽略重复注册`)
    return
  }
  cards.push(card)
}

/**
 * 获取当前所有已注册的工作台卡片,按 order 倒序。
 */
export function getRegisteredCards(): WorkbenchCard[] {
  return [...cards].sort((a, b) => (b.order ?? 0) - (a.order ?? 0))
}

/**
 * 清空所有卡片(仅供测试使用,二次开发方不要调用)。
 */
export function __clearCards(): void {
  cards.length = 0
}
