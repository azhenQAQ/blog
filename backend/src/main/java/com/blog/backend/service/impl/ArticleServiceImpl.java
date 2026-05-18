package com.blog.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.backend.exception.BusinessException;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.mapper.ArticleMapper;
import com.blog.backend.mapper.ArticleTagMapper;
import com.blog.backend.model.dto.article.ArticleAddRequest;
import com.blog.backend.model.dto.article.ArticleEditRequest;
import com.blog.backend.model.dto.article.ArticleQueryRequest;
import com.blog.backend.model.entity.Article;
import com.blog.backend.model.entity.ArticleTag;
import com.blog.backend.model.entity.User;
import com.blog.backend.model.enums.ArticleStatusEnum;
import com.blog.backend.model.vo.article.ArticleDetailVO;
import com.blog.backend.model.vo.article.ArticleVO;
import com.blog.backend.service.ArticleService;
import com.blog.backend.service.TagService;
import com.blog.backend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章服务实现
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ArticleTagMapper articleTagMapper;

    @Resource
    private UserService userService;

    @Resource
    private TagService tagService;

    @Override
    public Long addArticle(ArticleAddRequest request, Long userId) {
        if (request == null || StrUtil.isBlank(request.getTitle())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章标题不能为空");
        }
        if (request.getTitle().length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题不能超过200字");
        }
        // 发布时正文不能为空
        if (ArticleStatusEnum.PUBLISHED.getValue().equals(request.getStatus())
                && StrUtil.isBlank(request.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "发布文章需要填写正文内容");
        }

        // 摘要自动生成
        String summary = request.getSummary();
        if (StrUtil.isBlank(summary) && StrUtil.isNotBlank(request.getContent())) {
            summary = generateSummary(request.getContent());
        }

        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(summary);
        article.setCoverImage(request.getCoverImage());
        article.setStatus(request.getStatus() != null ? request.getStatus()
                : ArticleStatusEnum.DRAFT.getValue());
        article.setIsTop(request.getIsTop() != null ? request.getIsTop() : 0);
        article.setViewCount(0);
        article.setUserId(userId);

        boolean saved = this.save(article);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文章保存失败");
        }

        // 保存标签关联
        syncTags(article.getId(), request.getTagIds());

        return article.getId();
    }

    @Override
    public boolean updateArticle(ArticleEditRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章ID不能为空");
        }

        Article article = this.getById(request.getId());
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }

        if (StrUtil.isNotBlank(request.getTitle())) {
            if (request.getTitle().length() > 200) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题不能超过200字");
            }
            article.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            article.setContent(request.getContent());
        }
        if (request.getSummary() != null) {
            article.setSummary(request.getSummary());
        }
        if (request.getCoverImage() != null) {
            article.setCoverImage(request.getCoverImage());
        }
        if (request.getStatus() != null) {
            article.setStatus(request.getStatus());
        }
        if (request.getIsTop() != null) {
            article.setIsTop(request.getIsTop());
        }

        // 摘要自动生成
        if (StrUtil.isBlank(article.getSummary()) && StrUtil.isNotBlank(article.getContent())) {
            article.setSummary(generateSummary(article.getContent()));
        }

        this.updateById(article);

        // 同步标签
        if (request.getTagIds() != null) {
            syncTags(article.getId(), request.getTagIds());
        }

        return true;
    }

    @Override
    public boolean deleteArticle(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        Article article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }
        return this.removeById(id);
    }

    @Override
    public boolean restoreArticle(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        // 检查记录是否存在于回收站
        Article article = articleMapper.selectDeletedById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在或未被删除");
        }
        return articleMapper.restoreById(id) > 0;
    }

    @Override
    public boolean permanentlyDeleteArticle(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        // 删除文章标签关联
        articleTagMapper.delete(new QueryWrapper<ArticleTag>().eq("article_id", id));
        // 物理删除（绕过 @TableLogic）
        return articleMapper.deletePermanently(id) > 0;
    }

    @Override
    public Page<ArticleVO> listByPage(ArticleQueryRequest request) {
        long current = request.getCurrent() != null ? request.getCurrent() : 1;
        long size = request.getPageSize() != null ? request.getPageSize() : 10;

        Page<Article> page;
        if (Boolean.TRUE.equals(request.getIsRecycleBin())) {
            // 回收站模式：使用自定义查询绕过 @TableLogic
            QueryWrapper<Article> wrapper = new QueryWrapper<>();
            applyQueryConditions(wrapper, request);
            page = articleMapper.selectPageWithDeleted(new Page<>(current, size), wrapper);
        } else {
            page = this.page(new Page<>(current, size), getQueryWrapper(request));
        }

        Page<ArticleVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(getArticleVOList(page.getRecords()));
        return voPage;
    }

    @Override
    public ArticleDetailVO getArticleDetailVO(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        Article article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }
        ArticleDetailVO vo = new ArticleDetailVO();
        BeanUtil.copyProperties(article, vo);
        fillAuthorAndTags(vo, article);
        return vo;
    }

    @Override
    public ArticleVO getArticleVO(Article article) {
        if (article == null) {
            return null;
        }
        ArticleVO vo = new ArticleVO();
        BeanUtil.copyProperties(article, vo);
        fillAuthorAndTags(vo, article);
        return vo;
    }

    @Override
    public List<ArticleVO> getArticleVOList(List<Article> articleList) {
        if (CollUtil.isEmpty(articleList)) {
            return new ArrayList<>();
        }
        return articleList.stream().map(this::getArticleVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<Article> getQueryWrapper(ArticleQueryRequest request) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        applyQueryConditions(queryWrapper, request);
        return queryWrapper;
    }

    /**
     * 应用查询条件
     */
    private void applyQueryConditions(QueryWrapper<Article> queryWrapper, ArticleQueryRequest request) {
        if (request == null) {
            return;
        }
        String title = request.getTitle();
        Integer status = request.getStatus();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        queryWrapper.like(StrUtil.isNotBlank(title), "title", title);
        queryWrapper.eq(status != null, "status", status);

        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), StrUtil.toUnderlineCase(sortField));
        } else {
            queryWrapper.orderByDesc("created_at");
        }
    }

    /**
     * 同步文章标签关联（全量替换策略）
     */
    private void syncTags(Long articleId, List<Long> tagIds) {
        // 删除旧关联
        articleTagMapper.delete(new QueryWrapper<ArticleTag>().eq("article_id", articleId));
        // 批量插入新关联
        if (CollUtil.isNotEmpty(tagIds)) {
            for (Long tagId : tagIds) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(articleId);
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
    }

    /**
     * 填充作者信息和标签
     */
    private void fillAuthorAndTags(ArticleVO vo, Article article) {
        // 作者信息
        if (article.getUserId() != null) {
            User user = userService.getById(article.getUserId());
            if (user != null) {
                vo.setAuthorName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                vo.setAuthorAvatar(user.getAvatar());
            }
        }
        // 标签
        vo.setTags(tagService.listByArticleId(article.getId()));
    }

    /**
     * 从 Markdown 内容生成摘要（取纯文本前 200 字）
     */
    private String generateSummary(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        // 简易 Markdown 脱衣
        String plainText = content
                .replaceAll("[#*`>\\[\\]()\\-_|~]", "")
                .replaceAll("\\s+", " ")
                .trim();
        if (plainText.length() <= 200) {
            return plainText;
        }
        return plainText.substring(0, 200);
    }

    @Override
    public Page<ArticleVO> listPublicByPage(long current, long size) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("status", ArticleStatusEnum.PUBLISHED.getValue())
                .eq("is_top", 0)
                .orderByDesc("created_at");
        Page<Article> page = this.page(new Page<>(current, size), wrapper);
        Page<ArticleVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(getArticleVOList(page.getRecords()));
        return voPage;
    }

    @Override
    public List<ArticleVO> listTopArticles() {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("status", ArticleStatusEnum.PUBLISHED.getValue())
                .eq("is_top", 1)
                .orderByDesc("created_at");
        List<Article> articles = this.list(wrapper);
        return getArticleVOList(articles);
    }

    @Override
    public ArticleDetailVO getPublicArticleDetail(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        Article article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }
        if (!ArticleStatusEnum.PUBLISHED.getValue().equals(article.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "文章未发布");
        }
        // 递增浏览量
        articleMapper.incrementViewCount(id);
        article.setViewCount(article.getViewCount() + 1);

        ArticleDetailVO vo = new ArticleDetailVO();
        BeanUtil.copyProperties(article, vo);
        fillAuthorAndTags(vo, article);
        return vo;
    }

    @Override
    public Map<String, ArticleVO> getAdjacentArticles(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        Article article = this.getById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        }
        var createdAt = DateUtil.toLocalDateTime(article.getCreatedAt());
        Article prev = articleMapper.selectPreviousPublished(createdAt);
        Article next = articleMapper.selectNextPublished(createdAt);
        Map<String, ArticleVO> result = new HashMap<>();
        result.put("prev", prev != null ? getArticleVO(prev) : null);
        result.put("next", next != null ? getArticleVO(next) : null);
        return result;
    }
}
