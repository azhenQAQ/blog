package com.blog.backend.config;

import com.blog.backend.model.entity.User;
import com.blog.backend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限接口实现
 * 提供角色和权限数据
 */
@Component
public class StpInterfaceImpl implements cn.dev33.satoken.stp.StpInterface {

    @Resource
    private UserService userService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        User user = userService.getById(Long.valueOf(loginId.toString()));
        if (user == null) {
            return List.of();
        }
        return List.of(user.getRole());
    }
}
