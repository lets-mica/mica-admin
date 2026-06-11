import { api } from '#/api/request';

export interface ServerInfo {
  time: string;
  sys: {
    os: string;
    ip: string;
    day: string;
  };
  cpu: {
    name: string;
    package: string;
    core: string;
    logic: string;
    used: string;
    coreNumber: number;
  };
  memory: {
    total: string;
    used: string;
    available: string;
    usageRate: string;
  };
  swap: {
    total: string;
    used: string;
    available: string;
    usageRate: string;
  };
  disk: {
    total: string;
    used: string;
    available: string;
    usageRate: string;
  };
}

export async function getServerInfo(): Promise<ServerInfo> {
  return api.get<ServerInfo>('/api/system/monitor/server');
}
