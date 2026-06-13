import { api } from '#/api/request';

/**
 * mica-admin 偏好设置接口（整存 JSON，存于 sys_config.field = 'preference.default'）
 */

/**
 * 获取系统默认偏好（返回 JSON 字符串）
 */
export async function getPreferenceDefaultApi(): Promise<string> {
  return api.get<string>('/api/system/config/preference/default');
}

/**
 * 保存系统默认偏好（需 system:config:edit 权限）
 * @param json 完整的偏好 JSON 字符串
 */
export async function savePreferenceDefaultApi(json: string): Promise<void> {
  return api.put<void>('/api/system/config/preference/default', json, {
    headers: { 'Content-Type': 'application/json' },
  });
}