package com.blog.backend.utils;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.util.StrUtil;
import com.blog.backend.exception.BusinessException;
import com.blog.backend.exception.ErrorCode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 文件工具类
 */
public class FileUtils {

    /**
     * 生成 UUID 存储文件名（保留原扩展名）
     */
    public static String generateStoredName(String originalFilename) {
        String ext = getExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return StrUtil.isNotBlank(ext) ? uuid + "." + ext : uuid;
    }

    /**
     * 获取文件扩展名（小写，不含点）
     */
    public static String getExtension(String filename) {
        if (StrUtil.isBlank(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 根据 MIME 类型判断文件大类
     */
    public static String getCategoryDir(String fileType) {
        if (StrUtil.isBlank(fileType)) {
            return "others";
        }
        if (fileType.startsWith("image/")) {
            return "images";
        }
        if (fileType.startsWith("application/") || fileType.startsWith("text/")) {
            return "documents";
        }
        return "others";
    }

    /**
     * 校验扩展名是否在白名单中
     */
    public static void validateExtension(String filename, String allowedExtensions) {
        String ext = getExtension(filename);
        if (StrUtil.isBlank(ext)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件缺少扩展名");
        }
        Set<String> allowedSet = new HashSet<>(Arrays.asList(allowedExtensions.split(",")));
        if (!allowedSet.contains(ext)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的文件类型: ." + ext);
        }
    }

    /**
     * 格式化文件大小为可读字符串
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        }
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * 判断是否为图片类型（需要生成缩略图）
     */
    public static boolean isImage(String fileType) {
        return StrUtil.isNotBlank(fileType) && fileType.startsWith("image/");
    }
}
