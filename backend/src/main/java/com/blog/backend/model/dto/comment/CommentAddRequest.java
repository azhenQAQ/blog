package com.blog.backend.model.dto.comment;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 提交评论/留言请求
 */
@Data
public class CommentAddRequest implements Serializable {

    /**
     * 类型: comment / guestbook
     */
    private String type;

    /**
     * 关联文章ID（type=comment 时必填）
     */
    private Long articleId;

    /**
     * 父评论ID（回复时传入）
     */
    private Long parentId;

    /**
     * 被回复者昵称
     */
    private String replyToName;

    /**
     * 访客昵称（必填）
     */
    private String nickname;

    /**
     * 访客邮箱（必填）
     */
    private String email;

    /**
     * 评论内容（必填，最长500字）
     */
    private String content;

    @Serial
    private static final long serialVersionUID = 1L;
}
