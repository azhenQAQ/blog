package com.blog.backend.model.dto.tag;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 标签查询请求
 */
@Data
public class TagQueryRequest implements Serializable {

    /**
     * 关键字模糊搜索
     */
    private String keyword;

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
