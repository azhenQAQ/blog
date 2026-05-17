package com.blog.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.backend.model.dto.tag.TagAddRequest;
import com.blog.backend.model.dto.tag.TagQueryRequest;
import com.blog.backend.model.dto.tag.TagUpdateRequest;
import com.blog.backend.model.entity.Tag;
import com.blog.backend.model.vo.tag.TagVO;

import java.util.List;

public interface TagService extends IService<Tag> {

    /**
     * 添加标签
     */
    Long addTag(TagAddRequest request);

    /**
     * 更新标签
     */
    boolean updateTag(TagUpdateRequest request);

    /**
     * 删除标签
     * @param id 标签ID
     * @param force 是否强制删除（移除关联后删除）
     */
    boolean deleteTag(Long id, boolean force);

    /**
     * 分页查询标签列表
     */
    Page<TagVO> listByPage(TagQueryRequest request);

    /**
     * 获取所有标签（文章多选用）
     */
    List<TagVO> listAll();

    /**
     * 查询某文章关联的标签
     */
    List<TagVO> listByArticleId(Long articleId);

    /**
     * 获取脱敏标签视图
     */
    TagVO getTagVO(Tag tag);

    /**
     * 获取脱敏标签视图列表
     */
    List<TagVO> getTagVOList(List<Tag> tagList);

    /**
     * 获取查询条件
     */
    QueryWrapper<Tag> getQueryWrapper(TagQueryRequest request);
}
