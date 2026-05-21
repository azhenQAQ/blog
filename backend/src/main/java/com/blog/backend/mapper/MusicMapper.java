package com.blog.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.backend.model.entity.Music;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface MusicMapper extends BaseMapper<Music> {

    /**
     * 恢复音乐（清除 deleted_at）
     */
    @Update("UPDATE music SET deleted_at = NULL WHERE id = #{id}")
    int restoreById(@Param("id") Long id);

    /**
     * 物理删除音乐
     */
    @Update("DELETE FROM music WHERE id = #{id}")
    int deletePermanently(@Param("id") Long id);
}
