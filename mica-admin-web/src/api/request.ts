/**
 * mica-admin HTTP 请求封装
 *
 * 抽象层：
 * - `createApi()` 创建带 token 注入的 axios 实例，**只返回 body**（response.data）
 * - `parsePage()` 统一解析 mica-admin 分页响应（IPage 扁平 / Vben 风格 / 纯数组）
 * - `pickData()`  提取响应对象中的 data 字段（仅供部分非标接口使用）
 * - `api`        默认共享实例，绝大多数业务接口用这个即可
 *
 * 响应约定（mica-admin 后端）：
 * - 正常响应：直接返回数据体，无 R 包装
 *   · 分页:    { records: [...], total: 0 }        （Mybatis-Plus IPage）
 *   · 对象:    { ... }                              （直接 JSON）
 *   · 列表:    [...]
 * - 异常响应：由 Spring 异常处理器统一包装为 { code, msg, ... } 或
 *             标准 Spring 错误 { status, error, message, path }
 *   错误已经在 axios 拦截层抛出，业务代码不需处理 R 包装
 */
import type { AxiosInstance, AxiosRequestConfig } from 'axios';

import axios from 'axios';

import { useAccessStore } from '@vben/stores';

/** 简化版方法签名：直接返回 response body */
export interface ApiClient {
  get: <T = any>(url: string, config?: AxiosRequestConfig) => Promise<T>;
  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) => Promise<T>;
  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig) => Promise<T>;
  delete: <T = any>(url: string, config?: AxiosRequestConfig) => Promise<T>;
  /** 透传底层 axios 实例（用于需要 response headers / 自定义拦截器等场景） */
  raw: AxiosInstance;
}

/**
 * 创建带 token 注入的 API 客户端
 * get/post/put/delete 直接返回 response body（已解包 data）
 */
export function createApi(): ApiClient {
  const instance = axios.create();

  // 请求拦截：自动注入 Bearer token
  instance.interceptors.request.use((config) => {
    const accessStore = useAccessStore();
    if (accessStore.accessToken) {
      config.headers.Authorization = `Bearer ${accessStore.accessToken}`;
    }
    return config;
  });

  return {
    get: (url, config) => instance.get(url, config).then((r) => r.data),
    post: (url, data, config) => instance.post(url, data, config).then((r) => r.data),
    put: (url, data, config) => instance.put(url, data, config).then((r) => r.data),
    delete: (url, config) => instance.delete(url, config).then((r) => r.data),
    raw: instance,
  };
}

/**
 * 分页响应解析（mica-admin 后端 IPage 扁平结构）
 * 适配的返回形式（**正常响应无 R 包装**）：
 *   1. IPage<T> 扁平:        { records: [...], total: 0 }
 *   2. Vben 风格:            { list: [...], total: 0 }
 *   3. 纯数组:               [...]
 *
 * 异常响应（被 axios 拦截层抛出）不会进入此函数
 */
export function parsePage<T = any>(payload: unknown): { list: T[]; total: number } {
  if (Array.isArray(payload)) {
    return { list: payload as T[], total: payload.length };
  }
  if (!payload || typeof payload !== 'object') {
    return { list: [], total: 0 };
  }

  const data = payload as Record<string, any>;

  // IPage<T> 扁平（mica-admin 默认）
  if (Array.isArray(data.records)) {
    return {
      list: data.records as T[],
      total: data.total ?? data.records.length,
    };
  }

  // Vben 风格
  if (Array.isArray(data.list)) {
    return {
      list: data.list as T[],
      total: data.total ?? data.list.length,
    };
  }

  return { list: [], total: 0 };
}

/**
 * 提取响应对象中的 data 字段
 * 仅供部分非标接口使用——mica-admin 正常响应通常没有 R 包装
 */
export function pickData<T = any>(payload: unknown): T {
  if (
    payload &&
    typeof payload === 'object' &&
    'data' in payload &&
    (payload as Record<string, unknown>).data !== undefined
  ) {
    return (payload as Record<string, any>).data as T;
  }
  return payload as T;
}

/** 默认共享实例（绝大多数业务接口直接用这个） */
export const api = createApi();
