-- ============================================================
-- 博客系统建表脚本
-- 数据库: blog（需提前创建）
-- ============================================================

USE `blog`;

-- ============================================================
-- 由于存在外键约束，删除表时必须先删子表，再删父表
-- DROP 顺序: article_tag → article → friend_link → tag → user
-- CREATE 顺序相反: user → tag → friend_link → article → article_tag
-- ============================================================

DROP TABLE IF EXISTS `article_tag`;
DROP TABLE IF EXISTS `article`;
DROP TABLE IF EXISTS `friend_link`;
DROP TABLE IF EXISTS `tag`;
DROP TABLE IF EXISTS `user`;

-- -----------------------------------------------------------
-- 1. 用户表 (user)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `username`     VARCHAR(50)  NOT NULL                  COMMENT '用户名',
    `password`     VARCHAR(255) NOT NULL                  COMMENT '密码(加密后)',
    `nickname`     VARCHAR(50)  DEFAULT NULL              COMMENT '昵称',
    `email`        VARCHAR(100) DEFAULT NULL              COMMENT '邮箱',
    `avatar`       VARCHAR(255) DEFAULT NULL              COMMENT '头像URL',
    `user_profile` VARCHAR(255) DEFAULT NULL              COMMENT '个人简介',
    `role`         VARCHAR(20)  NOT NULL DEFAULT 'user'   COMMENT '角色: admin/user',
    `status`       TINYINT      NOT NULL DEFAULT 1        COMMENT '状态: 1启用 0禁用',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at`   DATETIME     DEFAULT NULL              COMMENT '删除时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -----------------------------------------------------------
-- 2. 标签表 (tag)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tag` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '标签ID',
    `name`        VARCHAR(50)  NOT NULL                  COMMENT '标签名称',
    `color`       VARCHAR(20)  DEFAULT NULL              COMMENT '标签颜色(十六进制)',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at`  DATETIME     DEFAULT NULL              COMMENT '删除时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- -----------------------------------------------------------
-- 3. 友链表 (friend_link)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `friend_link` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '友链ID',
    `name`        VARCHAR(100) NOT NULL                  COMMENT '站点名称',
    `url`         VARCHAR(255) NOT NULL                  COMMENT '站点链接',
    `logo`        VARCHAR(255) DEFAULT NULL              COMMENT 'LOGO链接',
    `description` VARCHAR(255) DEFAULT NULL              COMMENT '站点描述',
    `sort_order`  INT          NOT NULL DEFAULT 0        COMMENT '排序(越小越前)',
    `status`      TINYINT      NOT NULL DEFAULT 1        COMMENT '状态: 1显示 0隐藏',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at`  DATETIME     DEFAULT NULL              COMMENT '删除时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='友链表';

-- -----------------------------------------------------------
-- 4. 文章表 (article)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `article` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '文章ID',
    `title`        VARCHAR(200) NOT NULL                  COMMENT '文章标题',
    `content`      LONGTEXT     NOT NULL                  COMMENT '文章内容(Markdown)',
    `summary`      VARCHAR(500) DEFAULT NULL              COMMENT '文章摘要',
    `cover_image`  VARCHAR(255) DEFAULT NULL              COMMENT '封面图片URL',
    `status`       TINYINT      NOT NULL DEFAULT 0        COMMENT '状态: 1已发布 0草稿',
    `is_top`       TINYINT      NOT NULL DEFAULT 0        COMMENT '是否置顶: 1是 0否',
    `view_count`   INT          NOT NULL DEFAULT 0        COMMENT '浏览量',
    `user_id`      BIGINT       NOT NULL                  COMMENT '作者ID',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at`   DATETIME     DEFAULT NULL              COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_article_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- -----------------------------------------------------------
-- 5. 文章-标签关联表 (article_tag) - 多对多关系
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `article_tag` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT  COMMENT '关联ID',
    `article_id`  BIGINT   NOT NULL                  COMMENT '文章ID',
    `tag_id`      BIGINT   NOT NULL                  COMMENT '标签ID',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';
