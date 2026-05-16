package com.blog.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.backend.annotation.AuthCheck;
import com.blog.backend.common.BaseResponse;
import com.blog.backend.common.ResultUtils;
import com.blog.backend.constant.UserConstant;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.model.dto.attachment.AttachmentQueryRequest;
import com.blog.backend.model.vo.attachment.AttachmentVO;
import com.blog.backend.service.AttachmentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/attachment")
public class AttachmentController {

    @Resource
    private AttachmentService attachmentService;

    /**
     * 上传附件（需要登录）
     */
    @PostMapping("/upload")
    public BaseResponse<AttachmentVO> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文件为空");
        }
        AttachmentVO vo = attachmentService.upload(file);
        return ResultUtils.success(vo);
    }

    /**
     * 分页查询附件列表（管理员）
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AttachmentVO>> listByPage(AttachmentQueryRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        Page<AttachmentVO> page = attachmentService.listByPage(request);
        return ResultUtils.success(page);
    }

    /**
     * 获取单个附件详情（管理员）
     */
    @GetMapping("/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AttachmentVO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "附件ID无效");
        }
        AttachmentVO vo = attachmentService.getAttachmentVO(attachmentService.getById(id));
        return ResultUtils.success(vo);
    }

    /**
     * 删除附件（管理员，软删除）
     */
    @DeleteMapping("/delete/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> delete(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "附件ID无效");
        }
        boolean result = attachmentService.deleteAttachment(id);
        return ResultUtils.success(result);
    }
}
