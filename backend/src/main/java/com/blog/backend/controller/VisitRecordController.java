package com.blog.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.backend.common.BaseResponse;
import com.blog.backend.common.ResultUtils;
import com.blog.backend.constant.UserConstant;
import com.blog.backend.annotation.AuthCheck;
import com.blog.backend.model.dto.visit.VisitRecordQueryRequest;
import com.blog.backend.model.vo.visit.VisitRecordVO;
import com.blog.backend.service.VisitRecordService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 访问记录管理接口（Admin）
 */
@RestController
@RequestMapping("/visitRecord")
public class VisitRecordController {

    @Resource
    private VisitRecordService visitRecordService;

    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<VisitRecordVO>> listByPage(VisitRecordQueryRequest request) {
        Page<VisitRecordVO> page = visitRecordService.listByPage(request);
        return ResultUtils.success(page);
    }
}
