package com.blog.backend.model.vo.tag;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 标签视图
 */
@Data
public class TagVO implements Serializable {

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签颜色(十六进制)
     */
    private String color;

    /**
     * 关联文章数
     */
    private Integer articleCount;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @Serial
    private static final long serialVersionUID = 1L;
}
