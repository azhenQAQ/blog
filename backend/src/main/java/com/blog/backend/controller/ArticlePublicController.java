package com.blog.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.backend.common.BaseResponse;
import com.blog.backend.common.ResultUtils;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.model.dto.article.ArticlePublicQueryRequest;
import com.blog.backend.model.vo.article.ArticleDetailVO;
import com.blog.backend.model.vo.article.ArticleVO;
import com.blog.backend.service.ArticleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 公开文章接口（博客端，无需认证）
 */
@RestController
@RequestMapping("/article/public")
public class ArticlePublicController {

    @Resource
    private ArticleService articleService;

    @GetMapping("/top")
    public BaseResponse<List<ArticleVO>> listTop() {
        List<ArticleVO> list = articleService.listTopArticles();
        return ResultUtils.success(list);
    }

    @GetMapping("/list")
    public BaseResponse<Page<ArticleVO>> listByPage(ArticlePublicQueryRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        long current = request.getCurrent() != null ? request.getCurrent() : 1;
        long size = request.getPageSize() != null ? request.getPageSize() : 10;
        Page<ArticleVO> page = articleService.listPublicByPage(current, size);
        return ResultUtils.success(page);
    }

    @GetMapping("/{id}")
    public BaseResponse<ArticleDetailVO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        ArticleDetailVO vo = articleService.getPublicArticleDetail(id);
        return ResultUtils.success(vo);
    }

    @GetMapping("/{id}/adjacent")
    public BaseResponse<Map<String, ArticleVO>> getAdjacent(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        Map<String, ArticleVO> result = articleService.getAdjacentArticles(id);
        return ResultUtils.success(result);
    }
}
