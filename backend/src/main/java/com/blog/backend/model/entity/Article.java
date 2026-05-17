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
 * 文章表
 */
@TableName(value = "article")
@Data
public class Article implements Serializable {

    /**
     * 文章ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文章标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 文章内容(Markdown)
     */
    @TableField(value = "content")
    private String content;

    /**
     * 文章摘要
     */
    @TableField(value = "summary")
    private String summary;

    /**
     * 封面图片URL
     */
    @TableField(value = "cover_image")
    private String coverImage;

    /**
     * 状态: 0=草稿 1=已发布 2=已归档
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 是否置顶: 1是 0否
     */
    @TableField(value = "is_top")
    private Integer isTop;

    /**
     * 浏览量
     */
    @TableField(value = "view_count")
    private Integer viewCount;

    /**
     * 作者ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 发布时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 删除时间
     */
    @TableLogic
    private Date deletedAt;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
