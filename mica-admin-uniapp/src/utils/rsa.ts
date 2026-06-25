/**
 * RSA 加密工具(对齐 mica-admin-web/src/utils/rsa.ts)
 * 使用 JSEncrypt 对登录密码进行 RSA 加密
 *
 * H5 端开箱即用;App 端如果缺少 atob/btoa,需要补充 polyfill
 */
import JSEncrypt from 'jsencrypt'
import { http } from './request'

let publicKeyCache: string | null = null

/**
 * 获取后端 RSA 公钥(带缓存)
 */
export async function getPublicKey(): Promise<string> {
  if (publicKeyCache) return publicKeyCache
  const key = await http.get<string>('/api/auth/public-key')
  publicKeyCache = key
  return key
}

/**
 * RSA 加密(用于登录密码)
 * @param plain 明文
 * @param publicKey PEM 格式公钥
 * @returns 加密后的 base64 字符串
 */
export function encryptRSA(plain: string, publicKey: string): string {
  if (!plain || !publicKey) return ''
  try {
    const encryptor = new JSEncrypt()
    encryptor.setPublicKey(publicKey)
    const encrypted = encryptor.encrypt(plain)
    return encrypted || ''
  } catch (error) {
    console.error('[rsa] encrypt error:', error)
    return ''
  }
}

/**
 * 清空公钥缓存(用于切换账号、登出后强制刷新)
 */
export function clearPublicKeyCache() {
  publicKeyCache = null
}