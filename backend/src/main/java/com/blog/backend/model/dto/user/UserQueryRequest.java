package com.blog.backend.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户查询请求（管理员）
 */
@Data
public class UserQueryRequest implements Serializable {

    private Long id;
    private String username;
    private String nickname;
    private String userProfile;
    private String role;
    private String sortField;
    private String sortOrder;
    private Long current;
    private Long pageSize;

    @Serial
    private static final long serialVersionUID = 1L;
}
