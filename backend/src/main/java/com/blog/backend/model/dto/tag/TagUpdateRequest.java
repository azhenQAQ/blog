package com.blog.backend.model.dto.tag;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 更新标签请求
 */
@Data
public class TagUpdateRequest implements Serializable {

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

    @Serial
    private static final long serialVersionUID = 1L;
}
