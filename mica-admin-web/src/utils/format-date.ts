/**
 * 统一的日期/时间格式化工具
 *
 * 基于 @vben-core/shared/utils 的 dayjs 实现
 * 全项目统一使用以下格式：
 *  - date        : YYYY-MM-DD          (仅日期，如 2026-06-01)
 *  - dateTime    : YYYY-MM-DD HH:mm:ss (完整时间，如 2026-06-01 14:30:00)
 *  - time        : HH:mm:ss            (仅时间，如 14:30:00)
 *  - monthDay    : MM-DD HH:mm         (月日时分，用于列表紧凑显示)
 *  - month       : YYYY-MM             (年月)
 */

import dayjs from 'dayjs';
import { formatDate as vbenFormatDate } from '@vben-core/shared/utils';

/** 标准日期格式：YYYY-MM-DD */
export const DATE_FORMAT = 'YYYY-MM-DD';
/** 标准时间格式：YYYY-MM-DD HH:mm:ss */
export const DATETIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';
/** 仅时间：HH:mm:ss */
export const TIME_FORMAT = 'HH:mm:ss';
/** 月日时分：MM-DD HH:mm */
export const MONTH_DAY_FORMAT = 'MM-DD HH:mm';
/** 年月：YYYY-MM */
export const MONTH_FORMAT = 'YYYY-MM';

/**
 * 格式化日期
 * @param value 时间值（Date / 数字时间戳 / 字符串 / dayjs 对象）
 * @param format 格式字符串，默认 YYYY-MM-DD HH:mm:ss
 * @returns 格式化后的字符串，无效值返回空串
 */
export function formatDate(
  value?: dayjs.ConfigType,
  format: string = DATETIME_FORMAT,
): string {
  if (value === undefined || value === null || value === '') return '';
  return vbenFormatDate(value as any, format as any);
}

/** 快捷：仅日期 YYYY-MM-DD */
export function formatDateOnly(value?: dayjs.ConfigType): string {
  return formatDate(value, DATE_FORMAT);
}

/** 快捷：完整日期时间 YYYY-MM-DD HH:mm:ss */
export function formatDateTime(value?: dayjs.ConfigType): string {
  return formatDate(value, DATETIME_FORMAT);
}

/** 快捷：紧凑格式 MM-DD HH:mm */
export function formatMonthDay(value?: dayjs.ConfigType): string {
  return formatDate(value, MONTH_DAY_FORMAT);
}

/**
 * 表格列渲染辅助：直接传入 value 即可
 * 用法：{ title: '创建时间', key: 'createdAt', render: dateCell }
 */
export function dateCell(row: any, _key: string, format: string = DATETIME_FORMAT) {
  return formatDate(row?.[_key] ?? row, format);
}

/**
 * 友好相对时间（如：3 分钟前 / 2 小时前 / 昨天 / 5 天前）
 * 可在列表"列表项 - 元信息"中使用
 */
export function formatRelative(value?: dayjs.ConfigType): string {
  if (!value) return '';
  const target = dayjs(value);
  if (!target.isValid()) return '';
  const diffMs = Date.now() - target.valueOf();
  if (diffMs < 60_000) return '刚刚';
  if (diffMs < 3_600_000) return `${Math.floor(diffMs / 60_000)} 分钟前`;
  if (diffMs < 86_400_000) return `${Math.floor(diffMs / 3_600_000)} 小时前`;
  if (diffMs < 7 * 86_400_000) return `${Math.floor(diffMs / 86_400_000)} 天前`;
  return formatDateOnly(value);
}

export { dayjs };
