package com.blog.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.backend.annotation.AuthCheck;
import com.blog.backend.common.BaseResponse;
import com.blog.backend.common.ResultUtils;
import com.blog.backend.constant.UserConstant;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.model.dto.article.ArticleAddRequest;
import com.blog.backend.model.dto.article.ArticleEditRequest;
import com.blog.backend.model.dto.article.ArticleQueryRequest;
import com.blog.backend.model.entity.User;
import com.blog.backend.model.vo.article.ArticleDetailVO;
import com.blog.backend.model.vo.article.ArticleVO;
import com.blog.backend.service.ArticleService;
import com.blog.backend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private UserService userService;

    /**
     * 创建文章
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addArticle(@RequestBody ArticleAddRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser();
        Long id = articleService.addArticle(request, loginUser.getId());
        return ResultUtils.success(id);
    }

    /**
     * 编辑文章
     */
    @PutMapping("/edit")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> editArticle(@RequestBody ArticleEditRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        boolean result = articleService.updateArticle(request);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询文章列表
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ArticleVO>> listByPage(ArticleQueryRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        Page<ArticleVO> page = articleService.listByPage(request);
        return ResultUtils.success(page);
    }

    /**
     * 获取文章详情（含 content）
     */
    @GetMapping("/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<ArticleDetailVO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        ArticleDetailVO vo = articleService.getArticleDetailVO(id);
        return ResultUtils.success(vo);
    }

    /**
     * 删除文章（软删除，进回收站）
     */
    @DeleteMapping("/delete/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> delete(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        boolean result = articleService.deleteArticle(id);
        return ResultUtils.success(result);
    }

    /**
     * 恢复文章（从回收站）
     */
    @PutMapping("/restore/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> restore(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        boolean result = articleService.restoreArticle(id);
        return ResultUtils.success(result);
    }

    /**
     * 永久删除文章
     */
    @DeleteMapping("/permanent/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> permanentDelete(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        boolean result = articleService.permanentlyDeleteArticle(id);
        return ResultUtils.success(result);
    }
}
