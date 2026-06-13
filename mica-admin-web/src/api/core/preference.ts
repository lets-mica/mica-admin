import { api } from '#/api/request';

/**
 * mica-admin 偏好设置接口（整存 JSON，存于 sys_config.field = 'preference.default'）
 */

/**
 * 获取系统默认偏好（后端直接返回 JSON 对象，axios 拿到时已是对象）
 */
export async function getPreferenceDefaultApi(): Promise<Record<string, any>> {
  return api.get<Record<string, any>>('/api/system/config/preference/default');
}

/**
 * 保存系统默认偏好（需 system:config:edit 权限）
 * @param json 完整的偏好对象
 */
export async function savePreferenceDefaultApi(json: Record<string, any>): Promise<void> {
  return api.put<void>('/api/system/config/preference/default', json);
}