package com.blog.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.backend.model.dto.article.ArticleAddRequest;
import com.blog.backend.model.dto.article.ArticleEditRequest;
import com.blog.backend.model.dto.article.ArticleQueryRequest;
import com.blog.backend.model.entity.Article;
import com.blog.backend.model.vo.article.ArticleDetailVO;
import com.blog.backend.model.vo.article.ArticleVO;

import java.util.List;
import java.util.Map;

public interface ArticleService extends IService<Article> {

    /**
     * 创建文章
     */
    Long addArticle(ArticleAddRequest request, Long userId);

    /**
     * 编辑文章
     */
    boolean updateArticle(ArticleEditRequest request);

    /**
     * 删除文章（软删除，进回收站）
     */
    boolean deleteArticle(Long id);

    /**
     * 恢复文章（从回收站）
     */
    boolean restoreArticle(Long id);

    /**
     * 永久删除文章
     */
    boolean permanentlyDeleteArticle(Long id);

    /**
     * 分页查询文章列表（含回收站）
     */
    Page<ArticleVO> listByPage(ArticleQueryRequest request);

    /**
     * 获取文章详情（含 content）
     */
    ArticleDetailVO getArticleDetailVO(Long id);

    /**
     * 获取脱敏文章视图
     */
    ArticleVO getArticleVO(Article article);

    /**
     * 获取脱敏文章视图列表
     */
    List<ArticleVO> getArticleVOList(List<Article> articleList);

    /**
     * 获取查询条件
     */
    QueryWrapper<Article> getQueryWrapper(ArticleQueryRequest request);

    /**
     * 公开分页查询已发布非置顶文章
     */
    Page<ArticleVO> listPublicByPage(long current, long size);

    /**
     * 查询所有已发布置顶文章
     */
    List<ArticleVO> listTopArticles();

    /**
     * 获取公开文章详情（仅已发布），递增浏览量
     */
    ArticleDetailVO getPublicArticleDetail(Long id);

    /**
     * 获取相邻文章（上一篇/下一篇）
     */
    Map<String, ArticleVO> getAdjacentArticles(Long id);
}
