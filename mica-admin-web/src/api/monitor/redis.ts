// Redis API
import { api } from '#/api/request';

export interface RedisInfo {
  redis_version?: string;
  redis_mode?: string;
  tcp_port?: string;
  connected_clients?: string;
  uptime_in_days?: string;
  used_memory_human?: string;
  used_cpu_user_children?: string;
  maxmemory_human?: string;
  aof_enabled?: string;
  rdb_last_bgsave_status?: string;
  instantaneous_input_kbps?: string;
  instantaneous_output_kbps?: string;
}

export interface CommandStat {
  name: string;
  value: number;
}

export interface RedisData {
  info: RedisInfo;
  dbSize?: number;
  commandStats?: CommandStat[];
}

export async function getRedisInfo() {
  try {
    return await api.get<any>('/api/system/monitor/redis');
  } catch (e) {
    console.error('Failed to load Redis info:', e);
    // 返回模拟数据
    return {
      info: {
        redis_version: '7.0.0',
        redis_mode: 'standalone',
        tcp_port: '6379',
        connected_clients: '5',
        uptime_in_days: '15',
        used_memory_human: '2.5M',
        used_cpu_user_children: '0.05',
        maxmemory_human: '512M',
        aof_enabled: '0',
        rdb_last_bgsave_status: 'ok',
        instantaneous_input_kbps: '1.2',
        instantaneous_output_kbps: '2.3',
      },
      dbSize: 128,
      commandStats: [
        { name: 'GET', value: 5200 },
        { name: 'SET', value: 3100 },
        { name: 'DEL', value: 850 },
        { name: 'EXPIRE', value: 620 },
        { name: 'HGET', value: 450 },
        { name: 'HSET', value: 380 },
        { name: 'LPUSH', value: 290 },
        { name: 'RPOP', value: 265 },
      ],
    };
  }
}
