import { api, parsePage, pickData } from '#/api/request';

/** 存储类型 */
export type StorageType = 'LOCAL' | 'OSS' | 'S3' | 'MINIO' | string;

/** 文件存储信息（对应后端 sys_file_storage 表） */
export interface FileStorageItem {
  id: number;
  /** 存储类型：LOCAL / OSS / S3 / MINIO */
  storageType?: StorageType;
  /** OSS bucket */
  bucket?: string;
  /** OSS endpoint */
  endpoint?: string;
  /** 文件唯一 key */
  fileKey?: string;
  /** 文件 MD5 */
  md5?: string;
  /** 访问地址 */
  url?: string;
  /** 原始文件名 */
  fileName?: string;
  /** 真实存储名 */
  fileRealName?: string;
  /** 后缀 */
  suffix?: string;
  /** 文件大小（字节） */
  size?: number;
  /** MIME 类型 */
  mimeType?: string;
  /** 业务类型 */
  fileType?: string;
  /** 上传用户ID */
  userId?: number;
  /** 是否私有：1=私有 0=公开 */
  isPrivate?: boolean;
  /** 创建者 */
  createdBy?: string;
  /** 创建时间 */
  createdAt?: string;
  /** 更新时间 */
  updatedAt?: string;
}

/** 上传文件请求参数 */
export interface UploadFileParams {
  file: File;
  /** 业务类型 */
  fileType?: string;
  /** 是否私有 */
  isPrivate?: boolean;
  /** 存储类型（可选，不传则使用后端默认） */
  storageType?: StorageType;
}

/** 分页查询参数 */
export interface FileStorageQuery {
  page?: number;
  size?: number;
  /** 原始文件名（模糊） */
  fileName?: string;
  /** 业务类型 */
  fileType?: string;
  /** 存储类型 */
  storageType?: StorageType;
  /** 创建时间范围 */
  createdAt?: [string, string];
}

const BASE_URL = '/api/system/file/storage';

/** 分页查询文件存储列表 */
export async function getFileStorageList(params?: FileStorageQuery) {
  const data = await api.get<any>(BASE_URL, { params });
  return parsePage<FileStorageItem>(data);
}

/** 获取文件详情 */
export async function getFileStorage(id: number): Promise<FileStorageItem> {
  const data = await api.get<any>(`${BASE_URL}/${id}`);
  return pickData<FileStorageItem>(data);
}

/** 删除文件（支持批量） */
export async function deleteFileStorage(ids: number[]) {
  return api.delete(BASE_URL, { data: ids });
}

/** 上传文件 */
export async function uploadFileStorage(params: UploadFileParams): Promise<FileStorageItem> {
  const formData = new FormData();
  formData.append('file', params.file);
  if (params.fileType) formData.append('fileType', params.fileType);
  if (params.isPrivate !== undefined) formData.append('isPrivate', String(params.isPrivate));
  if (params.storageType) formData.append('storageType', params.storageType);
  const data = await api.post<any>(`${BASE_URL}/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return pickData<FileStorageItem>(data);
}

/** 下载文件（通过 blob 保存到本地） */
export async function downloadFileStorage(url: string, filename?: string) {
  const blob = await api.get<Blob>(url, { responseType: 'blob' });
  const objectUrl = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = objectUrl;
  a.download = filename || (url.split('/').pop() || 'download');
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(objectUrl);
}

/** 兼容旧命名（保留向后兼容，避免引用侧大改） */
export type OssFile = FileStorageItem;
export const getOssList = getFileStorageList;
export const deleteOss = deleteFileStorage;
