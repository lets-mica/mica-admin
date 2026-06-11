/** 11 位手机号（与老版 mica-admin-web 一致） */
export function isValidPhone(phone: string): boolean {
  return /^1[3|4|5|7|8][0-9]\d{8}$/.test(phone);
}

/** 简单邮箱校验 */
export function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}
