package com.blog.backend.model.dto.attachment;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 附件查询请求
 */
@Data
public class AttachmentQueryRequest implements Serializable {

    /**
     * 文件名模糊搜索
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方向（ascend/descend）
     */
    private String sortOrder;

    /**
     * 当前页
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long pageSize;

    @Serial
    private static final long serialVersionUID = 1L;
}
