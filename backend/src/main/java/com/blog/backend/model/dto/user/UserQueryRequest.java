package com.blog.backend.model.dto.user;

import com.blog.backend.common.PageQueryRequest;
import lombok.Data;

/**
 * 用户查询请求（管理员）
 */
@Data
public class UserQueryRequest extends PageQueryRequest {

    private Long id;
    private String username;
    private String nickname;
    private String userProfile;
    private String role;
}
