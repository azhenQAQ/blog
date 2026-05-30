package com.blog.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.backend.model.entity.ArticleTag;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    /**
     * 按标签分组统计文章数
     */
    @Select("SELECT tag_id, COUNT(*) AS cnt FROM article_tag GROUP BY tag_id")
    java.util.List<Map<String, Object>> selectArticleCountGroupByTag();
}
