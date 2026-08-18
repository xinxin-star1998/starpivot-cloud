package cn.org.starpivot.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.config.AiRuntimeModelCatalog;
import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.domain.entity.AiConfig;
import cn.org.starpivot.ai.domain.entity.AiProvider;
import cn.org.starpivot.ai.domain.vo.AiModelVo;
import cn.org.starpivot.ai.mapper.AiConfigMapper;
import cn.org.starpivot.ai.provider.AiModelRef;
import cn.org.starpivot.ai.service.AiProviderService;
import cn.org.starpivot.ai.service.AiRuntimeConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRuntimeConfigServiceImpl implements AiRuntimeConfigService, SmartInitializingSingleton {

    private static final String STATUS_NORMAL = "0";
    private static final String DEFAULT_FLAG = "0";

    private final AiConfigMapper aiConfigMapper;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<AiProviderService> aiProviderService;

    private volatile AiRuntimeSnapshot cachedSnapshot;

    /**
     * 使用 SmartInitializingSingleton 代替 @PostConstruct，
     * 确保所有单例 Bean（包括 AiProviderService）都创建完毕后再初始化，
     * 避免与 AiProviderServiceImpl 之间的循环依赖。
     */
    @Override
    public void afterSingletonsInstantiated() {
        refresh();
    }

    @Override
    public void refresh() {
        cachedSnapshot = loadSnapshot();
        log.info("AI runtime config refreshed, defaultModel={} models={}",
                cachedSnapshot.getDefaultModel(),
                cachedSnapshot.getModels() != null ? cachedSnapshot.getModels().size() : 0);
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
        List<AiModelVo> models = mergeChatModels(parseModels(config.getModelsJson()));
        return AiRuntimeSnapshot.builder()
                .botName(config.getBotName())
                .botAvatar(config.getBotAvatar())
                .welcomeMessage(config.getWelcomeMessage())
                .systemPrompt(config.getSystemPrompt())
                .defaultModel(resolveDefaultModel(config.getDefaultModel(), models))
                .defaultTemperature(toDouble(config.getDefaultTemperature()))
                .maxMemoryMessages(config.getMaxMemoryMessages() != null ? config.getMaxMemoryMessages() : 30)
                .models(models)
                .ragEnabled(resolveRagEnabled(config.getRagEnabled()))
                .ragTopK(config.getRagTopK() != null ? config.getRagTopK() : 5)
                .defaultPromptScene(aiProperties.resolvedDefaultPromptScene())
                .promptTemplates(aiProperties.resolvedPromptTemplates(config.getSystemPrompt()))
                .build();
    }

    private AiRuntimeSnapshot fromProperties() {
        List<AiModelVo> models = mergeChatModels(aiProperties.resolvedModels());
        return AiRuntimeSnapshot.builder()
                .botName(aiProperties.resolvedBotName())
                .botAvatar(aiProperties.getBotAvatar())
                .welcomeMessage(aiProperties.getWelcomeMessage())
                .systemPrompt(aiProperties.getSystemPrompt())
                .defaultModel(resolveDefaultModel(aiProperties.resolvedDefaultModel(), models))
                .defaultTemperature(aiProperties.getDefaultTemperature())
                .maxMemoryMessages(aiProperties.getMaxMemoryMessages())
                .models(models)
                .ragEnabled(aiProperties.getRag().isEnabled())
                .ragTopK(5)
                .defaultPromptScene(aiProperties.resolvedDefaultPromptScene())
                .promptTemplates(aiProperties.resolvedPromptTemplates(null))
                .build();
    }

    /** Nacos 总开关 + 后台配置（0=开启 1=关闭）同时满足才启用 RAG */
    private boolean resolveRagEnabled(String ragEnabledFlag) {
        if (!aiProperties.getRag().isEnabled()) {
            return false;
        }
        return "0".equals(ragEnabledFlag);
    }

    private List<AiModelVo> mergeChatModels(List<AiModelVo> base) {
        AiProviderService providerService = aiProviderService.getIfAvailable();
        List<AiModelVo> providerModels = providerService != null ? providerService.listChatModels() : List.of();
        return AiRuntimeModelCatalog.copy(AiRuntimeModelCatalog.preferProviderModels(providerModels, base));
    }

    private String resolveDefaultModel(String configured, List<AiModelVo> models) {
        String providerDefault = null;
        AiProviderService providerService = aiProviderService.getIfAvailable();
        if (providerService != null) {
            AiProvider defaultChat = providerService.findDefaultChatProvider();
            if (defaultChat != null && StringUtils.hasText(defaultChat.getDefaultChatModel())) {
                providerDefault = AiModelRef.encode(
                        defaultChat.getProviderId(), defaultChat.getDefaultChatModel().trim());
            }
        }
        return AiRuntimeModelCatalog.resolveDefaultModel(providerDefault, configured, models);
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
