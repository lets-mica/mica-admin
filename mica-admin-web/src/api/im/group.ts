/**
 * IM 群管理 API（PR-1.1.2）
 *
 * 后端接口路径：
 *   GET    /api/im/groups/my                    我加入的群列表
 *   GET    /api/im/groups/{groupId}             群详情
 *   GET    /api/im/groups/{groupId}/members     群成员列表
 *   POST   /api/im/groups                       创建群组
 *   POST   /api/im/groups/{groupId}/members     邀请成员入群
 *   DELETE /api/im/groups/{groupId}/members/{userId} 移除成员
 *   DELETE /api/im/groups/{groupId}/members      主动退群
 *   DELETE /api/im/groups/{groupId}              解散群
 */
import { api } from '#/api/request';

// ---------- Type ----------

export interface GroupVO {
  id: number;
  name: string;
  avatar?: string;
  type: 'normal' | 'department';
  ownerId: number;
  ownerName?: string;
  deptId?: number;
  announcement?: string;
  memberCount: number;
  maxMembers: number;
  role?: string; // owner / admin / member
  nickname?: string;
}

export interface GroupMemberVO {
  groupId: number;
  userId: number;
  userName?: string;
  userNickName?: string;
  avatar?: string;
  role: string;
  nickname?: string;
  joinedAt?: string;
}

export interface GroupCreateForm {
  name: string;
  avatar?: string;
  type?: string;
  announcement?: string;
  memberIds?: number[];
}

export interface GroupMemberForm {
  userIds: number[];
}

// ---------- API ----------

export async function getMyGroups(): Promise<GroupVO[]> {
  return api.get<GroupVO[]>('/api/im/groups/my');
}

export async function getGroupDetail(groupId: number): Promise<GroupVO> {
  return api.get<GroupVO>(`/api/im/groups/${groupId}`);
}

export async function getGroupMembers(groupId: number): Promise<{ total: number; list: GroupMemberVO[] }> {
  return api.get(`/api/im/groups/${groupId}/members`);
}

export async function createGroup(form: GroupCreateForm): Promise<{ groupId: number; name: string; memberCount: number }> {
  return api.post('/api/im/groups', form);
}

export async function addGroupMembers(
  groupId: number,
  form: GroupMemberForm,
): Promise<{ groupId: number; added: number }> {
  return api.post(`/api/im/groups/${groupId}/members`, form);
}

export async function removeGroupMember(
  groupId: number,
  userId: number,
): Promise<{ groupId: number; removedUserId: number }> {
  return api.delete(`/api/im/groups/${groupId}/members/${userId}`);
}

export async function quitGroup(groupId: number): Promise<{ groupId: number; userId: number }> {
  return api.delete(`/api/im/groups/${groupId}/members`);
}

export async function dismissGroup(groupId: number): Promise<{ groupId: number }> {
  return api.delete(`/api/im/groups/${groupId}`);
}
