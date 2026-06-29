-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数名称',
  `field` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键名',
  `value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键值',
  `is_system` tinyint(1) NULL DEFAULT 0 COMMENT '系统内置（0否1是 ）',
  `created_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `updated_at` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_field`(`field`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '参数配置表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 1, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '初始化密码 123456');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '祖级列表',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `seq` int(11) NULL DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '部门状态（0停用 1正常）',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标志（0代表正常 1代表删除）',
  `created_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `updated_at` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '部门表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (100, 0, '0', '如梦技术', 0, 'admin', '15888888888', '596392912@qq.com', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00');
INSERT INTO `sys_dept` VALUES (101, 100, '0,100', '深圳总公司', 1, 'admin', '15888888888', '596392912@qq.com', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00');
INSERT INTO `sys_dept` VALUES (102, 100, '0,100', '长沙分公司', 2, 'admin', '15888888888', '596392912@qq.com', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00');
INSERT INTO `sys_dept` VALUES (103, 101, '0,100,101', '研发部门', 1, 'admin', '15888888888', '596392912@qq.com', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00');
INSERT INTO `sys_dept` VALUES (104, 101, '0,100,101', '市场部门', 2, 'admin', '15888888888', '596392912@qq.com', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00');
INSERT INTO `sys_dept` VALUES (105, 101, '0,100,101', '测试部门', 3, 'admin', '15888888888', '596392912@qq.com', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00');
INSERT INTO `sys_dept` VALUES (106, 101, '0,100,101', '财务部门', 4, 'admin', '15888888888', '596392912@qq.com', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00');
INSERT INTO `sys_dept` VALUES (107, 101, '0,100,101', '运维部门', 5, 'admin', '15888888888', '596392912@qq.com', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00');
INSERT INTO `sys_dept` VALUES (108, 102, '0,100,102', '市场部门', 1, 'admin', '15888888888', '596392912@qq.com', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00');
INSERT INTO `sys_dept` VALUES (109, 102, '0,100,102', '财务部门', 2, 'admin', '15888888888', '596392912@qq.com', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00');

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `description` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典名称',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `created_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `updated_at` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES (1, 'sys_user_sex', '用户性别', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '用户性别列表');
INSERT INTO `sys_dict` VALUES (2, 'sys_show_hide', '菜单状态', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '菜单状态列表');
INSERT INTO `sys_dict` VALUES (3, 'sys_normal_disable', '系统开关', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '系统开关列表');
INSERT INTO `sys_dict` VALUES (4, 'sys_job_status', '任务状态', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '任务状态列表');
INSERT INTO `sys_dict` VALUES (5, 'sys_job_group', '任务分组', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '任务分组列表');
INSERT INTO `sys_dict` VALUES (6, 'sys_yes_no', '系统是否', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '系统是否列表');
INSERT INTO `sys_dict` VALUES (7, 'sys_notice_type', '通知类型', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '通知类型列表');
INSERT INTO `sys_dict` VALUES (8, 'sys_notice_status', '通知状态', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '通知状态列表');
INSERT INTO `sys_dict` VALUES (9, 'sys_oper_type', '操作类型', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '操作类型列表');
INSERT INTO `sys_dict` VALUES (10, 'sys_common_status', '系统状态', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '登录状态列表');
INSERT INTO `sys_dict` VALUES (11, 'sys_common_enabled', '启用状态', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '通用启用状态');
INSERT INTO `sys_dict` VALUES (12, 'sys_jobs_state', '任务状态', 0, 'admin', '2021-06-11 18:31:40', 'admin', '2021-06-11 18:31:40', NULL);

-- ----------------------------
-- Table structure for sys_dict_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_info`;
CREATE TABLE `sys_dict_info`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `seq` int(11) NULL DEFAULT 0 COMMENT '字典排序',
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典标签',
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典键值',
  `type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认（0否1是 ）',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
  `created_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `updated_at` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典详情表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sys_dict_info
-- ----------------------------
INSERT INTO `sys_dict_info` VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '性别男');
INSERT INTO `sys_dict_info` VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '性别女');
INSERT INTO `sys_dict_info` VALUES (3, 1, '显示', '0', 'sys_show_hide', '', 'primary', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '显示菜单');
INSERT INTO `sys_dict_info` VALUES (4, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '隐藏菜单');
INSERT INTO `sys_dict_info` VALUES (5, 1, '启用', '0', 'sys_normal_disable', '', 'primary', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '正常状态');
INSERT INTO `sys_dict_info` VALUES (6, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '停用状态');
INSERT INTO `sys_dict_info` VALUES (7, 1, '正常', '0', 'sys_job_status', '', 'primary', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '正常状态');
INSERT INTO `sys_dict_info` VALUES (8, 2, '暂停', '1', 'sys_job_status', '', 'danger', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '停用状态');
INSERT INTO `sys_dict_info` VALUES (9, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '默认分组');
INSERT INTO `sys_dict_info` VALUES (10, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '系统分组');
INSERT INTO `sys_dict_info` VALUES (11, 1, '是', '1', 'sys_yes_no', '', 'primary', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '系统默认是');
INSERT INTO `sys_dict_info` VALUES (12, 2, '否', '0', 'sys_yes_no', '', 'danger', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '系统默认否');
INSERT INTO `sys_dict_info` VALUES (13, 1, '通知', '1', 'sys_notice_type', '', 'warning', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '通知');
INSERT INTO `sys_dict_info` VALUES (14, 2, '公告', '2', 'sys_notice_type', '', 'success', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '公告');
INSERT INTO `sys_dict_info` VALUES (15, 1, '正常', '0', 'sys_notice_status', '', 'primary', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '正常状态');
INSERT INTO `sys_dict_info` VALUES (16, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '关闭状态');
INSERT INTO `sys_dict_info` VALUES (17, 1, '新增', '1', 'sys_oper_type', '', 'info', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '新增操作');
INSERT INTO `sys_dict_info` VALUES (18, 2, '修改', '2', 'sys_oper_type', '', 'info', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '修改操作');
INSERT INTO `sys_dict_info` VALUES (19, 3, '删除', '3', 'sys_oper_type', '', 'danger', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '删除操作');
INSERT INTO `sys_dict_info` VALUES (20, 4, '授权', '4', 'sys_oper_type', '', 'primary', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '授权操作');
INSERT INTO `sys_dict_info` VALUES (21, 5, '导出', '5', 'sys_oper_type', '', 'warning', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '导出操作');
INSERT INTO `sys_dict_info` VALUES (22, 6, '导入', '6', 'sys_oper_type', '', 'warning', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '导入操作');
INSERT INTO `sys_dict_info` VALUES (23, 7, '强退', '7', 'sys_oper_type', '', 'danger', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '强退操作');
INSERT INTO `sys_dict_info` VALUES (24, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '生成操作');
INSERT INTO `sys_dict_info` VALUES (25, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '清空操作');
INSERT INTO `sys_dict_info` VALUES (26, 1, '成功', '0', 'sys_common_status', '', 'primary', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '正常状态');
INSERT INTO `sys_dict_info` VALUES (27, 2, '失败', '1', 'sys_common_status', '', 'danger', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '停用状态');
INSERT INTO `sys_dict_info` VALUES (28, 1, '停用', '0', 'sys_common_enabled', '', 'danger', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '停用状态');
INSERT INTO `sys_dict_info` VALUES (29, 2, '启用', '1', 'sys_common_enabled', '', 'primary', 1, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '启用状态');
INSERT INTO `sys_dict_info` VALUES (30, 1, '等待运行', 'WAITING', 'sys_jobs_state', '', 'danger', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '停用状态');
INSERT INTO `sys_dict_info` VALUES (31, 1, '运行中', 'ACQUIRED', 'sys_jobs_state', '', 'danger', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '停用状态');
INSERT INTO `sys_dict_info` VALUES (32, 1, '暂停', 'PAUSED', 'sys_jobs_state', '', 'danger', 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '停用状态');

-- ----------------------------
-- Table structure for sys_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户id',
  `user_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登陆名',
  `log_type` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '日志类别',
  `description` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `params` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '请求参数',
  `data` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '请求数据',
  `successful` tinyint(1) NULL DEFAULT NULL COMMENT '是否成功[0失败,1成功]',
  `class_method` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '类-方法',
  `exception_detail` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '异常信息',
  `request_ip` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求ip',
  `request_time` bigint(20) NULL DEFAULT NULL COMMENT '请求耗时',
  `os` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '系统信息',
  `browser` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '浏览器信息',
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '地址',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '系统日志' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父菜单ID',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '菜单标题',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '菜单名称',
  `seq` int(11) NULL DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '路由地址',
  `permission` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  `component` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `is_frame` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为外链（0否 1是）',
  `type` tinyint(4) NOT NULL DEFAULT 2 COMMENT '菜单类型（0目录 1菜单 2按钮）',
  `cache` tinyint(1) NOT NULL DEFAULT 0 COMMENT '缓存（0否 1是）',
  `hidden` tinyint(1) NOT NULL DEFAULT 0 COMMENT '显示状态（0显示，1隐藏）',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '菜单状态（0正常 1停用）',
  `created_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `updated_at` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单权限表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
-- 一级目录
INSERT INTO `sys_menu` VALUES (1, NULL, '系统管理', 'System', 1, 'system', '', NULL, 'lucide:settings', 0, 0, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '系统管理目录');
INSERT INTO `sys_menu` VALUES (2, NULL, '系统监控', 'Monitor', 2, 'monitor', '', NULL, 'lucide:monitor', 0, 0, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '系统监控目录');
INSERT INTO `sys_menu` VALUES (3, NULL, '系统工具', 'Tools', 3, 'tool', '', NULL, 'lucide:wrench', 0, 0, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '系统工具目录');
INSERT INTO `sys_menu` VALUES (4, NULL, '如梦官网', 'DreamluSite', 4, 'https://www.dreamlu.net', '', NULL, 'lucide:globe', 1, 0, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '若依官网地址');
-- 一级目录（前端有，数据库补齐）
INSERT INTO `sys_menu` VALUES (5, NULL, '仪表盘', 'Dashboard', 0, 'dashboard', '', NULL, 'lucide:layout-dashboard', 0, 0, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '仪表盘目录');
INSERT INTO `sys_menu` VALUES (6, NULL, '公共组件', 'Components', 5, 'components', '', NULL, 'lucide:layout-grid', 0, 0, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '公共组件目录');
-- 系统管理（parent_id=1）
INSERT INTO `sys_menu` VALUES (100, 1, '用户管理', 'User', 1, 'user', 'system:user:list', 'system/user/index', 'lucide:users', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '用户管理菜单');
INSERT INTO `sys_menu` VALUES (101, 1, '角色管理', 'Role', 2, 'role', 'system:role:list', 'system/role/index', 'lucide:shield-check', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '角色管理菜单');
INSERT INTO `sys_menu` VALUES (102, 1, '菜单管理', 'Menu', 3, 'menu', 'system:menu:list', 'system/menu/index', 'lucide:menu', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '菜单管理菜单');
INSERT INTO `sys_menu` VALUES (103, 1, '部门管理', 'Dept', 4, 'dept', 'system:dept:list', 'system/dept/index', 'lucide:git-branch', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '部门管理菜单');
INSERT INTO `sys_menu` VALUES (104, 1, '岗位管理', 'Post', 5, 'post', 'system:post:list', 'system/post/index', 'lucide:briefcase', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '岗位管理菜单');
INSERT INTO `sys_menu` VALUES (105, 1, '字典管理', 'Dict', 6, 'dict', 'system:dict:list', 'system/dict/index', 'lucide:bookmark', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '字典管理菜单');
INSERT INTO `sys_menu` VALUES (106, 1, '参数设置', 'Config', 7, 'config', 'system:config:list', 'system/config/index', 'lucide:sliders-horizontal', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '参数设置菜单');
INSERT INTO `sys_menu` VALUES (107, 1, '通知公告', 'Notice', 8, 'notice', 'system:notice:list', 'system/notice/index', 'lucide:bell', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '通知公告菜单');
-- 系统管理 -> OSS存储（前端有，数据库补齐）
INSERT INTO `sys_menu` VALUES (108, 1, 'OSS存储', 'Oss', 9, 'oss', 'system:oss:list', 'system/oss/index', 'lucide:folder-open', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', 'OSS存储菜单');
-- 系统监控（parent_id=2）
INSERT INTO `sys_menu` VALUES (109, 2, 'Token管理', 'Token', 1, 'token', 'monitor:token:list', 'monitor/token/index', 'lucide:key-round', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', 'Token管理菜单');
INSERT INTO `sys_menu` VALUES (110, 2, '在线用户', 'Online', 2, 'online', 'monitor:online:list', 'monitor/online/index', 'lucide:users', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '在线用户菜单');
INSERT INTO `sys_menu` VALUES (111, 2, 'Druid监控', 'Druid', 3, 'druid', 'monitor:druid:list', 'monitor/druid/index', 'lucide:database', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', 'Druid监控菜单');
INSERT INTO `sys_menu` VALUES (112, 2, '服务监控', 'Server', 4, 'server', 'monitor:server:list', 'monitor/server/index', 'lucide:server', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '服务监控菜单');
INSERT INTO `sys_menu` VALUES (113, 2, 'Redis监控', 'Redis', 5, 'redis', 'monitor:redis:list', 'monitor/redis/index', 'lucide:database', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', 'Redis监控菜单');
-- 系统工具（parent_id=3）
-- 表单构建 (id=114) 已废弃，前端页面 tools/build/index.vue 已删除
INSERT INTO `sys_menu` VALUES (115, 3, '系统接口', 'Swagger', 1, 'swagger', 'tool:swagger:list', 'tools/swagger/index', 'lucide:book', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '系统接口菜单');
-- 仪表盘子项（parent_id=5）
INSERT INTO `sys_menu` VALUES (116, 5, '分析页', 'Analytics', 1, 'analytics', 'dashboard:analytics:list', 'dashboard/analytics/index', 'lucide:area-chart', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '分析页菜单');
-- 工作台（id=117）前端暂无独立视图，暂不挂出
-- 公共组件子项（parent_id=6）
INSERT INTO `sys_menu` VALUES (118, 6, '图表组件', 'ECharts', 1, 'echarts', 'components:echarts:list', 'components/ECharts', 'lucide:pie-chart', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '图表组件');
INSERT INTO `sys_menu` VALUES (119, 6, '富文本编辑器', 'Editor', 2, 'editor', 'components:editor:list', 'components/Editor', 'lucide:file-text', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '富文本编辑器');
INSERT INTO `sys_menu` VALUES (120, 6, 'Markdown编辑器', 'Markdown', 3, 'markdown', 'components:markdown:list', 'components/MarkDown', 'lucide:file-code', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', 'Markdown编辑器');
INSERT INTO `sys_menu` VALUES (121, 6, '图标选择器', 'Icons', 4, 'icons', 'components:icons:list', 'components/icons/index', 'lucide:smile', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '图标选择器');
-- 系统管理 - 消息中心（前端 views/system/message/index.vue 已存在，补齐菜单节点）
INSERT INTO `sys_menu` VALUES (122, 1, '消息中心', 'SystemMessage', 10, 'message', 'system:message:list', 'system/message/index', 'lucide:mail', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '消息中心菜单');
-- 系统管理 - 任务管理（@SysJob 配套页面 views/system/job/index.vue）
INSERT INTO `sys_menu` VALUES (123, 1, '任务管理', 'Job', 11, 'job', 'system:job:list', 'system/job/index', 'lucide:alarm-clock', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '任务管理菜单');
-- 任务管理 - 按钮权限
INSERT INTO `sys_menu` VALUES (1052, 123, '任务查询', 'JobQuery', 1, '#', 'system:job:query', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1053, 123, '任务新增', 'JobAdd', 2, '#', 'system:job:add', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1054, 123, '任务修改', 'JobEdit', 3, '#', 'system:job:edit', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1055, 123, '任务删除', 'JobRemove', 4, '#', 'system:job:remove', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1056, 123, '任务导出', 'JobExport', 5, '#', 'system:job:export', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
-- 系统监控 - 日志（parent_id=2）
INSERT INTO `sys_menu` VALUES (500, 2, '操作日志', 'Log', 6, 'log', 'monitor:operlog:list', 'monitor/log/index', 'lucide:file-text', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '操作日志菜单');
INSERT INTO `sys_menu` VALUES (501, 2, '异常日志', 'ErrorLog', 7, 'log/error', 'monitor:logininfor:list', 'monitor/log/errorLog', 'lucide:triangle-alert', 0, 1, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '异常日志菜单');
INSERT INTO `sys_menu` VALUES (1001, 100, '用户查询', 'UserQuery', 1, '', 'system:user:query', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1002, 100, '用户新增', 'UserAdd', 2, '', 'system:user:add', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1003, 100, '用户修改', 'UserEdit', 3, '', 'system:user:edit', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1004, 100, '用户删除', 'UserDelete', 4, '', 'system:user:del', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1005, 100, '用户导出', 'UserExport', 5, '', 'system:user:export', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1006, 100, '用户导入', 'UserImport', 6, '', 'system:user:import', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1007, 100, '重置密码', 'UserResetPwd', 7, '', 'system:user:resetPwd', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1008, 101, '角色查询', 'RoleQuery', 1, '', 'system:role:query', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1009, 101, '角色新增', 'RoleAdd', 2, '', 'system:role:add', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1010, 101, '角色修改', 'RoleEdit', 3, '', 'system:role:edit', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1011, 101, '角色删除', 'RoleDelete', 4, '', 'system:role:del', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1012, 101, '角色导出', 'RoleExport', 5, '', 'system:role:export', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1013, 102, '菜单查询', 'MenuQuery', 1, '', 'system:menu:query', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1014, 102, '菜单新增', 'MenuAdd', 2, '', 'system:menu:add', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1015, 102, '菜单修改', 'MenuEdit', 3, '', 'system:menu:edit', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1016, 102, '菜单删除', 'MenuDelete', 4, '', 'system:menu:del', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1049, 102, '菜单导出', 'MenuExport', 5, '', 'system:menu:export', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1017, 103, '部门查询', 'DeptQuery', 1, '', 'system:dept:query', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1018, 103, '部门新增', 'DeptAdd', 2, '', 'system:dept:add', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1019, 103, '部门修改', 'DeptEdit', 3, '', 'system:dept:edit', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1020, 103, '部门删除', 'DeptDelete', 4, '', 'system:dept:del', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1050, 103, '部门导出', 'DeptExport', 5, '', 'system:dept:export', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1021, 104, '岗位查询', 'PostQuery', 1, '', 'system:post:query', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1022, 104, '岗位新增', 'PostAdd', 2, '', 'system:post:add', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1023, 104, '岗位修改', 'PostEdit', 3, '', 'system:post:edit', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1024, 104, '岗位删除', 'PostDelete', 4, '', 'system:post:del', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1025, 104, '岗位导出', 'PostExport', 5, '', 'system:post:export', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1026, 105, '字典查询', 'DictQuery', 1, '#', 'system:dict:query', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1027, 105, '字典新增', 'DictAdd', 2, '#', 'system:dict:add', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1028, 105, '字典修改', 'DictEdit', 3, '#', 'system:dict:edit', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1029, 105, '字典删除', 'DictDelete', 4, '#', 'system:dict:del', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1030, 105, '字典导出', 'DictExport', 5, '#', 'system:dict:export', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1031, 106, '参数查询', 'ConfigQuery', 1, '#', 'system:config:query', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1032, 106, '参数新增', 'ConfigAdd', 2, '#', 'system:config:add', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1033, 106, '参数修改', 'ConfigEdit', 3, '#', 'system:config:edit', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1034, 106, '参数删除', 'ConfigDelete', 4, '#', 'system:config:del', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1035, 106, '参数导出', 'ConfigExport', 5, '#', 'system:config:export', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1036, 107, '公告查询', 'NoticeQuery', 1, '#', 'system:notice:query', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1037, 107, '公告新增', 'NoticeAdd', 2, '#', 'system:notice:add', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1038, 107, '公告修改', 'NoticeEdit', 3, '#', 'system:notice:edit', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1039, 107, '公告删除', 'NoticeDelete', 4, '#', 'system:notice:del', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1051, 107, '公告导出', 'NoticeExport', 5, '#', 'system:notice:export', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1040, 500, '操作查询', 'LogQuery', 1, '#', 'system:logs:info:list', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1041, 500, '操作删除', 'LogDelete', 2, '#', 'system:logs:info:del', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1042, 500, '操作导出', 'LogExport', 4, '#', 'system:logs:info:export', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1043, 501, '异常查询', 'ErrorLogQuery', 1, '#', 'system:logs:error:list', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1044, 501, '异常删除', 'ErrorLogDelete', 2, '#', 'system:logs:error:del', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1045, 501, '异常导出', 'ErrorLogExport', 3, '#', 'system:logs:error:export', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1046, 109, '在线查询', 'TokenQuery', 1, '#', 'monitor:online:query', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1047, 109, '批量强退', 'TokenBatchLogout', 2, '#', 'monitor:online:batchLogout', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_menu` VALUES (1048, 109, '单条强退', 'TokenForceLogout', 3, '#', 'monitor:online:forceLogout', '', '#', 0, 2, 0, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');

-- ----------------------------
-- Table structure for sys_message
-- ----------------------------
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'system' COMMENT '分类（字典 sys_message_category）',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '消息内容',
  `send_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '是否推送（0否 1是）',
  `seq` int(11) NULL DEFAULT 0 COMMENT '排序（越大越在前）',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态（0停用 1正常）',
  `created_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `updated_at` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category`) USING BTREE,
  INDEX `idx_enabled`(`enabled`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统消息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_user_message
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_message`;
CREATE TABLE `sys_user_message`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '推送记录ID',
  `message_id` bigint(20) UNSIGNED NOT NULL COMMENT '消息ID（关联 sys_message.id）',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '接收用户ID（关联 sys_user.id）',
  `read_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '已读（0否 1是）',
  `created_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `updated_at` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_message_id`(`message_id`) USING BTREE,
  INDEX `idx_user_unread`(`user_id`, `read_flag`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户消息推送记录表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '公告类型（1通知 2公告）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '公告内容',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '公告状态（0正常 1关闭）',
  `created_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `updated_at` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知公告表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, '温馨提醒：2018-07-01 mica-admin 新版本发布啦', 2, '新版本内容', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '管理员');
INSERT INTO `sys_notice` VALUES (2, '维护通知：2018-07-01 mica-admin 系统凌晨维护', 1, '维护内容', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '管理员');

-- ----------------------------
-- Table structure for sys_file_storage
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_storage`;
CREATE TABLE `sys_file_storage` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `storage_type` varchar(20) NOT NULL COMMENT '存储类型：LOCAL / OSS / S3 / MINIO',
  `bucket` varchar(100) DEFAULT NULL COMMENT 'OSS bucket（本地可为空）',
  `endpoint` varchar(255) DEFAULT NULL COMMENT 'OSS endpoint',
  `file_key` varchar(255) NOT NULL COMMENT '文件唯一 key（OSS key / 本地路径）',
  `md5` varchar(32) DEFAULT NULL COMMENT '文件 MD5',
  `url` varchar(500) DEFAULT NULL COMMENT '访问地址',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_real_name` varchar(255) DEFAULT NULL COMMENT '真实存储名',
  `suffix` varchar(20) DEFAULT NULL COMMENT '后缀',
  `size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `mime_type` varchar(50) DEFAULT NULL COMMENT 'MIME 类型',
  `file_type` varchar(50) DEFAULT NULL COMMENT '业务类型',
  `user_id` bigint(20) UNSIGNED DEFAULT NULL COMMENT '上传用户ID（关联 sys_user.id）',
  `is_private` tinyint(1) DEFAULT '0' COMMENT '是否私有：1=私有 0=公开',
  `created_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `updated_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_file_key` (`storage_type`,`file_key`),
  KEY `idx_md5` (`md5`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='统一文件存储表';

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
  `seq` int(11) NOT NULL COMMENT '显示顺序',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态（0停用,1正常）',
  `created_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `updated_at` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '岗位信息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, 'ceo', '董事长', 1, 1, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_post` VALUES (2, 'se', '项目经理', 2, 1, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_post` VALUES (3, 'hr', '人力资源', 3, 1, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');
INSERT INTO `sys_post` VALUES (4, 'user', '普通员工', 4, 1, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `seq` int(11) NOT NULL COMMENT '显示顺序',
  `data_scope` tinyint(4) NULL DEFAULT 1 COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `status` tinyint(4) NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  `created_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `updated_at` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色信息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '管理员', 'admin', 1, 1, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '管理员');
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 2, 2, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '普通角色');

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和部门关联表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
INSERT INTO `sys_role_dept` VALUES (2, 100);
INSERT INTO `sys_role_dept` VALUES (2, 101);
INSERT INTO `sys_role_dept` VALUES (2, 105);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (2, 1);
INSERT INTO `sys_role_menu` VALUES (2, 2);
INSERT INTO `sys_role_menu` VALUES (2, 3);
INSERT INTO `sys_role_menu` VALUES (2, 4);
-- 补充的一级目录
INSERT INTO `sys_role_menu` VALUES (2, 5);
INSERT INTO `sys_role_menu` VALUES (2, 6);
INSERT INTO `sys_role_menu` VALUES (2, 100);
INSERT INTO `sys_role_menu` VALUES (2, 101);
INSERT INTO `sys_role_menu` VALUES (2, 102);
INSERT INTO `sys_role_menu` VALUES (2, 103);
INSERT INTO `sys_role_menu` VALUES (2, 104);
INSERT INTO `sys_role_menu` VALUES (2, 105);
INSERT INTO `sys_role_menu` VALUES (2, 106);
INSERT INTO `sys_role_menu` VALUES (2, 107);
-- 补充的 OSS存储
INSERT INTO `sys_role_menu` VALUES (2, 108);
INSERT INTO `sys_role_menu` VALUES (2, 109);
-- 补充的 在线用户
INSERT INTO `sys_role_menu` VALUES (2, 110);
INSERT INTO `sys_role_menu` VALUES (2, 111);
INSERT INTO `sys_role_menu` VALUES (2, 112);
INSERT INTO `sys_role_menu` VALUES (2, 113);
-- 表单构建 (id=114) 已废弃，前端已删除
INSERT INTO `sys_role_menu` VALUES (2, 115);
-- 补充的仪表盘子项（工作台 id=117 前端暂无独立视图，未挂出，此处也不分配）
INSERT INTO `sys_role_menu` VALUES (2, 116);
-- 补充的公共组件子项
INSERT INTO `sys_role_menu` VALUES (2, 118);
INSERT INTO `sys_role_menu` VALUES (2, 119);
INSERT INTO `sys_role_menu` VALUES (2, 120);
INSERT INTO `sys_role_menu` VALUES (2, 121);
-- 系统管理 - 消息中心
INSERT INTO `sys_role_menu` VALUES (2, 122);
INSERT INTO `sys_role_menu` VALUES (2, 500);
INSERT INTO `sys_role_menu` VALUES (2, 501);
INSERT INTO `sys_role_menu` VALUES (2, 1001);
INSERT INTO `sys_role_menu` VALUES (2, 1002);
INSERT INTO `sys_role_menu` VALUES (2, 1003);
INSERT INTO `sys_role_menu` VALUES (2, 1004);
INSERT INTO `sys_role_menu` VALUES (2, 1005);
INSERT INTO `sys_role_menu` VALUES (2, 1006);
INSERT INTO `sys_role_menu` VALUES (2, 1007);
INSERT INTO `sys_role_menu` VALUES (2, 1008);
INSERT INTO `sys_role_menu` VALUES (2, 1009);
INSERT INTO `sys_role_menu` VALUES (2, 1010);
INSERT INTO `sys_role_menu` VALUES (2, 1011);
INSERT INTO `sys_role_menu` VALUES (2, 1012);
INSERT INTO `sys_role_menu` VALUES (2, 1013);
INSERT INTO `sys_role_menu` VALUES (2, 1014);
INSERT INTO `sys_role_menu` VALUES (2, 1015);
INSERT INTO `sys_role_menu` VALUES (2, 1016);
INSERT INTO `sys_role_menu` VALUES (2, 1017);
INSERT INTO `sys_role_menu` VALUES (2, 1018);
INSERT INTO `sys_role_menu` VALUES (2, 1019);
INSERT INTO `sys_role_menu` VALUES (2, 1020);
INSERT INTO `sys_role_menu` VALUES (2, 1021);
INSERT INTO `sys_role_menu` VALUES (2, 1022);
INSERT INTO `sys_role_menu` VALUES (2, 1023);
INSERT INTO `sys_role_menu` VALUES (2, 1024);
INSERT INTO `sys_role_menu` VALUES (2, 1025);
INSERT INTO `sys_role_menu` VALUES (2, 1026);
INSERT INTO `sys_role_menu` VALUES (2, 1027);
INSERT INTO `sys_role_menu` VALUES (2, 1028);
INSERT INTO `sys_role_menu` VALUES (2, 1029);
INSERT INTO `sys_role_menu` VALUES (2, 1030);
INSERT INTO `sys_role_menu` VALUES (2, 1031);
INSERT INTO `sys_role_menu` VALUES (2, 1032);
INSERT INTO `sys_role_menu` VALUES (2, 1033);
INSERT INTO `sys_role_menu` VALUES (2, 1034);
INSERT INTO `sys_role_menu` VALUES (2, 1035);
INSERT INTO `sys_role_menu` VALUES (2, 1036);
INSERT INTO `sys_role_menu` VALUES (2, 1037);
INSERT INTO `sys_role_menu` VALUES (2, 1038);
INSERT INTO `sys_role_menu` VALUES (2, 1039);
INSERT INTO `sys_role_menu` VALUES (2, 1040);
INSERT INTO `sys_role_menu` VALUES (2, 1041);
INSERT INTO `sys_role_menu` VALUES (2, 1042);
INSERT INTO `sys_role_menu` VALUES (2, 1043);
INSERT INTO `sys_role_menu` VALUES (2, 1044);
INSERT INTO `sys_role_menu` VALUES (2, 1045);
INSERT INTO `sys_role_menu` VALUES (2, 1046);
INSERT INTO `sys_role_menu` VALUES (2, 1047);
INSERT INTO `sys_role_menu` VALUES (2, 1048);
-- 默认给管理员角色（role_id=2）授予任务管理全部按钮权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(2, 123), (2, 1052), (2, 1053), (2, 1054), (2, 1055), (2, 1056);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint(20) NULL DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户昵称',
  `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户邮箱',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '手机号码',
  `gender` tinyint(2) NOT NULL DEFAULT 0 COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '头像地址',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码',
  `is_admin` tinyint(1) NOT NULL DEFAULT 0 COMMENT '用户类型（0系统用户 1管理员）',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '帐号状态（0停用 1正常）',
  `locked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '登录状态（0:正常 1:锁定）',
  `del_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  `created_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `updated_at` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 103, 'admin', 'mica-admin', '596392912@qq.com', '15888888888', 1, '', '$2a$04$J973m0QcnU7s.AHpR1Gvq.5M7N7y8KqiT8TeZqJjfBMd9Ng8kVCrG', 1, 1, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2020-05-21 22:22:46', '超级管理员');
INSERT INTO `sys_user` VALUES (2, 105, 'mica', 'mica', '596392912@qq.com', '15666666666', 1, '', '$2a$04$J973m0QcnU7s.AHpR1Gvq.5M7N7y8KqiT8TeZqJjfBMd9Ng8kVCrG', 1, 1, 0, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '测试员');

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `post_id` bigint(20) NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户与岗位关联表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` VALUES (1, 1);
INSERT INTO `sys_user_post` VALUES (2, 2);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户和角色关联表' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);

-- ============================================================================
-- 升级迁移脚本（已有数据时执行）
-- 把 sys_menu.name 字段从 NULL 改为 NOT NULL DEFAULT ''
-- 注意：执行前需要先把所有 NULL 的 name 补上非空值
-- ============================================================================

-- 第一步：把所有 NULL 的 name 填上默认值（用 title 兜底）
UPDATE `sys_menu` SET `name` = `title` WHERE `name` IS NULL OR `name` = '';

-- 第二步：修改字段为 NOT NULL DEFAULT ''
ALTER TABLE `sys_menu` MODIFY COLUMN `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '菜单名称';

-- ============================================================================
-- Table structure for sys_job
-- 数据库驱动定时任务（@SysJob）。所有调度参数（cron / 启停 / 参数 schema）
-- 均存储在此表，应用启动时由 SysJobScheduler 读取并初始化调度。
-- ============================================================================
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `job_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务唯一标识（对应 @SysJob.value）',
  `job_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `cron_expression` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT 'cron 表达式',
  `enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否启用（0否 1是）',
  `param_schema` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '参数结构定义 JSON，例如：{"bizDate":"DATE","force":"BOOLEAN"}',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '任务描述',
  `created_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `updated_at` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_job_key`(`job_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据库驱动定时任务' ROW_FORMAT = Compact;

-- 演示任务：与 DemoSysJob 配套
INSERT INTO `sys_job` (`job_key`, `job_name`, `cron_expression`, `enabled`, `param_schema`, `description`) VALUES
('demoTask', '演示任务', '0/30 * * * * ?', 0, '{"bizDate":"DATE","force":"BOOLEAN"}', '演示任务：定时打印业务日期；支持补数（bizDate / force）');

-- ============================================================================
-- IM 模块已下线（自 v1.0 起 mica-admin 定位为通用后台系统，不再内置 IM）
-- 如需历史数据迁移，参考 docs/database/migration-im-drop.sql（待发布）
-- ============================================================================
