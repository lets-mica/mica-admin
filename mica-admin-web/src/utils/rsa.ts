/**
 * RSA 加密工具
 * 使用 JSEncrypt 对密码进行加密
 */
import { JSEncrypt } from 'jsencrypt';

const DEFAULT_KEY_LENGTH = 1024;

/**
 * RSA 加密
 * @param publicKey 公钥 (PEM 格式)
 * @param text 要加密的文本
 * @returns 加密后的 base64 字符串
 */
export function encrypt(publicKey: string, text: string): string {
  if (!text || !publicKey) {
    return '';
  }

  try {
    const encryptor = new JSEncrypt();
    encryptor.setPublicKey(publicKey);
    const encrypted = encryptor.encrypt(text);
    return encrypted || '';
  } catch (error) {
    console.error('RSA encrypt error:', error);
    return '';
  }
}

/**
 * RSA 解密 (如果需要)
 * @param privateKey 私钥 (PEM 格式)
 * @param encrypted 加密的文本
 * @returns 解密后的原文
 */
export function decrypt(privateKey: string, encrypted: string): string {
  if (!encrypted || !privateKey) {
    return '';
  }

  try {
    const decryptor = new JSEncrypt();
    decryptor.setPrivateKey(privateKey);
    const decrypted = decryptor.decrypt(encrypted);
    return decrypted || '';
  } catch (error) {
    console.error('RSA decrypt error:', error);
    return '';
  }
}

export { DEFAULT_KEY_LENGTH };
