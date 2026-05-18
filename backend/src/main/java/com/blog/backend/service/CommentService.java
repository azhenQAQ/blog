package com.blog.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.backend.model.dto.comment.CommentAddRequest;
import com.blog.backend.model.dto.comment.CommentQueryRequest;
import com.blog.backend.model.entity.Comment;
import com.blog.backend.model.vo.comment.CommentVO;

import java.util.List;

public interface CommentService extends IService<Comment> {

    /**
     * 提交评论/留言
     */
    Long addComment(CommentAddRequest request, String ipAddress);

    /**
     * 获取文章全量评论
     */
    List<CommentVO> listByArticleId(Long articleId);

    /**
     * 获取留言板全量留言
     */
    List<CommentVO> listGuestbook();

    /**
     * 分页查询评论/留言列表
     */
    Page<CommentVO> listByPage(CommentQueryRequest request);

    /**
     * 删除评论（软删除）
     */
    boolean deleteComment(Long id);

    /**
     * 永久删除评论
     */
    boolean permanentlyDeleteComment(Long id);

    /**
     * 脱敏转换
     */
    CommentVO getCommentVO(Comment comment);

    /**
     * 批量脱敏转换
     */
    List<CommentVO> getCommentVOList(List<Comment> commentList);

    /**
     * 构造查询条件
     */
    QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest request);
}
