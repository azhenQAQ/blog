package com.blog.backend.model.dto.article;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 文章查询请求
 */
@Data
public class ArticleQueryRequest implements Serializable {

    /**
     * 标题模糊搜索
     */
    private String title;

    /**
     * 状态筛选: null=全部 0=草稿 1=已发布 2=已归档
     */
    private Integer status;

    /**
     * 回收站模式
     */
    private Boolean isRecycleBin;

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
