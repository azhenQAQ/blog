package com.blog.backend.controller;

import com.blog.backend.common.BaseResponse;
import com.blog.backend.common.ResultUtils;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.model.dto.comment.CommentAddRequest;
import com.blog.backend.model.vo.comment.CommentVO;
import com.blog.backend.service.CommentService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公开评论/留言接口（博客端，无需认证）
 */
@RestController
@RequestMapping("/comment/public")
public class CommentPublicController {

    @Resource
    private CommentService commentService;

    @PostMapping("/add")
    public BaseResponse<Long> add(@RequestBody CommentAddRequest request,
                                  HttpServletRequest httpRequest) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String ip = httpRequest.getRemoteAddr();
        Long id = commentService.addComment(request, ip);
        return ResultUtils.success(id);
    }

    @GetMapping("/article/{articleId}")
    public BaseResponse<List<CommentVO>> listByArticle(@PathVariable Long articleId) {
        if (articleId == null || articleId <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        List<CommentVO> list = commentService.listByArticleId(articleId);
        return ResultUtils.success(list);
    }

    @GetMapping("/guestbook")
    public BaseResponse<List<CommentVO>> listGuestbook() {
        List<CommentVO> list = commentService.listGuestbook();
        return ResultUtils.success(list);
    }
}
