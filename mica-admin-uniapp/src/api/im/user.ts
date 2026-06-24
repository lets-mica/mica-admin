/**
 * IM 用户查询 API
 */
import { http } from '@/utils/request'
import type { ImUserVo } from '@/types/im'

export function searchImUsers(keyword: string, limit = 20) {
  return http.get<ImUserVo[]>('/api/im/users/search', { keyword, limit })
}

export function getImUsersBatch(ids: number[] | string) {
  const param = Array.isArray(ids) ? ids.join(',') : ids
  return http.get<ImUserVo[]>('/api/im/users/batch', { ids: param })
}