/**
 * 通知公告 API
 */
import { http } from '@/utils/request'

export interface NoticeVo {
  id: number
  title: string
  type?: number
  content: string
  status?: number
  category?: string
  level?: string
  publisher?: string
  publishTime?: string
  createdAt: string
}

export function getNotices(params: { current?: number; size?: number; title?: string }) {
  return http.get<import('@/utils/request').PageResult<NoticeVo>>('/api/system/notice', params)
}

export function getNoticeFeed(params: { current?: number; size?: number; title?: string }) {
  return http.get<import('@/utils/request').PageResult<NoticeVo>>('/api/system/notice/feed', params)
}

export function getNoticeDetail(id: number) {
  return http.get<NoticeVo>(`/api/system/notice/${id}`)
}