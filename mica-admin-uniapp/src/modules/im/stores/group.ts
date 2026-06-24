/**
 * 群管理 store
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  createGroup as apiCreateGroup,
  dissolveGroup as apiDissolve,
  getGroupDetail,
  getGroupMembers,
  inviteMembers,
  kickMember,
  updateGroup
} from '@/api/im/group'
import type { GroupCreateForm, GroupVo, GroupMemberVo, GroupUpdateForm } from '@/types/im'
import { mqttClient } from '../mqtt-client'

export const useGroupStore = defineStore('im-group', () => {
  const current = ref<GroupVo | null>(null)
  const members = ref<GroupMemberVo[]>([])
  const loading = ref(false)

  async function loadDetail(groupId: number) {
    loading.value = true
    try {
      const [g, m] = await Promise.all([getGroupDetail(groupId), getGroupMembers(groupId)])
      current.value = g
      members.value = m || []
      // 订阅群收件箱
      mqttClient.subscribeGroupInbox(groupId)
    } finally {
      loading.value = false
    }
  }

  async function create(form: GroupCreateForm): Promise<GroupVo> {
    const g = await apiCreateGroup(form)
    mqttClient.subscribeGroupInbox(g.id)
    return g
  }

  async function update(groupId: number, form: GroupUpdateForm) {
    await updateGroup(groupId, form)
    if (current.value && current.value.id === groupId) {
      Object.assign(current.value, form)
    }
  }

  async function invite(groupId: number, userIds: number[]) {
    await inviteMembers(groupId, userIds)
  }

  async function kick(groupId: number, userId: number) {
    await kickMember(groupId, userId)
    members.value = members.value.filter((m) => m.userId !== userId)
  }

  async function dissolve(groupId: number) {
    await apiDissolve(groupId)
    mqttClient.unsubscribeGroupInbox(groupId)
    current.value = null
    members.value = []
  }

  return {
    current,
    members,
    loading,
    loadDetail,
    create,
    update,
    invite,
    kick,
    dissolve
  }
})