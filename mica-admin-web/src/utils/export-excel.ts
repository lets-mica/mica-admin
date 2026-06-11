/**
 * 通用 Excel 导出工具函数
 * 配合后端 @ResponseExcel 注解使用
 */
import type { AxiosRequestConfig } from 'axios';

/**
 * 满足以下结构的请求客户端即可：
 * - `get(url, config): Promise<blob | response with .data>`
 *
 * 既支持原生 axios 实例，也支持 #/api/request 中的 `api`（response 已被解包）
 */
export interface ExportApiClient {
  get: <T = any>(url: string, config?: AxiosRequestConfig) => Promise<T>;
}

export interface ExportOptions {
  /** 后端下载接口路径，如 /api/system/roles/download */
  url: string;
  /** 导出文件名（不含后缀） */
  filename: string;
  /** 查询参数 */
  params?: Record<string, unknown>;
  /** 客户端实例（axios 或 #/api/request 的 api 都可） */
  api: ExportApiClient;
}

/**
 * 调用后端导出接口并触发浏览器下载 Excel 文件
 */
export async function exportExcel(options: ExportOptions) {
  const { url, filename, params, api } = options;
  const data = await api.get<Blob>(url, {
    params,
    responseType: 'blob',
  });

  // 兼容两种返回：
  // 1. #/api/request 的 api 已解包：data 直接就是 Blob
  // 2. 原生 axios：data 是 AxiosResponse，blob 在 .data
  const blob = data instanceof Blob ? data : (data as any).data;

  const blobUrl = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = blobUrl;
  link.download = `${filename}.xlsx`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(blobUrl);
}
