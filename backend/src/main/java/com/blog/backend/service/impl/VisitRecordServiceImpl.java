package com.blog.backend.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.backend.common.IpRegionUtil;
import com.blog.backend.mapper.VisitRecordMapper;
import com.blog.backend.model.dto.visit.VisitRecordQueryRequest;
import com.blog.backend.model.entity.VisitRecord;
import com.blog.backend.model.vo.visit.VisitRecordVO;
import com.blog.backend.service.VisitRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 访问记录服务实现
 */
@Service
public class VisitRecordServiceImpl extends ServiceImpl<VisitRecordMapper, VisitRecord> implements VisitRecordService {

    private static final Pattern ARTICLE_PATH_PATTERN = Pattern.compile("^/api/article/public/(\\d+)$");

    @Resource
    private IpRegionUtil ipRegionUtil;

    @Override
    public void recordVisit(String path, String ip, String userAgent, String referer) {
        VisitRecord record = new VisitRecord();
        record.setPath(path);
        record.setIp(ip);
        record.setUserAgent(userAgent);
        record.setReferer(referer);

        // 提取文章ID，仅记录关联不重复递增浏览量（由 ArticleServiceImpl 处理）
        Long articleId = extractArticleId(path);
        if (articleId != null) {
            record.setArticleId(articleId);
        }

        // 解析 IP 地理位置
        record.setLocation(ipRegionUtil.search(ip));

        // 解析 User-Agent 获取浏览器摘要
        record.setBrowserSummary(parseBrowserSummary(userAgent));

        this.save(record);
    }

    @Override
    public Page<VisitRecordVO> listByPage(VisitRecordQueryRequest request) {
        long current = request.getCurrent() != null ? request.getCurrent() : 1L;
        long size = request.getPageSize() != null ? request.getPageSize() : 10L;
        Page<VisitRecord> page = this.page(new Page<>(current, size), getQueryWrapper(request));
        Page<VisitRecordVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(getVisitRecordVOList(page.getRecords()));
        return voPage;
    }

    @Override
    public VisitRecordVO getVisitRecordVO(VisitRecord visitRecord) {
        if (visitRecord == null) {
            return null;
        }
        VisitRecordVO vo = new VisitRecordVO();
        vo.setId(visitRecord.getId());
        vo.setPath(visitRecord.getPath());
        vo.setIp(visitRecord.getIp());
        vo.setUserAgent(visitRecord.getUserAgent());
        vo.setReferer(visitRecord.getReferer());
        vo.setArticleId(visitRecord.getArticleId());
        vo.setLocation(visitRecord.getLocation());
        vo.setBrowserSummary(visitRecord.getBrowserSummary());
        vo.setCreatedAt(visitRecord.getCreatedAt());
        return vo;
    }

    @Override
    public QueryWrapper<VisitRecord> getQueryWrapper(VisitRecordQueryRequest request) {
        QueryWrapper<VisitRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(request.getPath()), "path", request.getPath());
        queryWrapper.like(StrUtil.isNotBlank(request.getIp()), "ip", request.getIp());
        queryWrapper.like(StrUtil.isNotBlank(request.getReferer()), "referer", request.getReferer());
        queryWrapper.orderByDesc("created_at");
        return queryWrapper;
    }

    /**
     * 从访问路径中提取文章ID
     */
    private Long extractArticleId(String path) {
        if (StrUtil.isBlank(path)) {
            return null;
        }
        Matcher matcher = ARTICLE_PATH_PATTERN.matcher(path);
        if (matcher.matches()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 解析 User-Agent 为浏览器摘要（浏览器+版本 / 操作系统 / 设备类型）
     */
    private String parseBrowserSummary(String userAgent) {
        if (StrUtil.isBlank(userAgent)) {
            return "Unknown / Unknown / Unknown";
        }
        try {
            UserAgent ua = UserAgentUtil.parse(userAgent);
            if (ua == null) {
                return "Unknown / Unknown / Unknown";
            }

            String browser = ua.getBrowser() != null ? ua.getBrowser().getName() : "Unknown";
            String version = ua.getVersion();
            String browserStr;
            if (StrUtil.isNotBlank(browser) && !"Unknown".equals(browser)
                    && StrUtil.isNotBlank(version)) {
                String mainVer = version.contains(".") ? version.split("\\.")[0] : version;
                browserStr = browser + " " + mainVer;
            } else if (StrUtil.isNotBlank(browser) && !"Unknown".equals(browser)) {
                browserStr = browser;
            } else {
                browserStr = "Unknown";
            }

            String os = ua.getOs() != null ? ua.getOs().getName() : "Unknown";
            String platform = ua.getPlatform() != null ? ua.getPlatform().getName() : "Unknown";

            return browserStr + " / " + os + " / " + platform;
        } catch (Exception e) {
            return "Unknown / Unknown / Unknown";
        }
    }

    /**
     * 批量脱敏转换
     */
    private List<VisitRecordVO> getVisitRecordVOList(List<VisitRecord> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        return records.stream()
                .map(this::getVisitRecordVO)
                .collect(Collectors.toList());
    }
}
