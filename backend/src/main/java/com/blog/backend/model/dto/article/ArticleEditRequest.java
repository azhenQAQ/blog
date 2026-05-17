package com.blog.backend.model.dto.article;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * 编辑文章请求
 */
@Data
public class ArticleEditRequest implements Serializable {

    /**
     * 文章ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容(Markdown)
     */
    private String content;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 状态: 0=草稿 1=已发布 2=已归档
     */
    private Integer status;

    /**
     * 是否置顶: 1是 0否
     */
    private Integer isTop;

    /**
     * 标签ID列表
     */
    private List<Long> tagIds;

    @Serial
    private static final long serialVersionUID = 1L;
}
