package com.blog.backend.controller;

import com.blog.backend.common.BaseResponse;
import com.blog.backend.common.ResultUtils;
import com.blog.backend.model.vo.tag.TagVO;
import com.blog.backend.service.TagService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开标签接口（博客端，无需认证）
 */
@RestController
@RequestMapping("/tag/public")
public class TagPublicController {

    @Resource
    private TagService tagService;

    @GetMapping("/list")
    public BaseResponse<List<TagVO>> listAll() {
        List<TagVO> list = tagService.listAll();
        return ResultUtils.success(list);
    }
}
