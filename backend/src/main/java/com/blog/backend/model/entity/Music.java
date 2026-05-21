package com.blog.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 音乐表
 */
@TableName(value = "music")
@Data
public class Music implements Serializable {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 歌曲名
     */
    @TableField(value = "title")
    private String title;

    /**
     * 歌手名
     */
    @TableField(value = "artist")
    private String artist;

    /**
     * 封面图片URL
     */
    @TableField(value = "cover_url")
    private String coverUrl;

    /**
     * 音频文件URL
     */
    @TableField(value = "audio_url")
    private String audioUrl;

    /**
     * LRC歌词文件URL
     */
    @TableField(value = "lrc_url")
    private String lrcUrl;

    /**
     * 时长(秒)
     */
    @TableField(value = "duration")
    private Integer duration;

    /**
     * 排序(越小越前)
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 删除时间
     */
    @TableLogic
    private LocalDateTime deletedAt;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
