package com.blog.backend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum ArticleStatusEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    ARCHIVED(2, "已归档");

    private final Integer value;
    private final String text;

    ArticleStatusEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    public static ArticleStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ArticleStatusEnum anEnum : ArticleStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
