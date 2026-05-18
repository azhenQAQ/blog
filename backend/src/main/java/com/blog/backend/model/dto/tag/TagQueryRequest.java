package com.blog.backend.model.dto.tag;

import com.blog.backend.common.PageQueryRequest;
import lombok.Data;

/**
 * 标签查询请求
 */
@Data
public class TagQueryRequest extends PageQueryRequest {

    /**
     * 关键字模糊搜索
     */
    private String keyword;
}
