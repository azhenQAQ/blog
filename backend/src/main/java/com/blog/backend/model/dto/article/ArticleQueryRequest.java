package com.blog.backend.model.dto.article;

import com.blog.backend.common.PageQueryRequest;
import lombok.Data;

/**
 * 文章查询请求
 */
@Data
public class ArticleQueryRequest extends PageQueryRequest {

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
}
