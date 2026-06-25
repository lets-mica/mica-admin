-- =============================================
-- sys_user_message.read_flag → is_read 迁移脚本
-- =============================================
-- 背景:
--   原字段 read_flag CHAR(1) ('0'/'1') 与 mica-admin 其他 17 处
--   tinyint(1) 布尔字段不一致,前端存在 '0'.equals 字符串比较与 boolean
--   不匹配问题。本次改造:
--     - 列名: read_flag → is_read
--     - 类型: char(1)  → tinyint(1)
--     - 默认: '0'     → 0
-- 适用范围:已部署 mica-admin 且存在 sys_user_message 表的环境
-- 风险:    低(列重命名 + 类型转换,数据零丢失)
-- 回滚:    见文末
-- =============================================

-- 1. 新增 is_read 临时列
ALTER TABLE `sys_user_message`
  ADD COLUMN `is_read_tmp` TINYINT(1) NULL DEFAULT 0 COMMENT '迁移临时列';

-- 2. 数据迁移:字符串 '1' → 1,其他 → 0
UPDATE `sys_user_message`
SET `is_read_tmp` = CASE
    WHEN `read_flag` = '1' OR `read_flag` = 1 THEN 1
    ELSE 0
  END;

-- 3. 删除旧列
ALTER TABLE `sys_user_message` DROP COLUMN `read_flag`;

-- 4. 临时列改名为 is_read
ALTER TABLE `sys_user_message`
  CHANGE COLUMN `is_read_tmp` `is_read` TINYINT(1) NOT NULL DEFAULT 0
  COMMENT '是否已读（0否 1是）';

-- 5. 补索引(如缺失)
SET @idx := (SELECT COUNT(*) FROM information_schema.STATISTICS
             WHERE TABLE_SCHEMA = DATABASE()
               AND TABLE_NAME = 'sys_user_message'
               AND INDEX_NAME = 'idx_user_unread');
SET @sql := IF(@idx = 0,
  'ALTER TABLE `sys_user_message` ADD INDEX `idx_user_unread`(`user_id`, `is_read`)',
  'SELECT "idx_user_unread already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 回滚脚本(如需)
-- =============================================
-- ALTER TABLE `sys_user_message`
--   ADD COLUMN `read_flag` CHAR(1) NULL DEFAULT '0' COMMENT '已读（0否 1是）';
-- UPDATE `sys_user_message` SET `read_flag` = IF(`is_read` = 1, '1', '0');
-- ALTER TABLE `sys_user_message` DROP COLUMN `is_read`;
