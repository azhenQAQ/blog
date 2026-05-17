package com.blog.backend.model.vo.article;

import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文章详情视图（编辑用，含 content）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleDetailVO extends ArticleVO {

    /**
     * 文章内容(Markdown)
     */
    private String content;

    @Serial
    private static final long serialVersionUID = 1L;
}
