package com.blog.backend.model.dto.tag;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 创建标签请求
 */
@Data
public class TagAddRequest implements Serializable {

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
