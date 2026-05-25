-- ============================================================
-- 博客系统建表脚本
-- 数据库: blog（需提前创建）
-- ============================================================

USE `blog`;

-- ============================================================
-- 由于存在外键约束，删除表时必须先删子表，再删父表
-- DROP 顺序: comment → article_tag → article → attachment → friend_link → tag → user
-- CREATE 顺序相反: user → tag → friend_link → attachment → article → article_tag → comment
-- ============================================================

DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `article_tag`;
DROP TABLE IF EXISTS `article`;
DROP TABLE IF EXISTS `attachment`;
DROP TABLE IF EXISTS `friend_link`;
DROP TABLE IF EXISTS `tag`;
DROP TABLE IF EXISTS `music`;
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
-- 4. 附件表 (attachment)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `attachment` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `user_id`        BIGINT       NOT NULL                 COMMENT '上传者ID',
    `original_name`  VARCHAR(255) NOT NULL                 COMMENT '原始文件名',
    `stored_name`    VARCHAR(255) NOT NULL                 COMMENT '存储文件名(UUID)',
    `stored_path`    VARCHAR(500) NOT NULL                 COMMENT '存储相对路径',
    `thumbnail_path` VARCHAR(500) DEFAULT NULL             COMMENT '缩略图相对路径',
    `file_size`      BIGINT       NOT NULL                 COMMENT '文件大小(字节)',
    `file_type`      VARCHAR(100) NOT NULL                 COMMENT 'MIME类型',
    `file_ext`       VARCHAR(20)  DEFAULT NULL             COMMENT '文件扩展名',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at`     DATETIME     DEFAULT NULL             COMMENT '删除时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_file_ext` (`file_ext`),
    INDEX `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='附件表';

-- -----------------------------------------------------------
-- 5. 文章表 (article)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `article` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '文章ID',
    `title`        VARCHAR(200) NOT NULL                  COMMENT '文章标题',
    `content`      LONGTEXT     NOT NULL                  COMMENT '文章内容(Markdown)',
    `summary`      VARCHAR(500) DEFAULT NULL              COMMENT '文章摘要',
    `cover_image`  VARCHAR(255) DEFAULT NULL              COMMENT '封面图片URL',
    `status`       TINYINT      NOT NULL DEFAULT 0        COMMENT '0=草稿 1=已发布 2=已归档',
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
-- 6. 文章-标签关联表 (article_tag) - 多对多关系
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

-- -----------------------------------------------------------
-- 7. 音乐表 (music)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `music` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`      VARCHAR(255) NOT NULL COMMENT '歌曲名',
    `artist`     VARCHAR(255) DEFAULT '' COMMENT '歌手名',
    `cover_url`  VARCHAR(500) DEFAULT '' COMMENT '封面图片URL',
    `audio_url`  VARCHAR(500) NOT NULL COMMENT '音频文件URL',
    `lrc_url`    VARCHAR(500) DEFAULT '' COMMENT 'LRC歌词文件URL',
    `duration`   INT          DEFAULT 0 COMMENT '时长(秒)',
    `sort_order` INT          DEFAULT 0 COMMENT '排序(越小越前)',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME     DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='音乐表';

-- -----------------------------------------------------------
-- 8. 评论/留言表 (comment)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `comment` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `type`          VARCHAR(20)  NOT NULL                  COMMENT '类型: comment=文章评论, guestbook=留言板',
    `article_id`    BIGINT       DEFAULT NULL              COMMENT '关联文章ID（留言板时为NULL）',
    `parent_id`     BIGINT       DEFAULT NULL              COMMENT '父评论ID（回复时使用）',
    `reply_to_name` VARCHAR(50)  DEFAULT NULL              COMMENT '被回复者昵称',
    `nickname`      VARCHAR(50)  NOT NULL                  COMMENT '访客昵称',
    `email`         VARCHAR(100) NOT NULL                  COMMENT '访客邮箱',
    `content`       VARCHAR(500) NOT NULL                  COMMENT '评论内容',
    `ip_address`    VARCHAR(45)  DEFAULT NULL              COMMENT '访客IP',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at`    DATETIME     DEFAULT NULL              COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论/留言表';

-- -----------------------------------------------------------
-- 9. 访问记录表 (visit_record)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `visit_record`;
CREATE TABLE IF NOT EXISTS `visit_record` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `path`        VARCHAR(500) NOT NULL                  COMMENT '访问路径',
    `ip`          VARCHAR(50)  DEFAULT NULL              COMMENT '访问者IP',
    `user_agent`  VARCHAR(500) DEFAULT NULL              COMMENT '浏览器User-Agent',
    `referer`     VARCHAR(500) DEFAULT NULL              COMMENT '来源地址',
    `article_id`  BIGINT       DEFAULT NULL              COMMENT '关联文章ID',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at`  DATETIME     DEFAULT NULL              COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_path` (`path`(100)),
    KEY `idx_ip` (`ip`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网站访问记录表';

ALTER TABLE `visit_record`
    ADD COLUMN `location`        VARCHAR(100) DEFAULT NULL COMMENT 'IP地理位置'  AFTER `article_id`,
    ADD COLUMN `browser_summary` VARCHAR(100) DEFAULT NULL COMMENT '浏览器摘要'   AFTER `location`;
