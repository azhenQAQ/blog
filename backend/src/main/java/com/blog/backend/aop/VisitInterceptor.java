package com.blog.backend.aop;

import cn.hutool.core.util.StrUtil;
import com.blog.backend.service.VisitRecordService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 访问记录拦截器 — 自动记录页面访问（HandlerInterceptor 方式）
 * 注意与 AuthInterceptor（AOP 注解方式）技术机制不同，但职责同为横切关注点
 */
@Slf4j
@Component
public class VisitInterceptor implements HandlerInterceptor {

    @Resource
    private VisitRecordService visitRecordService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 跳过 admin 管理端的请求（通过 Referer 判断）
            String referer = request.getHeader("Referer");
            if (StrUtil.isNotBlank(referer) && referer.contains("/admin/")) {
                return true;
            }

            String path = request.getRequestURI();
            String ip = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");

            visitRecordService.recordVisit(path, ip, userAgent, referer);
        } catch (Exception e) {
            // 确保拦截器异常不影响主流程
            log.warn("记录访问日志失败: path={}, err={}", request.getRequestURI(), e.getMessage());
        }
        return true;
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
