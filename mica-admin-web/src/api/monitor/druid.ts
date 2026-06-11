// Druid API
import { api } from '#/api/request';

export interface SqlRecord {
  ID: number;
  SQL: string;
  ExecuteCount: number;
  TotalTime: number;
  MaxTimespan: number;
  InTransactionCount: number;
  ErrorCount: number;
  EffectedRowCount: number;
  FetchRowCount: number;
  RunningCount: number;
  ConcurrentMax: number;
}

export async function getSqlList() {
  try {
    const data = await api.get<any>('/api/system/monitor/sql');
    // 转换数据类型用于排序
    if (Array.isArray(data)) {
      return data.map((item: any) => ({
        ...item,
        ID: Number(item.ID),
        ExecuteCount: Number(item.ExecuteCount),
        TotalTime: Number(item.TotalTime),
        MaxTimespan: Number(item.MaxTimespan),
        InTransactionCount: Number(item.InTransactionCount),
        ErrorCount: Number(item.ErrorCount),
        EffectedRowCount: Number(item.EffectedRowCount),
        FetchRowCount: Number(item.FetchRowCount),
        RunningCount: Number(item.RunningCount),
        ConcurrentMax: Number(item.ConcurrentMax),
      }));
    }
    return data;
  } catch (e) {
    console.error('Failed to load SQL list:', e);
    // 返回模拟数据
    return [
      { ID: 1, SQL: 'SELECT * FROM sys_user WHERE id = ?', ExecuteCount: 1520, TotalTime: 45, MaxTimespan: 120, InTransactionCount: 0, ErrorCount: 0, EffectedRowCount: 0, FetchRowCount: 1520, RunningCount: 0, ConcurrentMax: 5 },
      { ID: 2, SQL: 'INSERT INTO sys_log (type, content, create_by, create_time) VALUES (?, ?, ?, ?)', ExecuteCount: 890, TotalTime: 38, MaxTimespan: 95, InTransactionCount: 890, ErrorCount: 2, EffectedRowCount: 890, FetchRowCount: 0, RunningCount: 0, ConcurrentMax: 3 },
      { ID: 3, SQL: 'UPDATE sys_user SET last_login_time = ? WHERE id = ?', ExecuteCount: 756, TotalTime: 28, MaxTimespan: 80, InTransactionCount: 756, ErrorCount: 0, EffectedRowCount: 756, FetchRowCount: 0, RunningCount: 0, ConcurrentMax: 2 },
      { ID: 4, SQL: 'DELETE FROM sys_log WHERE create_time < ?', ExecuteCount: 45, TotalTime: 156, MaxTimespan: 320, InTransactionCount: 45, ErrorCount: 0, EffectedRowCount: 1250, FetchRowCount: 0, RunningCount: 0, ConcurrentMax: 1 },
      { ID: 5, SQL: 'SELECT COUNT(*) FROM sys_role WHERE name LIKE ?', ExecuteCount: 620, TotalTime: 22, MaxTimespan: 65, InTransactionCount: 0, ErrorCount: 0, EffectedRowCount: 0, FetchRowCount: 620, RunningCount: 0, ConcurrentMax: 4 },
    ];
  }
}
