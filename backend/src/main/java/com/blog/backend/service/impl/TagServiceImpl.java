package com.blog.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.backend.exception.BusinessException;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.mapper.ArticleTagMapper;
import com.blog.backend.mapper.TagMapper;
import com.blog.backend.model.dto.tag.TagAddRequest;
import com.blog.backend.model.dto.tag.TagQueryRequest;
import com.blog.backend.model.dto.tag.TagUpdateRequest;
import com.blog.backend.model.entity.ArticleTag;
import com.blog.backend.model.entity.Tag;
import com.blog.backend.model.vo.tag.TagVO;
import com.blog.backend.service.TagService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Resource
    private ArticleTagMapper articleTagMapper;

    @Override
    public Long addTag(TagAddRequest request) {
        if (request == null || StrUtil.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称不能为空");
        }
        if (request.getName().length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称不能超过50个字符");
        }

        // 名称唯一性校验
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", request.getName());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称已存在");
        }

        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        boolean saved = this.save(tag);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加标签失败");
        }
        return tag.getId();
    }

    @Override
    public boolean updateTag(TagUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签ID不能为空");
        }

        Tag tag = this.getById(request.getId());
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "标签不存在");
        }

        // 名称唯一性校验（排除自身）
        if (StrUtil.isNotBlank(request.getName())) {
            if (request.getName().length() > 50) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称不能超过50个字符");
            }
            QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", request.getName());
            queryWrapper.ne("id", request.getId());
            long count = this.count(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称已存在");
            }
            tag.setName(request.getName());
        }

        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }

        return this.updateById(tag);
    }

    @Override
    public boolean deleteTag(Long id, boolean force) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签ID无效");
        }

        Tag tag = this.getById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "标签不存在");
        }

        // 检查关联文章数
        long count = articleTagMapper.selectCount(
                new QueryWrapper<ArticleTag>().eq("tag_id", id));
        if (count > 0 && !force) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "该标签关联了 " + count + " 篇文章，确认将同时移除关联关系");
        }

        // 移除关联
        if (count > 0) {
            articleTagMapper.delete(new QueryWrapper<ArticleTag>().eq("tag_id", id));
        }

        return this.removeById(id);
    }

    @Override
    public Page<TagVO> listByPage(TagQueryRequest request) {
        long current = request.getCurrent() != null ? request.getCurrent() : 1;
        long size = request.getPageSize() != null ? request.getPageSize() : 10;

        Page<Tag> page = this.page(new Page<>(current, size), getQueryWrapper(request));
        Page<TagVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(getTagVOList(page.getRecords()));
        return voPage;
    }

    @Override
    public List<TagVO> listAll() {
        List<Tag> tagList = this.list();
        return getTagVOList(tagList);
    }

    @Override
    public List<TagVO> listByArticleId(Long articleId) {
        if (articleId == null || articleId <= 0) {
            return new ArrayList<>();
        }
        QueryWrapper<ArticleTag> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId);
        List<ArticleTag> articleTagList = articleTagMapper.selectList(wrapper);
        if (CollUtil.isEmpty(articleTagList)) {
            return new ArrayList<>();
        }
        List<Long> tagIds = articleTagList.stream()
                .map(ArticleTag::getTagId)
                .collect(Collectors.toList());
        List<Tag> tagList = this.listByIds(tagIds);
        return getTagVOList(tagList);
    }

    @Override
    public TagVO getTagVO(Tag tag) {
        if (tag == null) {
            return null;
        }
        TagVO vo = new TagVO();
        BeanUtil.copyProperties(tag, vo);

        // 计算关联文章数
        long count = articleTagMapper.selectCount(
                new QueryWrapper<ArticleTag>().eq("tag_id", tag.getId()));
        vo.setArticleCount((int) count);

        return vo;
    }

    @Override
    public List<TagVO> getTagVOList(List<Tag> tagList) {
        if (CollUtil.isEmpty(tagList)) {
            return new ArrayList<>();
        }
        return tagList.stream().map(this::getTagVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<Tag> getQueryWrapper(TagQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        String keyword = request.getKeyword();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(keyword), "name", keyword);

        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), StrUtil.toUnderlineCase(sortField));
        } else {
            queryWrapper.orderByDesc("created_at");
        }

        return queryWrapper;
    }
}
