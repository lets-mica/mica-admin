-- ========================================================
-- mica-admin-web 切换为"后端动态路由"前，对 sys_menu 的整改清单
-- 说明：
--   1) 前端 views/ 目录下 .vue 文件为唯一真源；database 里 sys_menu.component
--      必须能精确映射到其中一个文件（支持 xxx.vue 与 xxx/index.vue 两种约定）。
--   2) icon 统一使用 lucide:xxx 前缀；不再沿用若依那批 element-ui 老图标。
--   3) name 字段是路由 name（前端 router.addRoute 用），父子菜单必须互不相同。
--   4) is_frame=1 时 path 必须以 http(s):// 开头；is_frame=0 且 path=http://
--      会被当作普通路由，导致 404。
--   5) 顶级目录 (parent_id is null / 0) 的 component 应为空，由 MenuVoUtil
--      自动填 'Layout'；其子菜单 component 写相对 views 的路径。
--
-- 执行方式：mysql -uroot -p mica_admin < docs/database/fix-menu.sql
-- 或在 Navicat/DBeaver 里全选运行。
-- ========================================================

-- --------------------------------------------------------------------
-- A. icon 统一改为 lucide:xxx（原先沿用若依老图标，不被前端识别，会 fallback）
-- --------------------------------------------------------------------
UPDATE sys_menu SET icon = 'lucide:settings'          WHERE id = 1;      -- 系统管理
UPDATE sys_menu SET icon = 'lucide:monitor'            WHERE id = 2;      -- 系统监控
UPDATE sys_menu SET icon = 'lucide:wrench'             WHERE id = 3;      -- 系统工具
UPDATE sys_menu SET icon = 'lucide:globe'              WHERE id = 4;      -- 如梦官网（外链）
UPDATE sys_menu SET icon = 'lucide:users'              WHERE id = 100;    -- 用户管理
UPDATE sys_menu SET icon = 'lucide:shield-check'       WHERE id = 101;    -- 角色管理
UPDATE sys_menu SET icon = 'lucide:menu'               WHERE id = 102;    -- 菜单管理
UPDATE sys_menu SET icon = 'lucide:git-branch'         WHERE id = 103;    -- 部门管理
UPDATE sys_menu SET icon = 'lucide:briefcase'          WHERE id = 104;    -- 岗位管理
UPDATE sys_menu SET icon = 'lucide:bookmark'           WHERE id = 105;    -- 字典管理
UPDATE sys_menu SET icon = 'lucide:sliders-horizontal' WHERE id = 106;    -- 参数设置
UPDATE sys_menu SET icon = 'lucide:bell'               WHERE id = 107;    -- 通知公告
-- 108 Oss, 109~113 Token/Online/Druid/Server/Redis, 115 Swagger, 116 Analytics
-- 这些原本就是 lucide:xxx，不动。

-- --------------------------------------------------------------------
-- B. 修复 component / 冗余节点 / 角色菜单表孤引用
-- --------------------------------------------------------------------
-- #117 [工作台] -> views/dashboard/workspace/index.vue 不存在，直接删除
DELETE FROM sys_menu WHERE id = 117;
-- 同步清理 sys_role_menu 中可能存在的孤儿引用
DELETE FROM sys_role_menu WHERE menu_id = 117;

-- #121 [图标选择器] -> component='components/icons' 需写全路径
--    (前端 access.ts 已兼容 xxx 与 xxx/index.vue，数据库写全路径更稳妥)
UPDATE sys_menu
SET component = 'components/icons/index'
WHERE id = 121;

-- 新的"消息中心"菜单节点也需要同步到普通角色的授权表（超管无需设置）
--    先避免重复插入再插入
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 122);

-- --------------------------------------------------------------------
-- C. 补上"消息中心"菜单：views/system/message/index.vue 有但 sys_menu 漏了
-- --------------------------------------------------------------------
-- parent=1(系统管理), seq=8.5 放在 notice 与 oss 之间(都已是 8 和 9)，
-- 实际 oss 已是 9，这里直接把 seq 调成 [notice=8, message=8.5 不行，int]
-- 方案：把 oss 后移一位，message 塞到 9；或 message seq=10 放最后。
-- 选后者（最小改动）。
INSERT INTO sys_menu
  (id, parent_id, title, name, seq, path, permission, component,
   icon, is_frame, type, cache, hidden, status,
   created_by, created_at, updated_by, updated_at, remark)
VALUES
  (122, 1, '消息中心', 'SystemMessage', 10, 'message', 'system:message:list',
   'system/message/index', 'lucide:mail', 0, 1, 0, 0, 0,
   'admin', NOW(), 'admin', NOW(), '系统管理/消息中心');

-- --------------------------------------------------------------------
-- D. 顶部"仪表盘"有两个子菜单（Analytics / Workspace），但 workspace 的
--    views 文件不存在。上面 B 已把它指向 analytics/index。
--    这里补充：Analytics 作为 Dashboard 的"默认首页"，设置 redirect
--    指向 analytics（其实 MenuVoUtil 没读 redirect，这里仅作文档。）
-- --------------------------------------------------------------------

-- --------------------------------------------------------------------
-- E. dashboard/analytics 目录里还有 analytics-visits 等 4 个子文件，它们是
--    analytics/index.vue 内部 <router-view> 调用的"子视图组件"，不应作为
--    独立菜单项。保持现状即可，无需在 sys_menu 中新增。
-- --------------------------------------------------------------------

-- --------------------------------------------------------------------
-- F. profile / message 子页（system/user/profile/index.vue、
--    system/user/message.vue）作为"个人中心" / "站内信详情"，是 user 菜单
--    的子路由，不是菜单树节点。保持现状。
--    如果想在左侧菜单上给一个"个人中心"入口，可以仿照如下 INSERT：
--      INSERT INTO sys_menu (...) VALUES (..., 'user/profile',
--        'system/user/profile/index', 'lucide:user-round', 0, 1, 0, 0, 0, ...);
--    但一般项目把它放右上角头像下拉，这里不强制加。
-- --------------------------------------------------------------------

-- --------------------------------------------------------------------
-- G. 校验：确保所有 type=1 的菜单 component 都能在 views 中命中
--   SELECT id, title, component FROM sys_menu WHERE type = 1
--      AND component NOT IN (
--        'system/user/index','system/role/index','system/menu/index',
--        'system/dept/index','system/post/index','system/dict/index',
--        'system/config/index','system/notice/index','system/oss/index',
--        'system/message/index',
--        'monitor/token/index','monitor/online/index','monitor/druid/index',
--        'monitor/server/index','monitor/redis/index',
--        'monitor/log/index','monitor/log/errorLog',
--        'tools/swagger/index',
--        'dashboard/analytics/index',
--        'components/ECharts','components/Editor','components/MarkDown',
--        'components/icons/index'
--      );
--   若返回 0 行，说明 component 映射已经全对。
-- --------------------------------------------------------------------

-- --------------------------------------------------------------------
-- H. IM 模块菜单（PR-1.1.1 读 + PR-1.1.2 写）
--
-- 菜单结构：
--   IM 通讯（id=2000, 顶级目录）
--     消息中心   （id=2001, im/index）
--     群管理     （id=2002, im/group/index）
--       群查询    （id=2003, 按钮）
--       创建群    （id=2004, 按钮）
--       邀请成员  （id=2005, 按钮）
--       移除成员  （id=2006, 按钮）
--       解散群    （id=2007, 按钮）
-- --------------------------------------------------------------------

-- 顶级目录：IM 通讯（icon 用 lucide:message-circle）
INSERT IGNORE INTO sys_menu
  (id, parent_id, title, name, seq, path, permission, component,
   icon, is_frame, type, cache, hidden, status,
   created_by, created_at, updated_by, updated_at, remark)
VALUES
  (2000, NULL, 'IM 通讯', 'Im', 7, 'im', NULL, NULL,
   'lucide:message-circle', 0, 0, 0, 0, 0,
   'admin', NOW(), 'admin', NOW(), 'IM 即时通讯模块');

-- 子菜单：消息中心（会话列表 / 聊天界面，views/im/index.vue）
INSERT IGNORE INTO sys_menu
  (id, parent_id, title, name, seq, path, permission, component,
   icon, is_frame, type, cache, hidden, status,
   created_by, created_at, updated_by, updated_at, remark)
VALUES
  (2001, 2000, '消息中心', 'ImConversation', 1, 'conversation', 'im:conversation:list',
   'im/index', 'lucide:message-square', 0, 1, 0, 0, 0,
   'admin', NOW(), 'admin', NOW(), 'IM 通讯/消息中心');

-- 子菜单：群管理（群 CRUD，views/im/group/index.vue，PR-1.1.2）
INSERT IGNORE INTO sys_menu
  (id, parent_id, title, name, seq, path, permission, component,
   icon, is_frame, type, cache, hidden, status,
   created_by, created_at, updated_by, updated_at, remark)
VALUES
  (2002, 2000, '群管理', 'ImGroup', 2, 'group', 'im:group:list',
   'im/group/index', 'lucide:users', 0, 1, 0, 0, 0,
   'admin', NOW(), 'admin', NOW(), 'IM 通讯/群管理');

-- 群管理按钮权限
INSERT IGNORE INTO sys_menu (id, parent_id, title, name, seq, path, permission, component, icon, is_frame, type, cache, hidden, status, created_by, created_at, updated_by, updated_at, remark)
VALUES
  (2003, 2002, '群查询', 'ImGroupQuery', 1, '#', 'im:group:query', NULL, '#', 0, 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT IGNORE INTO sys_menu (id, parent_id, title, name, seq, path, permission, component, icon, is_frame, type, cache, hidden, status, created_by, created_at, updated_by, updated_at, remark)
VALUES
  (2004, 2002, '创建群', 'ImGroupAdd', 2, '#', 'im:group:add', NULL, '#', 0, 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT IGNORE INTO sys_menu (id, parent_id, title, name, seq, path, permission, component, icon, is_frame, type, cache, hidden, status, created_by, created_at, updated_by, updated_at, remark)
VALUES
  (2005, 2002, '邀请成员', 'ImGroupInvite', 3, '#', 'im:group:invite', NULL, '#', 0, 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT IGNORE INTO sys_menu (id, parent_id, title, name, seq, path, permission, component, icon, is_frame, type, cache, hidden, status, created_by, created_at, updated_by, updated_at, remark)
VALUES
  (2006, 2002, '移除成员', 'ImGroupRemoveMember', 4, '#', 'im:group:remove', NULL, '#', 0, 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT IGNORE INTO sys_menu (id, parent_id, title, name, seq, path, permission, component, icon, is_frame, type, cache, hidden, status, created_by, created_at, updated_by, updated_at, remark)
VALUES
  (2007, 2002, '解散群', 'ImGroupDismiss', 5, '#', 'im:group:dismiss', NULL, '#', 0, 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');

-- 给普通角色（role_id=2）授权所有 IM 菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
  (2, 2000), (2, 2001), (2, 2002), (2, 2003), (2, 2004), (2, 2005), (2, 2006), (2, 2007);
