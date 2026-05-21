package com.blog.backend.model.dto.music;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 更新音乐请求
 */
@Data
public class MusicEditRequest implements Serializable {

    /**
     * 音乐ID
     */
    private Long id;

    /**
     * 歌曲名
     */
    private String title;

    /**
     * 歌手名
     */
    private String artist;

    /**
     * 排序(越小越前)
     */
    private Integer sortOrder;

    @Serial
    private static final long serialVersionUID = 1L;
}
