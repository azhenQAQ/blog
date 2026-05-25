package com.blog.backend.controller;

import com.blog.backend.common.BaseResponse;
import com.blog.backend.common.ResultUtils;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.model.dto.translate.TranslateRequest;
import com.blog.backend.service.TranslateService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/translate")
public class TranslateController {

    @Resource
    private TranslateService translateService;

    @PostMapping("/text")
    public BaseResponse<String> translate(@RequestBody TranslateRequest request) {
        String text = request.getText();
        if (text == null || text.isBlank()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "翻译文本不能为空");
        }

        String result = translateService.translate(text);
        return ResultUtils.success(result);
    }
}
