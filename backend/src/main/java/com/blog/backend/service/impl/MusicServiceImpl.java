package com.blog.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.backend.exception.BusinessException;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.mapper.MusicMapper;
import com.blog.backend.model.dto.music.MusicAddRequest;
import com.blog.backend.model.dto.music.MusicEditRequest;
import com.blog.backend.model.dto.music.MusicQueryRequest;
import com.blog.backend.model.entity.Music;
import com.blog.backend.model.vo.music.MusicVO;
import com.blog.backend.service.MusicService;
import com.blog.backend.utils.FileUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 音乐服务实现
 */
@Slf4j
@Service
public class MusicServiceImpl extends ServiceImpl<MusicMapper, Music> implements MusicService {

    @Value("${blog.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    /** 允许的音频扩展名 */
    private static final Set<String> ALLOWED_AUDIO_EXT = new HashSet<>(Arrays.asList(
            "mp3", "aac", "flac", "wav", "ogg"
    ));

    /** 允许的图片扩展名 */
    private static final Set<String> ALLOWED_IMAGE_EXT = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp"
    ));

    /** 允许的歌词扩展名 */
    private static final Set<String> ALLOWED_LRC_EXT = new HashSet<>(Arrays.asList(
            "lrc"
    ));

    /** 上传根目录的绝对路径 */
    private Path uploadBasePath;

    @PostConstruct
    public void init() {
        Path path = Paths.get(uploadDir);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        this.uploadBasePath = path.toAbsolutePath().normalize();
        log.info("音乐上传目录: {}", uploadBasePath);
    }

    @Override
    public Long addMusic(MusicAddRequest request, MultipartFile audioFile, MultipartFile coverFile, MultipartFile lrcFile) {
        if (request == null || StrUtil.isBlank(request.getTitle())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "歌曲名不能为空");
        }
        if (audioFile == null || audioFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "音频文件不能为空");
        }

        // 上传音频文件
        String audioUrl = uploadFile(audioFile, "audio", ALLOWED_AUDIO_EXT);

        // 上传封面图片（可选）
        String coverUrl = "";
        if (coverFile != null && !coverFile.isEmpty()) {
            coverUrl = uploadFile(coverFile, "images", ALLOWED_IMAGE_EXT);
        }

        // 上传歌词文件（可选）
        String lrcUrl = "";
        if (lrcFile != null && !lrcFile.isEmpty()) {
            lrcUrl = uploadFile(lrcFile, "lrc", ALLOWED_LRC_EXT);
        }

        // 保存数据库
        Music music = new Music();
        music.setTitle(request.getTitle());
        music.setArtist(request.getArtist() != null ? request.getArtist() : "");
        music.setCoverUrl(coverUrl);
        music.setAudioUrl(audioUrl);
        music.setLrcUrl(lrcUrl);
        music.setDuration(0); // 后续可以添加获取音频时长的逻辑
        music.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        boolean saved = this.save(music);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加音乐失败");
        }
        return music.getId();
    }

    @Override
    public boolean updateMusic(MusicEditRequest request, MultipartFile audioFile, MultipartFile coverFile, MultipartFile lrcFile) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "音乐ID不能为空");
        }

        Music music = this.getById(request.getId());
        if (music == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "音乐不存在");
        }

        // 更新基本信息
        if (StrUtil.isNotBlank(request.getTitle())) {
            music.setTitle(request.getTitle());
        }
        if (request.getArtist() != null) {
            music.setArtist(request.getArtist());
        }
        if (request.getSortOrder() != null) {
            music.setSortOrder(request.getSortOrder());
        }

        // 更新音频文件（可选）
        if (audioFile != null && !audioFile.isEmpty()) {
            String audioUrl = uploadFile(audioFile, "audio", ALLOWED_AUDIO_EXT);
            music.setAudioUrl(audioUrl);
        }

        // 更新封面图片（可选）
        if (coverFile != null && !coverFile.isEmpty()) {
            String coverUrl = uploadFile(coverFile, "images", ALLOWED_IMAGE_EXT);
            music.setCoverUrl(coverUrl);
        }

        // 更新歌词文件（可选）
        if (lrcFile != null && !lrcFile.isEmpty()) {
            String lrcUrl = uploadFile(lrcFile, "lrc", ALLOWED_LRC_EXT);
            music.setLrcUrl(lrcUrl);
        }

        return this.updateById(music);
    }

    @Override
    public boolean deleteMusic(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "音乐ID无效");
        }
        Music music = this.getById(id);
        if (music == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "音乐不存在");
        }
        return this.removeById(id);
    }

    @Override
    public boolean restoreMusic(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "音乐ID无效");
        }
        return baseMapper.restoreById(id) > 0;
    }

    @Override
    public boolean deleteMusicPermanently(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "音乐ID无效");
        }
        return baseMapper.deletePermanently(id) > 0;
    }

    @Override
    public Page<MusicVO> listByPage(MusicQueryRequest request) {
        long current = request.getCurrent() != null ? request.getCurrent() : 1;
        long size = request.getPageSize() != null ? request.getPageSize() : 10;

        Page<Music> page = this.page(new Page<>(current, size), getQueryWrapper(request));
        Page<MusicVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(getMusicVOList(page.getRecords()));
        return voPage;
    }

    @Override
    public List<MusicVO> listMusicPublic() {
        QueryWrapper<Music> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_order");
        List<Music> musicList = this.list(queryWrapper);
        return getMusicVOList(musicList);
    }

    @Override
    public MusicVO getMusicVO(Music music) {
        if (music == null) {
            return null;
        }
        MusicVO vo = new MusicVO();
        BeanUtil.copyProperties(music, vo);
        return vo;
    }

    @Override
    public List<MusicVO> getMusicVOList(List<Music> musicList) {
        if (CollUtil.isEmpty(musicList)) {
            return new ArrayList<>();
        }
        return musicList.stream().map(this::getMusicVO).toList();
    }

    @Override
    public QueryWrapper<Music> getQueryWrapper(MusicQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        String keyword = request.getKeyword();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        QueryWrapper<Music> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(keyword), "title", keyword);

        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), StrUtil.toUnderlineCase(sortField));
        } else {
            queryWrapper.orderByDesc("created_at");
        }

        return queryWrapper;
    }

    /**
     * 上传文件并返回访问 URL
     */
    private String uploadFile(MultipartFile file, String categoryDir, Set<String> allowedExts) {
        String originalName = file.getOriginalFilename();
        if (StrUtil.isBlank(originalName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名为空");
        }

        // 扩展名白名单校验
        String ext = FileUtils.getExtension(originalName);
        if (!allowedExts.contains(ext)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    "不支持的文件类型: ." + ext + "，允许的类型: " + String.join(", ", allowedExts));
        }

        // 文件大小校验（30MB）
        long maxSize = 30 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小超过 30MB 限制");
        }

        // 生成存储文件名和路径
        String storedName = FileUtils.generateStoredName(originalName);

        // 构建存储路径
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

        // 返回访问 URL
        return contextPath + "/uploads/" + categoryDir + "/" + storedName;
    }
}
