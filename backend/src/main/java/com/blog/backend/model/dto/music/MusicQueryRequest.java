package com.blog.backend.model.dto.music;

import com.blog.backend.common.PageQueryRequest;
import lombok.Data;

/**
 * 音乐查询请求
 */
@Data
public class MusicQueryRequest extends PageQueryRequest {

    /**
     * 关键字模糊搜索（歌曲名）
     */
    private String keyword;
}
