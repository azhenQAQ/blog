package com.blog.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.backend.model.entity.Article;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

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

    /**
     * 递增浏览量
     */
    @Update("UPDATE article SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 查询上一篇已发布文章（created_at 小于当前文章，非置顶，已发布）
     */
    @Select("SELECT * FROM article WHERE status = 1 AND is_top = 0 AND created_at < #{createdAt} ORDER BY created_at DESC LIMIT 1")
    Article selectPreviousPublished(@Param("createdAt") java.time.LocalDateTime createdAt);

    /**
     * 查询下一篇已发布文章（created_at 大于当前文章，非置顶，已发布）
     */
    @Select("SELECT * FROM article WHERE status = 1 AND is_top = 0 AND created_at > #{createdAt} ORDER BY created_at ASC LIMIT 1")
    Article selectNextPublished(@Param("createdAt") java.time.LocalDateTime createdAt);

    /**
     * 查询所有文章的总浏览量（排除逻辑删除）
     */
    @Select("SELECT COALESCE(SUM(view_count), 0) FROM article WHERE deleted_at IS NULL")
    Integer selectTotalViewCount();

    /**
     * 按状态分组统计文章数（排除逻辑删除）
     */
    @Select("SELECT status, COUNT(*) AS cnt FROM article WHERE deleted_at IS NULL GROUP BY status")
    List<Map<String, Object>> selectArticleCountGroupByStatus();
}
