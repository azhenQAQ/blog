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
 * 网站访问记录表
 */
@TableName(value = "visit_record")
@Data
public class VisitRecord implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 访问路径
     */
    @TableField(value = "path")
    private String path;

    /**
     * 访问者IP
     */
    @TableField(value = "ip")
    private String ip;

    /**
     * 浏览器User-Agent
     */
    @TableField(value = "user_agent")
    private String userAgent;

    /**
     * 来源地址
     */
    @TableField(value = "referer")
    private String referer;

    /**
     * 关联文章ID
     */
    @TableField(value = "article_id")
    private Long articleId;

    private Date createdAt;

    private Date updatedAt;

    @TableLogic
    private Date deletedAt;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
