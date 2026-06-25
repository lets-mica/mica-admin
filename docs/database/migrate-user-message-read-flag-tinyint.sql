-- =============================================
-- sys_user_message.read_flag 类型迁移脚本
-- char(1) ('0'/'1') → tinyint(1) (0/1)
-- =============================================
-- 背景:
--   原字段 read_flag CHAR(1) 存 '0'/'1' 字符串,Java 端需做字符串比较
--   ("0".equals(...)),易出错。本次改造:
--     - 类型: char(1) → tinyint(1)
--     - 默认: '0'   → 0
--   注意: 不改字段名(read_flag 保持原名)
-- 适用范围:已部署 mica-admin 且存在 sys_user_message 表的环境
-- 风险:    低(列类型转换,MySQL 自动将 '0'/'1' 字符串转为 0/1 整数)
-- 回滚:    见文末
-- =============================================

-- 1. 修改列类型(注意默认值一并修改)
ALTER TABLE `sys_user_message`
  MODIFY COLUMN `read_flag` TINYINT(1) NOT NULL DEFAULT 0
  COMMENT '已读（0否 1是）';

-- 2. (可选)数据归一:把仍然为 '0'/'1' 字符串的存量数据转 0/1
--    MySQL 在 MODIFY COLUMN 时通常会做隐式转换,执行后表中应为整数
--    如果还有遗留字符串(取决于字符集 / sql_mode),执行下面的强转:
-- UPDATE `sys_user_message`
-- SET `read_flag` = CASE
--     WHEN `read_flag` = '1' OR `read_flag` = 1 THEN 1
--     ELSE 0
--   END
-- WHERE `read_flag` IS NULL
--    OR `read_flag` NOT IN (0, 1);

-- =============================================
-- 回滚脚本(如需)
-- =============================================
-- ALTER TABLE `sys_user_message`
--   MODIFY COLUMN `read_flag` CHAR(1) NOT NULL DEFAULT '0'
--   COMMENT '已读（0否 1是）';
-- UPDATE `sys_user_message` SET `read_flag` = IF(`read_flag` = 1, '1', '0');
