package com.blog.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.backend.model.entity.VisitRecord;
import org.apache.ibatis.annotations.Select;

/**
 * 访问记录 Mapper
 */
public interface VisitRecordMapper extends BaseMapper<VisitRecord> {

    /**
     * 查询独立访客数（去重 IP，排除逻辑删除）
     */
    @Select("SELECT COUNT(DISTINCT ip) FROM visit_record WHERE deleted_at IS NULL")
    Integer selectUniqueVisitorCount();
}
