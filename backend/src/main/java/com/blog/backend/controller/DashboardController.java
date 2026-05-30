package com.blog.backend.controller;

import com.blog.backend.annotation.AuthCheck;
import com.blog.backend.common.BaseResponse;
import com.blog.backend.common.ResultUtils;
import com.blog.backend.constant.UserConstant;
import com.blog.backend.model.vo.dashboard.DashboardVO;
import com.blog.backend.service.DashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制台接口
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<DashboardVO> getDashboard() {
        DashboardVO dashboard = dashboardService.getDashboard();
        return ResultUtils.success(dashboard);
    }
}
