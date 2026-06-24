/**
 * IM 群管理 API
 */
import { http } from '@/utils/request'
import type {
  GroupVo,
  GroupMemberVo,
  GroupCreateForm,
  GroupUpdateForm
} from '@/types/im'

export function getMyGroups() {
  return http.get<GroupVo[]>('/api/im/groups/my')
}

export function createGroup(form: GroupCreateForm) {
  return http.post<GroupVo>('/api/im/groups', form)
}

export function getGroupDetail(groupId: number) {
  return http.get<GroupVo>(`/api/im/groups/${groupId}`)
}

export function updateGroup(groupId: number, form: GroupUpdateForm) {
  return http.put<void>(`/api/im/groups/${groupId}`, form)
}

export function dissolveGroup(groupId: number) {
  return http.delete<void>(`/api/im/groups/${groupId}`)
}

export function getGroupMembers(groupId: number) {
  return http.get<GroupMemberVo[]>(`/api/im/groups/${groupId}/members`)
}

export function inviteMembers(groupId: number, userIds: number[]) {
  return http.post<void>(`/api/im/groups/${groupId}/members`, { userIds })
}

export function kickMember(groupId: number, userId: number) {
  return http.delete<void>(`/api/im/groups/${groupId}/members/${userId}`)
}