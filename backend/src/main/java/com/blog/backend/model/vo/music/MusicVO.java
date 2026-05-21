package com.blog.backend.model.vo.music;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 音乐视图
 */
@Data
public class MusicVO implements Serializable {

    /**
     * 主键
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
     * 封面图片URL
     */
    private String coverUrl;

    /**
     * 音频文件URL
     */
    private String audioUrl;

    /**
     * LRC歌词文件URL
     */
    private String lrcUrl;

    /**
     * 时长(秒)
     */
    private Integer duration;

    /**
     * 排序(越小越前)
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    @Serial
    private static final long serialVersionUID = 1L;
}
