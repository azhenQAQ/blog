package com.blog.backend.model.dto.article;

import com.blog.backend.common.PageQueryRequest;
import lombok.Data;

/**
 * 公开文章列表查询请求（博客端，无需认证）
 */
@Data
public class ArticlePublicQueryRequest extends PageQueryRequest {
}
