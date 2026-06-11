import { api, parsePage } from '#/api/request';

export interface TokenItem {
  key: string;
  summary: string;
  userName: string;
  nickName: string;
  dept: string;
  ip: string;
  address: string;
  browser: string;
  loginTime: string;
}

export async function getTokenList(params?: {
  page?: number;
  size?: number;
  filter?: string;
}) {
  const data = await api.get<any>('/api/auth/token', { params });
  return parsePage<TokenItem>(data);
}

export async function delToken(keys: string[]) {
  return api.delete('/api/auth/token', { data: keys });
}
