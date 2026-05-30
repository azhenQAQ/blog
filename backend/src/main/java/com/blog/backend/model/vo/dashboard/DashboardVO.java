package com.blog.backend.model.vo.dashboard;

import com.blog.backend.model.vo.article.ArticleVO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 控制台聚合数据视图
 */
@Data
public class DashboardVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章统计
     */
    private ArticleStats articleStats;

    /**
     * 访问统计
     */
    private VisitStats visitStats;

    /**
     * 资源统计
     */
    private ResourceStats resourceStats;

    /**
     * 热门文章 TOP 10
     */
    private List<ArticleVO> hotArticles;

    /**
     * 草稿列表
     */
    private List<ArticleVO> draftArticles;

    /**
     * 各标签文章数统计（饼图用）
     */
    private List<TagArticleStat> tagArticleStats;

    @Data
    public static class ArticleStats implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private long total;
        private long published;
        private long draft;
        private long archived;
    }

    @Data
    public static class VisitStats implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private long totalViews;
        private long uniqueVisitors;
    }

    @Data
    public static class ResourceStats implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private long tags;
        private long comments;
        private long attachments;
        private long music;
    }

    @Data
    public static class TagArticleStat implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private String tagName;
        private String tagColor;
        private long articleCount;
    }
}
