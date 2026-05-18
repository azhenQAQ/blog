package com.blog.backend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum CommentTypeEnum {

    COMMENT("comment", "文章评论"),
    GUESTBOOK("guestbook", "留言板");

    private final String value;
    private final String text;

    CommentTypeEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public static CommentTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (CommentTypeEnum anEnum : CommentTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
