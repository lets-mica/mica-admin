/**
 * 日志 API
 */
import { api, parsePage } from '#/api/request';
import { exportExcel } from '#/utils/export-excel';

export interface LogItem {
  id: number;
  userId?: number;
  userName?: string;
  description: string;
  method?: string;
  params?: string;
  requestUrl?: string;
  ip?: string;
  requestIp?: string;
  ua?: string;
  browser?: string;
  address?: string;
  requestTime?: number;
  success?: boolean;
  createdAt?: string;
  createTime?: string;
  exceptionDetail?: string;
}

export async function getOperationLogList(params?: {
  page?: number;
  size?: number;
  blurry?: string;
  startDate?: string;
  endDate?: string;
}) {
  const data = await api.get<any>('/api/system/logs/info', { params });
  return parsePage<LogItem>(data);
}

export async function getErrorLogList(params?: {
  page?: number;
  size?: number;
  blurry?: string;
  startDate?: string;
  endDate?: string;
}) {
  const data = await api.get<any>('/api/system/logs/error', { params });
  return parsePage<LogItem>(data);
}

export async function clearOperationLogs() {
  return api.delete('/api/system/logs/info');
}

export async function clearErrorLogs() {
  return api.delete('/api/system/logs/error');
}

export async function getErrorDetail(id: number) {
  return api.get<any>(`/api/system/logs/error/${id}`);
}

export async function exportInfoLogExcel(params?: Record<string, unknown>) {
  return exportExcel({ api, url: '/api/system/logs/info/download', filename: '操作日志', params });
}

export async function exportErrorLogExcel(params?: Record<string, unknown>) {
  return exportExcel({ api, url: '/api/system/logs/error/download', filename: '错误日志', params });
}
