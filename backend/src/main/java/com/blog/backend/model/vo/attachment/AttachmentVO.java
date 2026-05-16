package com.blog.backend.model.vo.attachment;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 附件视图（脱敏）
 */
@Data
public class AttachmentVO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 上传者ID
     */
    private Long userId;

    /**
     * 上传者用户名
     */
    private String username;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 存储文件名(UUID)
     */
    private String storedName;

    /**
     * 存储相对路径
     */
    private String storedPath;

    /**
     * 缩略图相对路径
     */
    private String thumbnailPath;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * MIME类型
     */
    private String fileType;

    /**
     * 文件扩展名
     */
    private String fileExt;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 完整访问 URL
     */
    private String url;

    /**
     * 缩略图完整访问 URL
     */
    private String thumbnailUrl;

    @Serial
    private static final long serialVersionUID = 1L;
}
