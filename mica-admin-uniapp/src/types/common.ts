/**
 * mica-admin 公共类型定义
 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface CurrentUser {
  userId: number
  username: string
  nickname: string
  avatar?: string
  email?: string
  phone?: string
  deptId?: number
  deptName?: string
  postName?: string
  roles: string[]
  permissions: string[]
  isAdmin: boolean
}

export interface MenuItem {
  id: number
  parentId: number
  path: string
  name: string
  title: string
  icon?: string
  component?: string
  redirect?: string
  hidden?: boolean
  type: 'MENU_DIR' | 'MENU' | 'BUTTON'
  permission?: string
  keepAlive?: boolean
  children?: MenuItem[]
}