package com.blog.backend.model.vo.visit;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 访问记录视图
 */
@Data
public class VisitRecordVO implements Serializable {

    private Long id;

    private String path;

    private String ip;

    private String userAgent;

    private String referer;

    private Long articleId;

    private Date createdAt;

    @Serial
    private static final long serialVersionUID = 1L;
}
