/**
 * IM 模块的用户查询 API
 *
 * 后端接口路径：
 *   GET /api/im/users/search?keyword=&limit=  模糊搜索可聊天的用户
 *   GET /api/im/users/batch?ids=1,2,3          批量查询用户简要信息
 *
 * 与 system 模块的用户接口区别：
 *   - 仅校验登录，无细粒度权限限制
 *   - 只返回 id/userName/nickName/avatar 四个字段，不返回手机/邮箱等敏感信息
 *   - 自动排除禁用账号 + 自己
 */
import { api } from '#/api/request';

export interface ImUserItem {
  id: number;
  userName: string;
  nickName: string;
  avatar?: string;
}

/** 模糊搜索用户 */
export async function searchUsers(params?: {
  keyword?: string;
  limit?: number;
}): Promise<ImUserItem[]> {
  return api.get<ImUserItem[]>('/api/im/users/search', { params });
}

/** 按 id 批量查询用户简要信息 */
export async function getUsersByIds(ids: number[]): Promise<ImUserItem[]> {
  if (!ids || ids.length === 0) return [];
  return api.get<ImUserItem[]>('/api/im/users/batch', {
    params: { ids: ids.join(',') },
  });
}