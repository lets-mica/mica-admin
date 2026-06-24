/**
 * IM 群管理 API
 *
 * 后端 VO 字段与前端类型差异(role 大小写等),在 API 客户端做一次映射。
 */
import { http } from '@/utils/request'
import type {
  GroupVo,
  GroupMemberVo,
  GroupCreateForm,
  GroupUpdateForm
} from '@/types/im'

/* ========== 后端原始 VO ========== */
interface ServerGroupVo {
  id: number
  name: string
  avatar?: string
  type?: string
  ownerId: number
  ownerName?: string
  deptId?: number
  announcement?: string
  memberCount: number
  maxMembers?: number
  /** 后端:owner / admin / member */
  role?: string
  nickname?: string
  lastMsgPreview?: string
  lastMsgTime?: string
}

interface ServerGroupMemberVo {
  userId: number
  username?: string
  nickname: string
  avatar?: string
  /** 后端:owner / admin / member */
  role?: string
  joinedAt?: string
  muted?: boolean
}

/* ========== Server → Client 映射 ========== */
function normalizeRole(role?: string): 'OWNER' | 'ADMIN' | 'MEMBER' {
  const r = (role || '').toLowerCase()
  if (r === 'owner') return 'OWNER'
  if (r === 'admin') return 'ADMIN'
  return 'MEMBER'
}

function toClientGroup(s: ServerGroupVo): GroupVo {
  return {
    id: s.id,
    name: s.name,
    avatar: s.avatar,
    ownerId: s.ownerId,
    announcement: s.announcement,
    memberCount: s.memberCount,
    myRole: normalizeRole(s.role),
    createdAt: s.lastMsgTime || ''
  }
}

function toClientGroupMember(s: ServerGroupMemberVo): GroupMemberVo {
  return {
    userId: s.userId,
    username: s.username || '',
    nickname: s.nickname,
    avatar: s.avatar,
    role: normalizeRole(s.role),
    joinedAt: s.joinedAt || '',
    muted: s.muted
  }
}

/* ========== 对外 API ========== */
export function getMyGroups() {
  return http
    .get<ServerGroupVo[]>('/api/im/groups/my')
    .then((arr) => (arr || []).map(toClientGroup))
}

export function createGroup(form: GroupCreateForm) {
  return http
    .post<ServerGroupVo>('/api/im/groups', form)
    .then(toClientGroup)
}

export function getGroupDetail(groupId: number) {
  return http
    .get<ServerGroupVo>(`/api/im/groups/${groupId}`)
    .then(toClientGroup)
}

export function updateGroup(groupId: number, form: GroupUpdateForm) {
  return http.put<void>(`/api/im/groups/${groupId}`, form)
}

export function dissolveGroup(groupId: number) {
  return http.delete<void>(`/api/im/groups/${groupId}`)
}

export function getGroupMembers(groupId: number) {
  return http
    .get<ServerGroupMemberVo[]>(`/api/im/groups/${groupId}/members`)
    .then((arr) => (arr || []).map(toClientGroupMember))
}

export function inviteMembers(groupId: number, userIds: number[]) {
  return http.post<void>(`/api/im/groups/${groupId}/members`, { userIds })
}

export function kickMember(groupId: number, userId: number) {
  return http.delete<void>(`/api/im/groups/${groupId}/members/${userId}`)
}
