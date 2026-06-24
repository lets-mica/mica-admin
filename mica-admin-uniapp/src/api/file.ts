/**
 * 文件上传 API(x-file-storage)
 */
import { env } from '@/config/env'

export interface FileVo {
  id: number
  name: string
  url: string
  size: number
  contentType?: string
}

export function uploadFile(filePath: string, name?: string): Promise<FileVo> {
  return new Promise((resolve, reject) => {
    const header: Record<string, string> = {}
    try {
      const raw = uni.getStorageSync('mica-admin-token')
      if (raw) header['Authorization'] = `Bearer ${raw}`
    } catch {
      // ignore
    }

    uni.uploadFile({
      url: `${env.apiUrl}/upload/file`,
      filePath,
      name: name || 'file',
      header,
      success: (res) => {
        try {
          const body = JSON.parse(res.data) as { code: number; msg?: string; data: FileVo }
          if (body.code === 0) resolve(body.data)
          else reject(new Error(body.msg || '上传失败'))
        } catch (e) {
          reject(e as Error)
        }
      },
      fail: (err) => reject(err as unknown as Error)
    })
  })
}

export function previewFile(url: string) {
  uni.openDocument({
    filePath: url,
    showMenu: true,
    fail: () => uni.showToast({ title: '无法预览', icon: 'none' })
  })
}