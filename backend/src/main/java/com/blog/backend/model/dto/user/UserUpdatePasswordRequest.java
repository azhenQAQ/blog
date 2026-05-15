package com.blog.backend.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户修改密码请求
 */
@Data
public class UserUpdatePasswordRequest implements Serializable {

    private String oldPassword;

    private String newPassword;

    @Serial
    private static final long serialVersionUID = 1L;
}
