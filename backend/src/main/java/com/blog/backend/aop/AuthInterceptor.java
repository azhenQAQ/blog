package com.blog.backend.aop;

import cn.dev33.satoken.stp.StpUtil;
import com.blog.backend.annotation.AuthCheck;
import com.blog.backend.constant.UserConstant;
import com.blog.backend.exception.BusinessException;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.model.entity.User;
import com.blog.backend.service.UserService;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 权限校验 AOP 切面
 * 使用 Sa-Token 进行登录和角色校验
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();

        // 当前必须登录
        StpUtil.checkLogin();

        if (mustRole.isEmpty()) {
            return joinPoint.proceed();
        }

        // 校验角色
        if (UserConstant.ADMIN_ROLE.equals(mustRole)) {
            User loginUser = userService.getLoginUser();
            if (!UserConstant.ADMIN_ROLE.equals(loginUser.getRole())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

        return joinPoint.proceed();
    }
}
