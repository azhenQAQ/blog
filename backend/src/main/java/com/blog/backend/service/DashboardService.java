package com.blog.backend.service;

import com.blog.backend.model.vo.dashboard.DashboardVO;

/**
 * 控制台服务
 */
public interface DashboardService {

    /**
     * 获取控制台聚合数据
     */
    DashboardVO getDashboard();
}
