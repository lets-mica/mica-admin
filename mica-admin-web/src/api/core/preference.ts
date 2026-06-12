import { api } from '#/api/request';

/**
 * mica-admin 偏好设置接口（系统默认存储在 sys_config）
 *
 * 叶子粒度存储：每一项偏好对应 sys_config 一行，
 *   field = preference.{block}.{leaf}
 *   value = 字符串化的叶子值
 */

/**
 * 获取系统默认偏好（扁平 KV）
 */
export async function getPreferenceDefaultsApi(): Promise<Record<string, string>> {
  return api.get<Record<string, string>>('/api/system/config/preference/all');
}

/**
 * 批量保存系统默认偏好（需 system:config:edit 权限）
 */
export async function savePreferenceDefaultsApi(kv: Record<string, string>): Promise<void> {
  return api.put<void>('/api/system/config/preference/batch', kv);
}