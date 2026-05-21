package com.blog.backend.controller;

import com.blog.backend.common.BaseResponse;
import com.blog.backend.common.ResultUtils;
import com.blog.backend.model.vo.music.MusicVO;
import com.blog.backend.service.MusicService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/music/public")
public class MusicPublicController {

    @Resource
    private MusicService musicService;

    /**
     * 获取所有活跃音乐列表（公开接口）
     */
    @GetMapping("/list")
    public BaseResponse<List<MusicVO>> listMusic() {
        List<MusicVO> list = musicService.listMusicPublic();
        return ResultUtils.success(list);
    }
}
