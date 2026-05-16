package com.blog.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.backend.model.dto.attachment.AttachmentQueryRequest;
import com.blog.backend.model.entity.Attachment;
import com.blog.backend.model.vo.attachment.AttachmentVO;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService extends IService<Attachment> {

    /**
     * 上传附件
     */
    AttachmentVO upload(MultipartFile file);

    /**
     * 分页查询附件列表（管理员）
     */
    Page<AttachmentVO> listByPage(AttachmentQueryRequest request);

    /**
     * 获取单个附件（脱敏）
     */
    AttachmentVO getAttachmentVO(Attachment attachment);

    /**
     * 软删除附件
     */
    boolean deleteAttachment(Long id);

    /**
     * 获取查询条件
     */
    QueryWrapper<Attachment> getQueryWrapper(AttachmentQueryRequest request);
}
