package com.blog.backend.model.dto.attachment;

import com.blog.backend.common.PageQueryRequest;
import lombok.Data;

/**
 * 附件查询请求
 */
@Data
public class AttachmentQueryRequest extends PageQueryRequest {

    /**
     * 文件名模糊搜索
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;
}
