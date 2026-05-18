package com.blog.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.backend.exception.BusinessException;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.mapper.CommentMapper;
import com.blog.backend.model.dto.comment.CommentAddRequest;
import com.blog.backend.model.dto.comment.CommentQueryRequest;
import com.blog.backend.model.entity.Comment;
import com.blog.backend.model.enums.CommentTypeEnum;
import com.blog.backend.model.vo.comment.CommentVO;
import com.blog.backend.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Override
    public Long addComment(CommentAddRequest request, String ipAddress) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        String type = request.getType();
        if (StrUtil.isBlank(type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论类型不能为空");
        }
        CommentTypeEnum typeEnum = CommentTypeEnum.getEnumByValue(type);
        if (typeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论类型无效");
        }
        // 文章评论时 articleId 必填
        if (CommentTypeEnum.COMMENT.getValue().equals(type)
                && (request.getArticleId() == null || request.getArticleId() <= 0)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章ID不能为空");
        }
        if (StrUtil.isBlank(request.getNickname())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称不能为空");
        }
        if (request.getNickname().length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称不能超过50字");
        }
        if (StrUtil.isBlank(request.getEmail())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        }
        if (request.getEmail().length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能超过100字");
        }
        if (StrUtil.isBlank(request.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容不能为空");
        }
        if (request.getContent().length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容不能超过500字");
        }

        Comment comment = new Comment();
        comment.setType(type);
        comment.setArticleId(request.getArticleId());
        comment.setParentId(request.getParentId());
        comment.setReplyToName(request.getReplyToName());
        comment.setNickname(request.getNickname().trim());
        comment.setEmail(request.getEmail().trim());
        comment.setContent(request.getContent().trim());
        comment.setIpAddress(ipAddress);

        boolean saved = this.save(comment);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评论保存失败");
        }
        return comment.getId();
    }

    @Override
    public List<CommentVO> listByArticleId(Long articleId) {
        if (articleId == null || articleId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章ID无效");
        }
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("type", CommentTypeEnum.COMMENT.getValue())
                .eq("article_id", articleId)
                .orderByAsc("created_at");
        List<Comment> list = this.list(wrapper);
        return getCommentVOList(list);
    }

    @Override
    public List<CommentVO> listGuestbook() {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("type", CommentTypeEnum.GUESTBOOK.getValue())
                .orderByAsc("created_at");
        List<Comment> list = this.list(wrapper);
        return getCommentVOList(list);
    }

    @Override
    public Page<CommentVO> listByPage(CommentQueryRequest request) {
        long current = request.getCurrent() != null ? request.getCurrent() : 1;
        long size = request.getPageSize() != null ? request.getPageSize() : 10;

        Page<Comment> page = this.page(new Page<>(current, size), getQueryWrapper(request));
        Page<CommentVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(getCommentVOList(page.getRecords()));
        return voPage;
    }

    @Override
    public boolean deleteComment(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论ID无效");
        }
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评论不存在");
        }
        return this.removeById(id);
    }

    @Override
    public boolean permanentlyDeleteComment(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论ID无效");
        }
        // 物理删除（绕过 @TableLogic）
        return commentMapper.deleteById(id) > 0;
    }

    @Override
    public CommentVO getCommentVO(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentVO vo = new CommentVO();
        BeanUtil.copyProperties(comment, vo);
        return vo;
    }

    @Override
    public List<CommentVO> getCommentVOList(List<Comment> commentList) {
        if (CollUtil.isEmpty(commentList)) {
            return new ArrayList<>();
        }
        return commentList.stream().map(this::getCommentVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest request) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        if (request == null) {
            return queryWrapper;
        }

        String type = request.getType();
        String nickname = request.getNickname();
        String email = request.getEmail();
        String content = request.getContent();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        queryWrapper.eq(StrUtil.isNotBlank(type), "type", type);
        queryWrapper.like(StrUtil.isNotBlank(nickname), "nickname", nickname);
        queryWrapper.like(StrUtil.isNotBlank(email), "email", email);
        queryWrapper.like(StrUtil.isNotBlank(content), "content", content);

        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), StrUtil.toUnderlineCase(sortField));
        } else {
            queryWrapper.orderByDesc("created_at");
        }

        return queryWrapper;
    }
}
