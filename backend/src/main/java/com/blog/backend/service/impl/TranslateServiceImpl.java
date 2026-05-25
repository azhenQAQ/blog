package com.blog.backend.service.impl;

import com.blog.backend.config.TranslateConfig;
import com.blog.backend.exception.BusinessException;
import com.blog.backend.exception.ErrorCode;
import com.blog.backend.service.TranslateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Service
public class TranslateServiceImpl implements TranslateService {

    private static final String BAIDU_API_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate";

    @Resource
    private TranslateConfig translateConfig;

    @Resource
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String translate(String text) {
        String appId = translateConfig.getAppId();
        String key = translateConfig.getKey();

        if (appId == null || appId.isBlank() || key == null || key.isBlank()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "翻译服务未配置");
        }

        String salt = String.valueOf(System.currentTimeMillis());
        String sign = DigestUtils.md5DigestAsHex(
                (appId + text + salt + key).getBytes(StandardCharsets.UTF_8)
        );

        try {
            String url = BAIDU_API_URL
                    + "?q={q}&from=auto&to=auto"
                    + "&appid={appid}&salt={salt}&sign={sign}";

            String response = restTemplate.getForObject(
                    url, String.class, text, appId, salt, sign
            );
            JsonNode root = objectMapper.readTree(response);

            // 检查错误码
            JsonNode errorCode = root.get("error_code");
            if (errorCode != null && !"0".equals(errorCode.asText()) && !"52000".equals(errorCode.asText())) {
                String errorMsg = root.has("error_msg") ? root.get("error_msg").asText() : "未知错误";
                throw new BusinessException(ErrorCode.OPERATION_ERROR,
                        "翻译失败 [" + errorCode.asText() + "]: " + errorMsg);
            }

            // 提取翻译结果
            JsonNode transResult = root.get("trans_result");
            if (transResult == null || !transResult.isArray() || transResult.isEmpty()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "翻译结果为空");
            }

            StringBuilder result = new StringBuilder();
            for (JsonNode item : transResult) {
                if (result.length() > 0) result.append("\n");
                result.append(item.get("dst").asText());
            }
            return result.toString();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("翻译请求失败: " + e.getMessage(), e);
        }
    }
}
