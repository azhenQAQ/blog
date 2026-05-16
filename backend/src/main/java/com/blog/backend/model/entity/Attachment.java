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
 * 附件表
 */
@TableName(value = "attachment")
@Data
public class Attachment implements Serializable {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 上传者ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 原始文件名
     */
    @TableField(value = "original_name")
    private String originalName;

    /**
     * 存储文件名(UUID)
     */
    @TableField(value = "stored_name")
    private String storedName;

    /**
     * 存储相对路径
     */
    @TableField(value = "stored_path")
    private String storedPath;

    /**
     * 缩略图相对路径
     */
    @TableField(value = "thumbnail_path")
    private String thumbnailPath;

    /**
     * 文件大小(字节)
     */
    @TableField(value = "file_size")
    private Long fileSize;

    /**
     * MIME类型
     */
    @TableField(value = "file_type")
    private String fileType;

    /**
     * 文件扩展名
     */
    @TableField(value = "file_ext")
    private String fileExt;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 删除时间
     */
    private Date deletedAt;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
