/**
 * 字典 API
 */
import { http } from '@/utils/request'

export interface DictType {
  id: number
  type: string
  description: string
  remarks?: string
}

export interface DictItem {
  id: number
  type: string
  label: string
  value: string
  sort: number
  remarks?: string
}

export function getDictTypes() {
  return http.get<DictType[]>('/api/system/dict')
}

export function getDictItems(type: string) {
  return http.get<DictItem[]>('/api/system/dict-info', { type })
}