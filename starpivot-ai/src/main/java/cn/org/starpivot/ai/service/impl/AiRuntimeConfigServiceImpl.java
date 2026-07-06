package cn.org.starpivot.ai.service.impl;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.domain.entity.AiConfig;
import cn.org.starpivot.ai.domain.vo.AiModelVo;
import cn.org.starpivot.ai.mapper.AiConfigMapper;
import cn.org.starpivot.ai.service.AiRuntimeConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRuntimeConfigServiceImpl implements AiRuntimeConfigService {

    private static final String STATUS_NORMAL = "0";
    private static final String DEFAULT_FLAG = "0";

    private final AiConfigMapper aiConfigMapper;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    private volatile AiRuntimeSnapshot cachedSnapshot;

    @PostConstruct
    @Override
    public void refresh() {
        cachedSnapshot = loadSnapshot();
        log.info("AI runtime config refreshed, source={}", cachedSnapshot.getDefaultModel());
    }

    @Override
    public AiRuntimeSnapshot current() {
        AiRuntimeSnapshot snapshot = cachedSnapshot;
        if (snapshot == null) {
            refresh();
            snapshot = cachedSnapshot;
        }
        return snapshot;
    }

    private AiRuntimeSnapshot loadSnapshot() {
        AiConfig config = aiConfigMapper.selectOne(new LambdaQueryWrapper<AiConfig>()
                .eq(AiConfig::getIsDefault, DEFAULT_FLAG)
                .eq(AiConfig::getStatus, STATUS_NORMAL)
                .orderByAsc(AiConfig::getConfigId)
                .last("LIMIT 1"));
        if (config != null) {
            return fromEntity(config);
        }
        return fromProperties();
    }

    private AiRuntimeSnapshot fromEntity(AiConfig config) {
        return AiRuntimeSnapshot.builder()
                .botName(config.getBotName())
                .botAvatar(config.getBotAvatar())
                .welcomeMessage(config.getWelcomeMessage())
                .systemPrompt(config.getSystemPrompt())
                .defaultModel(config.getDefaultModel())
                .defaultTemperature(toDouble(config.getDefaultTemperature()))
                .maxMemoryMessages(config.getMaxMemoryMessages() != null ? config.getMaxMemoryMessages() : 30)
                .models(parseModels(config.getModelsJson()))
                .ragEnabled(resolveRagEnabled(config.getRagEnabled()))
                .ragTopK(config.getRagTopK() != null ? config.getRagTopK() : 5)
                .build();
    }

    private AiRuntimeSnapshot fromProperties() {
        return AiRuntimeSnapshot.builder()
                .botName(aiProperties.resolvedBotName())
                .botAvatar(aiProperties.getBotAvatar())
                .welcomeMessage(aiProperties.getWelcomeMessage())
                .systemPrompt(aiProperties.getSystemPrompt())
                .defaultModel(aiProperties.resolvedDefaultModel())
                .defaultTemperature(aiProperties.getDefaultTemperature())
                .maxMemoryMessages(aiProperties.getMaxMemoryMessages())
                .models(aiProperties.resolvedModels())
                .ragEnabled(aiProperties.getRag().isEnabled())
                .ragTopK(5)
                .build();
    }

    /** Nacos 总开关 + 后台配置（0=开启 1=关闭）同时满足才启用 RAG */
    private boolean resolveRagEnabled(String ragEnabledFlag) {
        if (!aiProperties.getRag().isEnabled()) {
            return false;
        }
        return "0".equals(ragEnabledFlag);
    }

    private List<AiModelVo> parseModels(String modelsJson) {
        if (!StringUtils.hasText(modelsJson)) {
            return new ArrayList<>();
        }
        try {
            List<AiModelVo> models = objectMapper.readValue(modelsJson, new TypeReference<>() {});
            return models != null ? models : new ArrayList<>();
        } catch (Exception ex) {
            log.warn("Failed to parse ai_config.models_json, fallback to empty list", ex);
            return new ArrayList<>();
        }
    }

    private Double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }
}
