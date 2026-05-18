package com.blog.backend.model.dto.comment;

import com.blog.backend.common.PageQueryRequest;

import java.io.Serial;
import lombok.Data;

/**
 * 评论/留言查询请求
 */
@Data
public class CommentQueryRequest extends PageQueryRequest {

    /**
     * 类型筛选: comment / guestbook
     */
    private String type;

    /**
     * 昵称搜索
     */
    private String nickname;

    /**
     * 邮箱搜索
     */
    private String email;

    /**
     * 内容搜索
     */
    private String content;

    @Serial
    private static final long serialVersionUID = 1L;
}
