/**
 * 字典 API
 */
import { http } from '@/utils/request'
import type { PageResult } from '@/utils/request'

/**
 * 字典类型(对应后端 SysDict)
 * @see net.dreamlu.mica.admin.project.system.entity.SysDict
 */
export interface DictType {
  id: number
  /** 字典名称(后端字段 name) */
  name: string
  description?: string
  status?: number
  /** 备注(后端字段 remark) */
  remark?: string
}

/**
 * 字典详情(对应后端 SysDictInfo)
 * @see net.dreamlu.mica.admin.project.system.entity.SysDictInfo
 */
export interface DictItem {
  id: number
  /** 显示顺序(后端字段 seq) */
  seq?: number
  label: string
  value: string
  /** 字典类型 */
  type?: string
  cssClass?: string
  listClass?: string
  isDefault?: boolean
  status?: number
  /** 备注(后端字段 remark) */
  remark?: string
}

/**
 * 字典类型分页
 */
export function getDictTypes(params?: { current?: number; size?: number; blurry?: string }) {
  return http.get<PageResult<DictType>>('/api/system/dict', params)
}

/**
 * 字典详情列表(后端 /api/system/dict/info)
 */
export function getDictItems(type: string, size = 9999) {
  return http.get<PageResult<DictItem>>('/api/system/dict/info', {
    name: type,
    current: 1,
    size
  })
}
