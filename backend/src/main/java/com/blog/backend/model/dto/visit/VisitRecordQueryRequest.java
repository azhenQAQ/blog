package com.blog.backend.model.dto.visit;

import com.blog.backend.common.PageQueryRequest;
import java.io.Serial;
import lombok.Data;

/**
 * 访问记录查询请求
 */
@Data
public class VisitRecordQueryRequest extends PageQueryRequest {

    /**
     * 路径搜索
     */
    private String path;

    /**
     * IP搜索
     */
    private String ip;

    /**
     * 来源地址搜索
     */
    private String referer;

    @Serial
    private static final long serialVersionUID = 1L;
}
