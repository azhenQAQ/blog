package com.blog.backend.common;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * IP 地址离线定位工具 — 基于 ip2region
 */
@Slf4j
@Component
public class IpRegionUtil implements InitializingBean {

    private Searcher searcher;

    @Override
    public void afterPropertiesSet() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("ip2region.xdb")) {
            if (is == null) {
                log.warn("ip2region.xdb 未在 classpath 中找到，IP 定位功能不可用");
                return;
            }
            byte[] cBuff = IoUtil.readBytes(is);
            searcher = Searcher.newWithBuffer(cBuff);
            log.info("ip2region 离线库加载成功，共 {} 字节", cBuff.length);
        } catch (Exception e) {
            log.error("ip2region 初始化失败", e);
        }
    }

    /**
     * 查询 IP 所在地理位置，返回格式化地址
     *
     * @return 如「浙江·杭州」、「日本·东京都·千代田区」、解析失败返回 "Unknown"
     */
    public String search(String ip) {
        if (searcher == null || StrUtil.isBlank(ip)) {
            return "Unknown";
        }
        try {
            String region = searcher.search(ip);
            return formatRegion(region);
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * 将 ip2region 原始格式「国家|区域|省份|城市|运营商」转为简洁地址
     */
    private String formatRegion(String region) {
        if (StrUtil.isBlank(region)) {
            return "Unknown";
        }
        String[] parts = region.split("\\|");
        String country = getSafe(parts, 0);
        String province = getSafe(parts, 2);
        String city = getSafe(parts, 3);

        String formatted;
        if ("中国".equals(country)) {
            formatted = formatChina(province, city);
        } else if (StrUtil.isNotBlank(country) && !"0".equals(country)) {
            formatted = formatForeign(country, province, city);
        } else {
            formatted = formatChina(province, city);
        }
        return StrUtil.isNotBlank(formatted) ? formatted : "Unknown";
    }

    private String formatChina(String province, String city) {
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotBlank(province) && !"0".equals(province)) {
            sb.append(province);
        }
        if (StrUtil.isNotBlank(city) && !"0".equals(city) && !city.equals(province)) {
            if (!sb.isEmpty()) sb.append("·");
            sb.append(city);
        }
        return sb.toString();
    }

    private String formatForeign(String country, String province, String city) {
        StringBuilder sb = new StringBuilder(country);
        if (StrUtil.isNotBlank(province) && !"0".equals(province)) {
            sb.append("·").append(province);
        }
        if (StrUtil.isNotBlank(city) && !"0".equals(city) && !city.equals(province)) {
            sb.append("·").append(city);
        }
        return sb.toString();
    }

    private String getSafe(String[] arr, int index) {
        return (arr != null && index < arr.length) ? StrUtil.trim(arr[index]) : "";
    }

    @PreDestroy
    public void destroy() {
        if (searcher != null) {
            try {
                searcher.close();
            } catch (Exception e) {
                log.warn("关闭 ip2region Searcher 异常", e);
            }
        }
    }
}
