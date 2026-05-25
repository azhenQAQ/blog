package com.blog.backend.service;

public interface TranslateService {

    /**
     * 调用百度翻译 API
     *
     * @param text 待翻译文本
     * @return 翻译结果
     */
    String translate(String text);
}
