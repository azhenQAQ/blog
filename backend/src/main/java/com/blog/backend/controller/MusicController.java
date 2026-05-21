package com.blog.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.backend.annotation.AuthCheck;
import com.blog.backend.common.BaseResponse;
import com.blog.backend.common.ResultUtils;
import com.blog.backend.constant.UserConstant;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.model.dto.music.MusicAddRequest;
import com.blog.backend.model.dto.music.MusicEditRequest;
import com.blog.backend.model.dto.music.MusicQueryRequest;
import com.blog.backend.model.vo.music.MusicVO;
import com.blog.backend.service.MusicService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/music")
public class MusicController {

    @Resource
    private MusicService musicService;

    /**
     * 添加音乐（需要管理员权限）
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addMusic(
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
            @RequestParam(value = "lrcFile", required = false) MultipartFile lrcFile,
            @RequestParam("title") String title,
            @RequestParam(value = "artist", required = false) String artist,
            @RequestParam(value = "sortOrder", required = false) Integer sortOrder) {
        if (audioFile == null || audioFile.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "音频文件为空");
        }
        MusicAddRequest request = new MusicAddRequest();
        request.setTitle(title);
        request.setArtist(artist);
        request.setSortOrder(sortOrder);
        Long id = musicService.addMusic(request, audioFile, coverFile, lrcFile);
        return ResultUtils.success(id);
    }

    /**
     * 更新音乐（需要管理员权限）
     */
    @PutMapping("/edit")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateMusic(
            @RequestParam(value = "audioFile", required = false) MultipartFile audioFile,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
            @RequestParam(value = "lrcFile", required = false) MultipartFile lrcFile,
            @RequestParam("id") Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "artist", required = false) String artist,
            @RequestParam(value = "sortOrder", required = false) Integer sortOrder) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "音乐ID无效");
        }
        MusicEditRequest request = new MusicEditRequest();
        request.setId(id);
        request.setTitle(title);
        request.setArtist(artist);
        request.setSortOrder(sortOrder);
        boolean result = musicService.updateMusic(request, audioFile, coverFile, lrcFile);
        return ResultUtils.success(result);
    }

    /**
     * 删除音乐（软删除，需要管理员权限）
     */
    @DeleteMapping("/delete/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteMusic(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "音乐ID无效");
        }
        boolean result = musicService.deleteMusic(id);
        return ResultUtils.success(result);
    }

    /**
     * 恢复已删除的音乐（需要管理员权限）
     */
    @PutMapping("/restore/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> restoreMusic(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "音乐ID无效");
        }
        boolean result = musicService.restoreMusic(id);
        return ResultUtils.success(result);
    }

    /**
     * 永久删除音乐（需要管理员权限）
     */
    @DeleteMapping("/permanent/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteMusicPermanently(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "音乐ID无效");
        }
        boolean result = musicService.deleteMusicPermanently(id);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询音乐列表（需要管理员权限）
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<MusicVO>> listByPage(MusicQueryRequest request) {
        if (request == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        Page<MusicVO> page = musicService.listByPage(request);
        return ResultUtils.success(page);
    }

    /**
     * 获取单个音乐（需要管理员权限）
     */
    @GetMapping("/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<MusicVO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "音乐ID无效");
        }
        MusicVO vo = musicService.getMusicVO(musicService.getById(id));
        return ResultUtils.success(vo);
    }
}
