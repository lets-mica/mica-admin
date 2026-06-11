// 检查 mica-admin-web 数据库脚本中的 sys_menu 记录
// 对比前端 views 目录下实际存在的 .vue 文件
// 使用: node scripts/check-menu-routes.mjs
import { readFileSync, readdirSync, statSync } from 'node:fs'
import { join, relative, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { dirname } from 'node:path'

const __dirname = dirname(fileURLToPath(import.meta.url))
const ROOT = resolve(__dirname, '..')

// 1. 递归收集 views 下所有 .vue 文件
function walkVue(dir, base, out = []) {
  for (const name of readdirSync(dir)) {
    const full = join(dir, name)
    const s = statSync(full)
    if (s.isDirectory()) walkVue(full, base, out)
    else if (name.endsWith('.vue')) out.push(relative(base, full).replace(/\\/g, '/'))
  }
  return out
}
const viewsDir = join(ROOT, 'mica-admin-web/src/views')
const vueFiles = walkVue(viewsDir, viewsDir)
// 同时支持不带 .vue 后缀做匹配
const vueLookup = new Set(vueFiles)
const vueLookupNoExt = new Set(vueFiles.map((f) => f.replace(/\.vue$/, '')))

// 2. 解析 mysql.sql 中 sys_menu 的 INSERT
const sql = readFileSync(join(ROOT, 'docs/database/mysql.sql'), 'utf8')
const insertRe = /INSERT INTO `sys_menu` VALUES (\(.+?\));/g
const tupleRe = /'((?:[^']|'')*)'|NULL|(\d+(?:\.\d+)?)/g

// 字段顺序
const FIELDS = [
  'id', 'parent_id', 'title', 'name', 'seq', 'path', 'permission',
  'component', 'icon', 'is_frame', 'type', 'cache', 'hidden', 'status',
  'created_by', 'created_at', 'updated_by', 'updated_at', 'remark',
]

const rows = []
let m
while ((m = insertRe.exec(sql))) {
  const inside = m[1].slice(1, -1) // 去掉外层括号
  const vals = []
  let vm
  // 按逗号拆分，但要正确处理 SQL 字符串里的逗号
  let buf = ''
  let inStr = false
  for (let i = 0; i < inside.length; i++) {
    const c = inside[i]
    if (c === "'") {
      // 转义 '' -> '
      if (inStr && inside[i + 1] === "'") { buf += "'"; i++; continue }
      inStr = !inStr
      buf += c
      continue
    }
    if (c === ',' && !inStr) {
      vals.push(buf.trim()); buf = ''; continue
    }
    buf += c
  }
  if (buf.length) vals.push(buf.trim())

  const obj = {}
  FIELDS.forEach((k, i) => {
    const v = vals[i]
    if (v === undefined || v === 'NULL' || v === '') obj[k] = null
    else if (v.startsWith("'") && v.endsWith("'")) obj[k] = v.slice(1, -1).replace(/''/g, "'")
    else obj[k] = isNaN(Number(v)) ? v : Number(v)
  })
  rows.push(obj)
}

// 3. 只校验 type=0 / type=1（目录/菜单），type=2（按钮）不管路由
const problems = []
for (const r of rows) {
  if (r.type === 2) continue

  // name 不允许为空
  if (!r.name || !String(r.name).trim()) {
    problems.push(`#${r.id} 菜单 name 为空 (title=${r.title})`)
  }

  // component 校验（支持 xxx/index.vue 约定）
  const comp = r.component
  if (r.type === 1 && comp && comp !== '#') {
    if (r.is_frame !== 1) {
      const withExt = comp.endsWith('.vue') ? comp : `${comp}.vue`
      const indexAlt = comp.endsWith('.vue')
        ? comp.replace(/\.vue$/, '/index.vue')
        : `${comp}/index.vue`
      if (
        !vueLookup.has(withExt) &&
        !vueLookupNoExt.has(comp) &&
        !vueLookup.has(indexAlt)
      ) {
        problems.push(
          `#${r.id} [${r.title}] component='${comp}'  -> views/${withExt} 或 views/${indexAlt} 都不存在`,
        )
      }
    }
  }

  // icon 前缀检查
  if (r.icon && r.icon !== '#' && !r.icon.startsWith('lucide:')) {
    problems.push(
      `#${r.id} [${r.title}] icon='${r.icon}' 不规范，建议改为 lucide:xxx`,
    )
  }

  // 外链 is_frame 必须同时满足 path=http(s)://
  if (r.is_frame === 1 && !/^https?:\/\//.test(r.path || '')) {
    problems.push(`#${r.id} [${r.title}] is_frame=1 但 path='${r.path}' 不以 http(s):// 开头`)
  }
}

// 4. 检查前端 route modules 里有、但 sys_menu 里没有（type=0|1）的菜单
//    尤其是 system 目录下的 views 文件
const menuComponentSet = new Set(
  rows
    .filter((r) => r.type !== 2 && r.component && r.component !== '#')
    .map((r) => (r.component.endsWith('.vue') ? r.component : `${r.component}.vue`)),
)
const routeModulePaths = new Set([
  // 本地 route modules 里定义过但 database 里可能没有的
])
console.log('\n=== views 下存在但 sys_menu type=1 未引用的 .vue 文件 ===')
for (const f of vueFiles) {
  if (f.startsWith('_core/')) continue
  // 允许 xxx/index.vue 与 xxx 形式的 component 匹配
  const alt = f.replace(/\/index\.vue$/, '.vue')
  const noIndex = f.replace(/\/index\.vue$/, '')
  if (
    !menuComponentSet.has(f) &&
    !menuComponentSet.has(alt) &&
    !menuComponentSet.has(noIndex)
  ) {
    // 子视图（如 profile/updatePass.vue）不算"缺失菜单"
    if (/profile\//.test(f) || /message\.vue$/.test(f)) {
      console.log('  - ' + f + '   (子视图，不需要独立路由)')
    } else {
      console.log('  - ' + f + '   (⚠️ views 中存在但 sys_menu 未引用)')
    }
  }
}

// 5. name 唯一性检查
const nameCount = {}
for (const r of rows) {
  if (!r.name) continue
  nameCount[r.name] = (nameCount[r.name] || 0) + 1
}
for (const [n, c] of Object.entries(nameCount)) {
  if (c > 1) problems.push(`路由 name='${n}' 重复出现 ${c} 次`)
}

// 4. 汇总
console.log('\n=== views 目录实际 .vue 文件 ===')
vueFiles.forEach((f) => console.log('  - ' + f))

console.log('\n=== sys_menu 有效记录 (type=0|1) ===')
for (const r of rows) {
  if (r.type === 2) continue
  const pad = (n) => String(n).padStart(4, ' ')
  const flag = `${r.is_frame ? '外链' : '内链'} type=${r.type} cache=${r.cache} hidden=${r.hidden}`
  console.log(
    `  #${pad(r.id)}  parent=${pad(r.parent_id ?? 0)}  name=${r.name}\n` +
    `     path="${r.path}"   component="${r.component}"   icon="${r.icon}"   (${flag})`,
  )
}

console.log('\n=== 潜在问题 ===')
if (problems.length === 0) console.log('  未发现明显不匹配。')
else problems.forEach((p) => console.log('  - ' + p))
console.log('')
