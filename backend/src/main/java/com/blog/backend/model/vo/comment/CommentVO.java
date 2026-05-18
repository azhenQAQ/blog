package com.blog.backend.model.vo.comment;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 评论/留言视图
 */
@Data
public class CommentVO implements Serializable {

    private Long id;

    private String type;

    private Long articleId;

    private Long parentId;

    private String replyToName;

    private String nickname;

    private String email;

    private String content;

    private Date createdAt;

    @Serial
    private static final long serialVersionUID = 1L;
}
