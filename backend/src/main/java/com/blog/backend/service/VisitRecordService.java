package com.blog.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.backend.model.dto.visit.VisitRecordQueryRequest;
import com.blog.backend.model.entity.VisitRecord;
import com.blog.backend.model.vo.visit.VisitRecordVO;

public interface VisitRecordService extends IService<VisitRecord> {

    /**
     * 记录一次访问
     */
    void recordVisit(String path, String ip, String userAgent, String referer);

    /**
     * 分页查询访问记录
     */
    Page<VisitRecordVO> listByPage(VisitRecordQueryRequest request);

    /**
     * 脱敏转换
     */
    VisitRecordVO getVisitRecordVO(VisitRecord visitRecord);

    /**
     * 构造查询条件
     */
    QueryWrapper<VisitRecord> getQueryWrapper(VisitRecordQueryRequest request);
}
