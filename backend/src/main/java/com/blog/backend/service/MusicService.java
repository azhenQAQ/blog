package com.blog.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.backend.model.dto.music.MusicAddRequest;
import com.blog.backend.model.dto.music.MusicEditRequest;
import com.blog.backend.model.dto.music.MusicQueryRequest;
import com.blog.backend.model.entity.Music;
import com.blog.backend.model.vo.music.MusicVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MusicService extends IService<Music> {

    /**
     * 添加音乐（包含文件上传）
     */
    Long addMusic(MusicAddRequest request, MultipartFile audioFile, MultipartFile coverFile, MultipartFile lrcFile);

    /**
     * 更新音乐（可选择更新文件）
     */
    boolean updateMusic(MusicEditRequest request, MultipartFile audioFile, MultipartFile coverFile, MultipartFile lrcFile);

    /**
     * 删除音乐（软删除）
     */
    boolean deleteMusic(Long id);

    /**
     * 恢复已删除的音乐
     */
    boolean restoreMusic(Long id);

    /**
     * 永久删除音乐
     */
    boolean deleteMusicPermanently(Long id);

    /**
     * 分页查询音乐列表（管理员）
     */
    Page<MusicVO> listByPage(MusicQueryRequest request);

    /**
     * 查询所有活跃音乐（公开接口，按排序升序）
     */
    List<MusicVO> listMusicPublic();

    /**
     * 获取脱敏音乐视图
     */
    MusicVO getMusicVO(Music music);

    /**
     * 获取脱敏音乐视图列表
     */
    List<MusicVO> getMusicVOList(List<Music> musicList);

    /**
     * 获取查询条件
     */
    QueryWrapper<Music> getQueryWrapper(MusicQueryRequest request);
}
