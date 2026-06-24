-- =============================================
-- IM 模块数据表 (Phase 1: 单聊 MVP)
-- 设计文档：docs/im/data-model.md
-- 执行方式：mysql -uroot -p mica_admin < docs/database/im-schema.sql
-- =============================================

-- ----------------------------
-- 1. 单聊会话表 im_conversation
--    - 单聊会话 id = "{min(userId)}_{max(userId)}"，固定 36 字符以内
--    - 单聊会话只有 2 个成员，所以这里只存冗余的 userA / userB 方便索引
-- ----------------------------
DROP TABLE IF EXISTS `im_conversation`;
CREATE TABLE `im_conversation` (
    `id`              VARCHAR(40)     NOT NULL                    COMMENT '会话 id，单聊 = {min}_{max}，群聊 = group id（Phase 1.1）',
    `type`            VARCHAR(16)     NOT NULL DEFAULT 'p2p'      COMMENT '会话类型：p2p 单聊，group 群聊',
    `user_a`          BIGINT          DEFAULT NULL                COMMENT '参与方 A（单聊专用，p2p 时非空）',
    `user_b`          BIGINT          DEFAULT NULL                COMMENT '参与方 B（单聊专用，p2p 时非空）',
    `last_msg_id`     BIGINT          DEFAULT NULL                COMMENT '最后一条消息 id（冗余，便于会话列表排序）',
    `last_msg_time`   DATETIME        DEFAULT NULL                COMMENT '最后一条消息时间',
    `last_msg_preview` VARCHAR(200)   DEFAULT NULL                COMMENT '最后一条消息预览文本',
    `created_by`      VARCHAR(64)     DEFAULT NULL                COMMENT '创建人',
    `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`      VARCHAR(64)     DEFAULT NULL                COMMENT '更新人',
    `updated_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_user_a_last_time` (`user_a`, `last_msg_time` DESC),
    KEY `idx_user_b_last_time` (`user_b`, `last_msg_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 会话表';

-- ----------------------------
-- 2. 会话成员表 im_conversation_member
--    - 单聊预创建 2 条记录（type=p2p）
--    - 群聊待 Phase 1.1 实现
--    - unread_count 维护每个成员在该会话下的未读数
-- ----------------------------
DROP TABLE IF EXISTS `im_conversation_member`;
CREATE TABLE `im_conversation_member` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT       COMMENT '主键',
    `conversation_id` VARCHAR(40)   NOT NULL                      COMMENT '会话 id',
    `user_id`       BIGINT          NOT NULL                      COMMENT '用户 id',
    `role`          VARCHAR(16)     NOT NULL DEFAULT 'member'    COMMENT '成员角色：owner / admin / member（Phase 1.1 用）',
    `unread_count`  INT             NOT NULL DEFAULT 0            COMMENT '未读消息数（仅作为缓存，权威值在 Redis）',
    `last_read_msg_id` BIGINT       DEFAULT NULL                  COMMENT '已读到的最大消息 id',
    `last_read_time` DATETIME       DEFAULT NULL                  COMMENT '最后一次已读时间',
    `mute`          TINYINT(1)      NOT NULL DEFAULT 0            COMMENT '是否免打扰 0否 1是',
    `top`           TINYINT(1)      NOT NULL DEFAULT 0            COMMENT '是否置顶 0否 1是',
    `created_by`    VARCHAR(64)     DEFAULT NULL                  COMMENT '创建人',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`    VARCHAR(64)     DEFAULT NULL                  COMMENT '更新人',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_conv_user` (`conversation_id`, `user_id`),
    KEY `idx_user_top_time` (`user_id`, `top`, `last_read_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 会话成员表';

-- ----------------------------
-- 3. 消息表 im_message
--    - 单聊场景，主键雪花算法（Long），由后端生成
--    - status：0=发送中 1=已送达 2=已撤回 3=发送失败
-- ----------------------------
DROP TABLE IF EXISTS `im_message`;
CREATE TABLE `im_message` (
    `id`              BIGINT          NOT NULL                    COMMENT '消息 id（雪花算法）',
    `conversation_id` VARCHAR(40)     NOT NULL                    COMMENT '会话 id',
    `sender_id`       BIGINT          NOT NULL                    COMMENT '发送者 userId',
    `receiver_id`     BIGINT          DEFAULT NULL                COMMENT '接收者 userId（单聊专用）',
    `msg_type`        VARCHAR(16)     NOT NULL DEFAULT 'text'     COMMENT '消息类型：text / image / file / system',
    `content`         TEXT            NOT NULL                    COMMENT '消息正文（文本或 JSON）',
    `extra`           VARCHAR(1024)   DEFAULT NULL                COMMENT '扩展字段（JSON 字符串）',
    `status`          TINYINT         NOT NULL DEFAULT 1          COMMENT '消息状态：0 发送中 1 已送达 2 已撤回 3 失败',
    `server_received_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '服务端入库时间（顺序基准）',
    `recall_by`       BIGINT          DEFAULT NULL                COMMENT '撤回操作人 userId',
    `recall_at`       DATETIME        DEFAULT NULL                COMMENT '撤回时间',
    `created_by`      VARCHAR(64)     DEFAULT NULL                COMMENT '创建人',
    `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`      VARCHAR(64)     DEFAULT NULL                COMMENT '更新人',
    `updated_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_conv_server_time` (`conversation_id`, `server_received_at`),
    KEY `idx_sender_received`  (`sender_id`, `server_received_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 消息表';

-- ----------------------------
-- 4. 单聊会话创建示例
-- ----------------------------
-- INSERT INTO `im_conversation` (`id`, `type`, `user_a`, `user_b`, `created_by`, `created_at`)
-- VALUES ('1_2', 'p2p', 1, 2, 'admin', NOW());

-- INSERT INTO `im_conversation_member` (`conversation_id`, `user_id`, `role`, `created_by`, `created_at`)
-- VALUES ('1_2', 1, 'member', 'admin', NOW()), ('1_2', 2, 'member', 'admin', NOW());