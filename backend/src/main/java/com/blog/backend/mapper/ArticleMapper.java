package com.blog.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.backend.model.entity.Article;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 分页查询（绕过 @TableLogic，用于回收站）
     */
    @Select("SELECT * FROM article ${ew.customSqlSegment}")
    Page<Article> selectPageWithDeleted(Page<Article> page, @Param(Constants.WRAPPER) QueryWrapper<Article> wrapper);

    /**
     * 查询已删除文章（绕过 @TableLogic）
     */
    @Select("SELECT * FROM article WHERE id = #{id} AND deleted_at IS NOT NULL")
    Article selectDeletedById(@Param("id") Long id);

    /**
     * 恢复文章（清除 deleted_at）
     */
    @Update("UPDATE article SET deleted_at = NULL WHERE id = #{id}")
    int restoreById(@Param("id") Long id);

    /**
     * 物理删除文章（绕过 @TableLogic）
     */
    @Update("DELETE FROM article WHERE id = #{id}")
    int deletePermanently(@Param("id") Long id);
}
