/**
 * mica-admin 后端认证 API
 *
 * 登录流程：
 *   1. getPublicKeyApi  → GET  /api/auth/public-key   (无需 token)
 *   2. getCaptchaApi    → GET  /api/auth/captcha       (无需 token)
 *   3. loginApi         → POST /api/session            (无需 token，密码 RSA 加密、form-urlencoded)
 *      表单字段：username / password / validateCodeId / validateCode / rememberMe
 *      （后端 SecWebAuthenticationDetails 识别的字段名与前端一致）
 *   4. getMenusApi      → GET  /api/auth/menus         (需 token)
 *   5. logoutApi        → GET  /api/logout             (需 token)
 *
 * 用户信息请用 `getUserInfoApi` from '#/api/core/user'（已映射为 UserInfo 类型）
 */
import { message } from '#/adapter/naive';
import { api, createApi } from '#/api/request';
import { encrypt } from '#/utils/rsa';

// 缓存公钥
let cachedPublicKey = '';

// 登录前的预认证实例（不带 token，登录 / 公钥 / 验证码 用）
const preAuthApi = createApi();

/**
 * 获取并缓存 RSA 公钥
 */
export async function getPublicKeyApi(): Promise<string> {
  if (cachedPublicKey) {
    return cachedPublicKey;
  }

  try {
    const data = await preAuthApi.get<any>('/api/auth/public-key');

    if (typeof data === 'string' && data.startsWith('MIG')) {
      cachedPublicKey = data;
      return data;
    }
    if (data && typeof data === 'object' && data.publicKey) {
      cachedPublicKey = data.publicKey;
      return cachedPublicKey;
    }

    throw new Error('未获取到有效的公钥');
  } catch (error: any) {
    console.error('[API] Failed to get public key:', error);
    message.error('获取公钥失败');
    throw error;
  }
}

/**
 * 获取验证码图片
 */
export async function getCaptchaApi(): Promise<{ captchaId: string; captchaImage: string }> {
  try {
    const data = await preAuthApi.get<any>('/api/auth/captcha', {
      params: { t: Date.now() },
    });

    if (data && data.uuid && data.base64) {
      return {
        captchaId: data.uuid,
        captchaImage: data.base64,
      };
    }

    return { captchaId: '', captchaImage: '' };
  } catch (error) {
    console.error('[API] Failed to load captcha:', error);
    return { captchaId: '', captchaImage: '' };
  }
}

export interface LoginParams {
  username: string;
  password: string;
  /** 验证码 UUID（来自 getCaptchaApi 返回的 captchaId） */
  validateCodeId: string;
  /** 用户输入的验证码 */
  validateCode: string;
  /** 是否记住登录（默认 true） */
  rememberMe?: boolean;
}

/**
 * 登录
 * 后端 SecWebAuthenticationDetails 识别的表单字段：
 *   username / password (RSA 加密后) / validateCodeId / validateCode / rememberMe
 */
export async function loginApi(data: LoginParams): Promise<{ accessToken: string; userInfo?: any }> {
  try {
    const publicKey = await getPublicKeyApi();
    const encryptedPassword = encrypt(publicKey, data.password || '');

    const formData = new URLSearchParams();
    formData.append('username', data.username || '');
    formData.append('password', encryptedPassword || '');
    formData.append('validateCodeId', data.validateCodeId);
    formData.append('validateCode', data.validateCode);
    formData.append('rememberMe', data.rememberMe === false ? 'false' : 'true');

    const respData = await preAuthApi.post<any>('/api/session', formData.toString(), {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });

    if (respData && respData.token) {
      return {
        accessToken: respData.token,
        userInfo: respData.userInfo,
      };
    }
    if (respData && typeof respData === 'object' && 'code' in respData && respData.code !== 200) {
      throw new Error(respData.msg || '登录失败');
    }

    throw new Error('登录响应格式错误');
  } catch (error: any) {
    const errorMsg = error?.response?.data?.msg || error.message || '登录失败';
    message.error(errorMsg);
    throw error;
  }
}

/**
 * 获取菜单
 */
export async function getMenusApi() {
  return api.get<any[]>('/api/auth/menus');
}

/**
 * 登出
 */
export async function logoutApi() {
  cachedPublicKey = '';
  return api.get<any>('/api/logout');
}
