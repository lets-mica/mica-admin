import { api } from '#/api/request';
import { exportExcel } from '#/utils/export-excel';

export interface SysJob {
  id: number;
  jobKey: string;
  jobName: string;
  cronExpression: string;
  enabled: boolean;
  paramSchema?: string;
  description?: string;
  createdBy?: string;
  createdAt?: string;
  updatedBy?: string;
  updatedAt?: string;
}

export interface JobQuery {
  page?: number;
  size?: number;
  blurry?: string;
  enabled?: boolean;
  createTime?: [string, string];
}

export interface JobForm {
  id?: number;
  jobKey: string;
  jobName: string;
  cronExpression?: string;
  enabled: boolean;
  paramSchema?: string;
  description?: string;
}

export interface RunOnceForm {
  jobKey: string;
  params?: Record<string, unknown>;
}

const BASE_URL = '/api/system/job';

export async function listJob(params?: JobQuery) {
  return api.get<any>(BASE_URL, { params });
}

export async function getJob(id: number) {
  return api.get<SysJob>(`${BASE_URL}/${id}`);
}

export async function addJob(data: JobForm) {
  return api.post(BASE_URL, data);
}

export async function updateJob(data: JobForm) {
  return api.put(BASE_URL, data);
}

export async function deleteJob(ids: number[]) {
  return api.delete(BASE_URL, { data: ids });
}

export async function exportJobExcel(params?: JobQuery) {
  return exportExcel({
    api,
    url: `${BASE_URL}/download`,
    filename: '任务数据',
    params: params as Record<string, unknown> | undefined,
  });
}

export async function startJob(jobKey: string) {
  return api.put(`${BASE_URL}/start/${jobKey}`);
}

export async function stopJob(jobKey: string) {
  return api.put(`${BASE_URL}/stop/${jobKey}`);
}

export async function refreshJob(jobKey: string) {
  return api.put(`${BASE_URL}/refresh/${jobKey}`);
}

export async function runOnce(jobKey: string) {
  return api.post(`${BASE_URL}/run-once/${jobKey}`);
}

export async function runOnceWithParams(data: RunOnceForm) {
  return api.post(`${BASE_URL}/run-once`, data);
}

export async function checkRegistered(jobKey: string) {
  return api.get<boolean>(`${BASE_URL}/registered/${jobKey}`);
}
