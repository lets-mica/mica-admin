/**
 * 工作台扩展点入口。
 *
 * 二次开发方在此 import 自己的卡片组件并注册,即可出现在工作台首页。
 *
 * 步骤:
 *   1. 在 ./cards/ 下新增卡片组件 (例如 approval-todo.vue)
 *   2. 在本文件中 import + registerCard
 *   3. App 启动时会在工作台页面渲染该卡片
 *
 * 注意:
 *   - 卡片组件必须 export default 一个 Vue 组件
 *   - 卡片内部自行处理数据拉取与样式,工作台页面只负责布局与排序
 *   - order 越大越靠前;不传则按注册顺序
 *
 * 示例:
 *   ```ts
 *   import { registerCard } from './registry'
 *   import ApprovalTodo from './cards/approval-todo.vue'
 *
 *   registerCard({
 *     id: 'approval-todo',
 *     title: '待我审批',
 *     order: 100,
 *     component: ApprovalTodo
 *   })
 *   ```
 *
 * 当前已注册的卡片:无
 */
import { registerCard } from './registry'

// 在此处添加 registerCard 调用以挂载业务卡片
// 例: registerCard({ id: 'xxx', title: 'xxx', order: 100, component: XxxCard })

export { registerCard } from './registry'
export type { WorkbenchCard } from './registry'
