/**
 * 偏好设置 ↔ sys_config KV 编解码工具
 *
 * 后端约定：
 *   - sys_config.field 使用点分路径存储，例如 preference.theme.colorPrimary
 *   - sys_config.value 是字符串叶子值（boolean/number 会被序列化）
 *
 * 前端偏好结构（Preferences）嵌套很深，本工具负责：
 *   flattenPreferences(obj)      -> [{ field, value }]   写 DB 用
 *   unflattenPreferences(rows)    -> object               读 DB 用
 */

const FIELD_PREFIX = 'preference.';

/**
 * 把字符串叶子值还原为 boolean / number / 字符串。
 * 与 defaultPreferences 里的字面量类型对齐。
 */
function castLeaf(value: string): unknown {
  if (value === 'true') return true;
  if (value === 'false') return false;
  if (value === '') return '';
  if (/^-?\d+(\.\d+)?$/.test(value)) {
    const num = Number(value);
    if (Number.isFinite(num)) return num;
  }
  return value;
}

/**
 * 把偏好对象扁平化为 {field, value} 列表，仅产出叶子节点。
 * 跳过 undefined / function / symbol 值。
 */
export function flattenPreferences(
  obj: Record<string, any> | null | undefined,
  prefix: string = FIELD_PREFIX,
): Array<{ field: string; value: string }> {
  const out: Array<{ field: string; value: string }> = [];
  if (!obj || typeof obj !== 'object') return out;

  const walk = (node: unknown, path: string[]) => {
    if (node === null || node === undefined) {
      out.push({ field: prefix + path.join('.'), value: '' });
      return;
    }
    if (Array.isArray(node)) {
      out.push({ field: prefix + path.join('.'), value: JSON.stringify(node) });
      return;
    }
    const t = typeof node;
    if (t === 'string' || t === 'number' || t === 'boolean') {
      out.push({ field: prefix + path.join('.'), value: String(node) });
      return;
    }
    if (t !== 'object') return;
    const entries = Object.entries(node as Record<string, unknown>);
    if (entries.length === 0) {
      out.push({ field: prefix + path.join('.'), value: '{}' });
      return;
    }
    for (const [k, v] of entries) walk(v, [...path, k]);
  };

  for (const [k, v] of Object.entries(obj)) walk(v, [k]);
  return out;
}

/**
 * 把 sys_config 行（[{field, value}] 或 Map）展开为嵌套对象。
 * 非 preference.* 前缀的字段会被忽略。
 */
export function unflattenPreferences(
  rows: Array<{ field: string; value: string }> | Record<string, string> | null | undefined,
): Record<string, any> {
  const out: Record<string, any> = {};
  if (!rows) return out;

  const iter: Iterable<[string, string]> = Array.isArray(rows)
    ? (rows
        .filter((r) => r && typeof r.field === 'string')
        .map((r) => [r.field, r.value ?? ''] as [string, string]))
    : Object.entries(rows);

  for (const [field, rawValue] of iter) {
    if (!field.startsWith(FIELD_PREFIX)) continue;
    const path = field.slice(FIELD_PREFIX.length).split('.');
    if (path.length === 0 || (path.length === 1 && path[0] === '')) continue;

    let cursor: Record<string, any> = out;
    for (let i = 0; i < path.length; i++) {
      const seg = path[i];
      const isLast = i === path.length - 1;
      if (isLast) {
        cursor[seg] = castLeaf(rawValue);
      } else {
        if (cursor[seg] == null || typeof cursor[seg] !== 'object') {
          cursor[seg] = {};
        }
        cursor = cursor[seg];
      }
    }
  }
  return out;
}

/**
 * 浅深合并：以后者为准，覆盖前者同名叶子；嵌套对象会递归合并。
 */
export function mergePreferences<T extends Record<string, any>>(
  base: T,
  override: Record<string, any> | null | undefined,
): T {
  if (!override) return base;
  const out: Record<string, any> = { ...base };
  for (const [k, v] of Object.entries(override)) {
    const baseVal = out[k];
    if (
      baseVal &&
      typeof baseVal === 'object' &&
      !Array.isArray(baseVal) &&
      v &&
      typeof v === 'object' &&
      !Array.isArray(v)
    ) {
      out[k] = mergePreferences(baseVal, v);
    } else {
      out[k] = v;
    }
  }
  return out as T;
}