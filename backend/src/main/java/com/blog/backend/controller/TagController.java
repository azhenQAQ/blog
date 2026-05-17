package com.blog.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.backend.annotation.AuthCheck;
import com.blog.backend.common.BaseResponse;
import com.blog.backend.common.ResultUtils;
import com.blog.backend.constant.UserConstant;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.model.dto.tag.TagAddRequest;
import com.blog.backend.model.dto.tag.TagQueryRequest;
import com.blog.backend.model.dto.tag.TagUpdateRequest;
import com.blog.backend.model.vo.tag.TagVO;
import com.blog.backend.service.TagService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private TagService tagService;

    /**
     * 添加标签
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addTag(@RequestBody TagAddRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        Long id = tagService.addTag(request);
        return ResultUtils.success(id);
    }

    /**
     * 更新标签
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateTag(@RequestBody TagUpdateRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        boolean result = tagService.updateTag(request);
        return ResultUtils.success(result);
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/delete/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteTag(@PathVariable Long id,
                                           @RequestParam(required = false, defaultValue = "false") boolean force) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "标签ID无效");
        }
        boolean result = tagService.deleteTag(id, force);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询标签列表
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<TagVO>> listByPage(TagQueryRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        Page<TagVO> page = tagService.listByPage(request);
        return ResultUtils.success(page);
    }

    /**
     * 获取所有标签（文章多选用）
     */
    @GetMapping("/list/all")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<TagVO>> listAll() {
        List<TagVO> list = tagService.listAll();
        return ResultUtils.success(list);
    }

    /**
     * 获取单个标签
     */
    @GetMapping("/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<TagVO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "标签ID无效");
        }
        TagVO vo = tagService.getTagVO(tagService.getById(id));
        return ResultUtils.success(vo);
    }
}
