package com.blog.backend.model.vo.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图（脱敏，供管理员展示）
 */
@Data
public class UserVO implements Serializable {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String userProfile;
    private String role;
    private Date createdAt;
    private Date updatedAt;

    @Serial
    private static final long serialVersionUID = 1L;
}
