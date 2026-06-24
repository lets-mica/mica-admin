/**
 * RSA 加密(登录密码用)
 * 使用 forge 风格的纯 JS 实现,跨端无需原生依赖
 *
 * 简化方案:实际生产建议使用 jsencrypt 或 forge;
 * 这里用一种跨端可用的方式 —— 调用 mica-admin 后端的 RSA 公钥对密码进行加密。
 */
import { http } from './request'

let publicKeyCache: string | null = null

export async function getPublicKey(): Promise<string> {
  if (publicKeyCache) return publicKeyCache
  const key = await http.get<string>('/api/auth/public-key')
  publicKeyCache = key
  return key
}

/**
 * 使用公钥加密(简化版:实际项目请使用 jsencrypt 或 forge)
 * 此处仅做占位,生产环境务必替换为标准实现
 */
export function encryptRSA(plain: string, publicKey: string): string {
  // 生产环境应使用 jsencrypt:
  // import JSEncrypt from 'jsencrypt'
  // const enc = new JSEncrypt()
  // enc.setPublicKey(publicKey)
  // return enc.encrypt(plain) || ''
  //
  // 此处为占位,实际部署前必须替换
  console.warn('[rsa] placeholder encryption — 请在生产环境前替换为 jsencrypt')
  return Buffer.from(plain).toString('base64')
}

export function clearPublicKeyCache() {
  publicKeyCache = null
}