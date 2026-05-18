package com.blog.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 评论/留言表
 */
@TableName(value = "comment")
@Data
public class Comment implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类型: comment=文章评论, guestbook=留言板
     */
    @TableField(value = "type")
    private String type;

    /**
     * 关联文章ID（留言板时为NULL）
     */
    @TableField(value = "article_id")
    private Long articleId;

    /**
     * 父评论ID（回复时使用）
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 被回复者昵称
     */
    @TableField(value = "reply_to_name")
    private String replyToName;

    /**
     * 访客昵称
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 访客邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 评论内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 访客IP
     */
    @TableField(value = "ip_address")
    private String ipAddress;

    private Date createdAt;

    private Date updatedAt;

    @TableLogic
    private Date deletedAt;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
