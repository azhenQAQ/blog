package com.blog.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.backend.exception.BusinessException;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.mapper.AttachmentMapper;
import com.blog.backend.model.dto.attachment.AttachmentQueryRequest;
import com.blog.backend.model.entity.Attachment;
import com.blog.backend.model.entity.User;
import com.blog.backend.model.vo.attachment.AttachmentVO;
import com.blog.backend.service.AttachmentService;
import com.blog.backend.service.UserService;
import com.blog.backend.utils.FileUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 附件服务实现
 */
@Slf4j
@Service
public class AttachmentServiceImpl extends ServiceImpl<AttachmentMapper, Attachment>
        implements AttachmentService {

    @Value("${blog.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${blog.file.allowed-extensions:jpg,jpeg,png,gif,webp,svg,bmp,pdf,doc,docx,xls,xlsx,ppt,pptx,md,txt,csv,zip,rar,mp3,mp4,avi,mov}")
    private String allowedExtensions;

    @Value("${blog.file.thumbnail-width:200}")
    private int thumbnailWidth;

    @Value("${blog.file.thumbnail-height:200}")
    private int thumbnailHeight;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Resource
    private UserService userService;

    /** 上传根目录的绝对路径（@PostConstruct 初始化） */
    private Path uploadBasePath;

    @PostConstruct
    public void init() {
        Path path = Paths.get(uploadDir);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        this.uploadBasePath = path.toAbsolutePath().normalize();
        log.info("附件上传目录: {}", uploadBasePath);
    }

    @Override
    public AttachmentVO upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件为空");
        }

        String originalName = file.getOriginalFilename();
        if (StrUtil.isBlank(originalName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名为空");
        }

        // 扩展名白名单校验
        FileUtils.validateExtension(originalName, allowedExtensions);

        // 文件大小二次校验（10MB）
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小超过 10MB 限制");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser();

        // 生成存储文件名和路径
        String storedName = FileUtils.generateStoredName(originalName);
        String fileType = file.getContentType();
        String fileExt = FileUtils.getExtension(originalName);
        String categoryDir = FileUtils.getCategoryDir(fileType);

        // 构建存储路径（使用绝对路径）
        Path categoryPath = uploadBasePath.resolve(categoryDir);
        try {
            Files.createDirectories(categoryPath);
        } catch (IOException e) {
            log.error("创建存储目录失败: {}", categoryPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建存储目录失败");
        }

        // 保存文件
        Path filePath = categoryPath.resolve(storedName);
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("文件保存失败: {}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存失败");
        }

        // 存储相对路径
        String storedPath = categoryDir + "/" + storedName;

        // 如果是图片，生成缩略图
        String thumbnailPath = null;
        if (FileUtils.isImage(fileType)) {
            thumbnailPath = generateThumbnail(categoryDir, storedName, filePath);
        }

        // 保存数据库记录
        Attachment attachment = new Attachment();
        attachment.setUserId(loginUser.getId());
        attachment.setOriginalName(originalName);
        attachment.setStoredName(storedName);
        attachment.setStoredPath(storedPath);
        attachment.setThumbnailPath(thumbnailPath);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(fileType);
        attachment.setFileExt(fileExt);

        boolean saved = this.save(attachment);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据库保存失败");
        }

        return getAttachmentVO(attachment);
    }

    /**
     * 生成缩略图
     */
    private String generateThumbnail(String categoryDir, String storedName, Path sourcePath) {
        String thumbName = storedName.substring(0, storedName.lastIndexOf('.'))
                + "_thumb." + storedName.substring(storedName.lastIndexOf('.') + 1);
        Path thumbDir = uploadBasePath.resolve(categoryDir).resolve("thumbnails");
        try {
            Files.createDirectories(thumbDir);
        } catch (IOException e) {
            log.warn("创建缩略图目录失败: {}", thumbDir, e);
            return null;
        }
        File thumbFile = thumbDir.resolve(thumbName).toFile();
        try {
            Thumbnails.of(sourcePath.toFile())
                    .size(thumbnailWidth, thumbnailHeight)
                    .keepAspectRatio(true)
                    .toFile(thumbFile);
            return categoryDir + "/thumbnails/" + thumbName;
        } catch (IOException e) {
            // 缩略图生成失败不影响主流程
            return null;
        }
    }

    @Override
    public Page<AttachmentVO> listByPage(AttachmentQueryRequest request) {
        long current = request.getCurrent() != null ? request.getCurrent() : 1;
        long size = request.getPageSize() != null ? request.getPageSize() : 12;

        Page<Attachment> page = this.page(new Page<>(current, size), getQueryWrapper(request));
        Page<AttachmentVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::getAttachmentVO)
                .toList());
        return voPage;
    }

    @Override
    public AttachmentVO getAttachmentVO(Attachment attachment) {
        if (attachment == null) {
            return null;
        }
        AttachmentVO vo = new AttachmentVO();
        BeanUtil.copyProperties(attachment, vo);

        // 构造完整访问 URL
        String urlPrefix = contextPath + "/uploads/";
        vo.setUrl(urlPrefix + attachment.getStoredPath());
        if (StrUtil.isNotBlank(attachment.getThumbnailPath())) {
            vo.setThumbnailUrl(urlPrefix + attachment.getThumbnailPath());
        }

        // 查询上传者用户名
        if (attachment.getUserId() != null) {
            User user = userService.getById(attachment.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
        }
        return vo;
    }

    @Override
    public boolean deleteAttachment(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "附件ID无效");
        }
        Attachment attachment = this.getById(id);
        if (attachment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "附件不存在");
        }
        // MyBatis-Plus 逻辑删除，物理文件保留
        return this.removeById(id);
    }

    @Override
    public QueryWrapper<Attachment> getQueryWrapper(AttachmentQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        String fileName = request.getFileName();
        String fileType = request.getFileType();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        QueryWrapper<Attachment> queryWrapper = new QueryWrapper<>();

        // 文件名模糊搜索
        queryWrapper.like(StrUtil.isNotBlank(fileName), "original_name", fileName);

        // 文件 MIME 类型筛选
        if (StrUtil.isNotBlank(fileType)) {
            if ("image".equals(fileType)) {
                queryWrapper.like("file_type", "image/");
            } else if ("document".equals(fileType)) {
                queryWrapper.and(w -> w.like("file_type", "application/")
                        .or().like("file_type", "text/"));
            } else if ("other".equals(fileType)) {
                queryWrapper.and(w -> w.notLike("file_type", "image/")
                        .notLike("file_type", "application/")
                        .notLike("file_type", "text/"));
            }
        }

        // 排序（默认按创建时间倒序）
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), StrUtil.toUnderlineCase(sortField));
        } else {
            queryWrapper.orderByDesc("created_at");
        }

        return queryWrapper;
    }
}
