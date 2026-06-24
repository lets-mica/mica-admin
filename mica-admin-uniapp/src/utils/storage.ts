/**
 * 本地存储封装(uni.setStorageSync 兼容 H5/App/小程序)
 */
function uniStorage(): {
  get<T>(key: string): T | undefined
  set(key: string, value: unknown): void
  remove(key: string): void
  clear(): void
} {
  return {
    get<T>(key: string): T | undefined {
      try {
        const v = uni.getStorageSync(key)
        return v === '' || v === null || v === undefined ? undefined : (v as T)
      } catch {
        return undefined
      }
    },
    set(key: string, value: unknown) {
      try {
        uni.setStorageSync(key, value)
      } catch (e) {
        console.warn('[storage] set failed', key, e)
      }
    },
    remove(key: string) {
      try {
        uni.removeStorageSync(key)
      } catch (e) {
        console.warn('[storage] remove failed', key, e)
      }
    },
    clear() {
      try {
        uni.clearStorageSync()
      } catch (e) {
        console.warn('[storage] clear failed', e)
      }
    }
  }
}

export const storage = uniStorage()