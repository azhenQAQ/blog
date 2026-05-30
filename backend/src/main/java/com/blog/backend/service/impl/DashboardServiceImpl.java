package com.blog.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.backend.mapper.ArticleTagMapper;
import com.blog.backend.model.entity.Article;
import com.blog.backend.model.entity.Tag;
import com.blog.backend.model.vo.article.ArticleVO;
import com.blog.backend.model.vo.dashboard.DashboardVO;
import com.blog.backend.model.vo.dashboard.DashboardVO.ArticleStats;
import com.blog.backend.model.vo.dashboard.DashboardVO.ResourceStats;
import com.blog.backend.model.vo.dashboard.DashboardVO.TagArticleStat;
import com.blog.backend.model.vo.dashboard.DashboardVO.VisitStats;
import com.blog.backend.mapper.ArticleMapper;
import com.blog.backend.mapper.VisitRecordMapper;
import com.blog.backend.service.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 控制台服务实现
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private VisitRecordMapper visitRecordMapper;

    @Resource
    private ArticleTagMapper articleTagMapper;

    @Resource
    private ArticleService articleService;

    @Resource
    private TagService tagService;

    @Resource
    private CommentService commentService;

    @Resource
    private AttachmentService attachmentService;

    @Resource
    private MusicService musicService;

    @Override
    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setArticleStats(buildArticleStats());
        vo.setVisitStats(buildVisitStats());
        vo.setResourceStats(buildResourceStats());
        vo.setHotArticles(buildHotArticles());
        vo.setDraftArticles(buildDraftArticles());
        vo.setTagArticleStats(buildTagArticleStats());
        return vo;
    }

    private ArticleStats buildArticleStats() {
        ArticleStats stats = new ArticleStats();
        long total = 0, published = 0, draft = 0, archived = 0;
        List<Map<String, Object>> rows = articleMapper.selectArticleCountGroupByStatus();
        for (Map<String, Object> row : rows) {
            Number status = (Number) row.get("status");
            Number cnt = (Number) row.get("cnt");
            long count = cnt.longValue();
            total += count;
            if (status != null) {
                switch (status.intValue()) {
                    case 1 -> published = count;
                    case 0 -> draft = count;
                    case 2 -> archived = count;
                }
            }
        }
        stats.setTotal(total);
        stats.setPublished(published);
        stats.setDraft(draft);
        stats.setArchived(archived);
        return stats;
    }

    private VisitStats buildVisitStats() {
        VisitStats stats = new VisitStats();
        Integer totalViews = articleMapper.selectTotalViewCount();
        stats.setTotalViews(totalViews != null ? totalViews : 0);
        Integer uniqueVisitors = visitRecordMapper.selectUniqueVisitorCount();
        stats.setUniqueVisitors(uniqueVisitors != null ? uniqueVisitors : 0);
        return stats;
    }

    private ResourceStats buildResourceStats() {
        ResourceStats stats = new ResourceStats();
        stats.setTags(tagService.count());
        stats.setComments(commentService.count());
        stats.setAttachments(attachmentService.count());
        stats.setMusic(musicService.count());
        return stats;
    }

    private List<ArticleVO> buildHotArticles() {
        Page<Article> page = articleService.page(
                new Page<>(1, 10),
                new QueryWrapper<Article>().eq("status", 1).orderByDesc("view_count"));
        return articleService.getArticleVOList(page.getRecords());
    }

    private List<ArticleVO> buildDraftArticles() {
        List<Article> articles = articleService.list(
                new QueryWrapper<Article>().eq("status", 0).orderByDesc("updated_at"));
        return articleService.getArticleVOList(articles);
    }

    private List<TagArticleStat> buildTagArticleStats() {
        // 一次查询获取所有标签文章数的聚合
        List<Map<String, Object>> tagCountRows = articleTagMapper.selectArticleCountGroupByTag();
        Map<Long, Long> tagCountMap = tagCountRows.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("tag_id")).longValue(),
                        row -> ((Number) row.get("cnt")).longValue()));

        List<Tag> tags = tagService.list();
        return tags.stream().map(tag -> {
            TagArticleStat stat = new TagArticleStat();
            stat.setTagName(tag.getName());
            stat.setTagColor(tag.getColor());
            stat.setArticleCount(tagCountMap.getOrDefault(tag.getId(), 0L));
            return stat;
        }).collect(Collectors.toList());
    }
}
