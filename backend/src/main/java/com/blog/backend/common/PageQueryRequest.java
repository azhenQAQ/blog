package com.blog.backend.common;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 分页查询请求基类
 */
@Data
public class PageQueryRequest implements Serializable {

    /**
     * 当前页（默认 1）
     */
    private Long current = 1L;

    /**
     * 每页大小（默认 10）
     */
    private Long pageSize = 10L;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方向（ascend/descend）
     */
    private String sortOrder;

    @Serial
    private static final long serialVersionUID = 1L;
}
