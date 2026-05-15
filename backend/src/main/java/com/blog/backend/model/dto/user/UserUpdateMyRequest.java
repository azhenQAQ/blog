package com.blog.backend.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户更新个人信息请求
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    private String nickname;
    private String userProfile;

    @Serial
    private static final long serialVersionUID = 1L;
}
