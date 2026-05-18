package com.blog.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.backend.annotation.AuthCheck;
import com.blog.backend.common.BaseResponse;
import com.blog.backend.common.ResultUtils;
import com.blog.backend.constant.UserConstant;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.model.dto.comment.CommentQueryRequest;
import com.blog.backend.model.vo.comment.CommentVO;
import com.blog.backend.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<CommentVO>> listByPage(CommentQueryRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        Page<CommentVO> page = commentService.listByPage(request);
        return ResultUtils.success(page);
    }

    @DeleteMapping("/delete/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> delete(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "评论ID无效");
        }
        boolean result = commentService.deleteComment(id);
        return ResultUtils.success(result);
    }

    @DeleteMapping("/permanent/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> permanentDelete(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "评论ID无效");
        }
        boolean result = commentService.permanentlyDeleteComment(id);
        return ResultUtils.success(result);
    }
}
