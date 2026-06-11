import type { UserInfo } from '@vben/types';

import { api } from '#/api/request';

/**
 * 获取用户信息 - mica-admin 后端
 * 通过共享 api 实例自动携带 token
 */
export async function getUserInfoApi(): Promise<UserInfo> {
  const data = await api.get<any>('/api/auth/info');
  const userInfo = data?.userInfo ?? data;

  if (!userInfo) {
    return {
      id: 0,
      username: '',
      realName: '',
      roles: [],
    } as UserInfo;
  }

  return {
    id: userInfo.id || 0,
    username: userInfo.userName || '',
    realName: userInfo.nickName || '',
    avatar: userInfo.avatar || '',
    email: userInfo.email || '',
    roles: userInfo.roleList || [],
  } as UserInfo;
}
