package com.blog.backend.model.vo.article;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * 文章视图（列表用，不含 content）
 */
@Data
public class ArticleVO implements Serializable {

    /**
     * 文章ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String title;

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
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 作者名
     */
    private String authorName;

    /**
     * 作者头像
     */
    private String authorAvatar;

    /**
     * 关联标签列表
     */
    private List<com.blog.backend.model.vo.tag.TagVO> tags;

    /**
     * 发布时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @Serial
    private static final long serialVersionUID = 1L;
}
