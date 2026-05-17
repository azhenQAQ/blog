package com.blog.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 文章标签关联表
 */
@TableName(value = "article_tag")
@Data
public class ArticleTag implements Serializable {

    /**
     * 关联ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文章ID
     */
    @TableField(value = "article_id")
    private Long articleId;

    /**
     * 标签ID
     */
    @TableField(value = "tag_id")
    private Long tagId;

    /**
     * 创建时间
     */
    private Date createdAt;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
