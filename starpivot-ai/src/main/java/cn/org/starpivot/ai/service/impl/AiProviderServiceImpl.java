package cn.org.starpivot.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.domain.dto.AiProviderQueryDto;
import cn.org.starpivot.ai.domain.dto.AiProviderSaveDto;
import cn.org.starpivot.ai.domain.entity.AiProvider;
import cn.org.starpivot.ai.domain.vo.AiModelVo;
import cn.org.starpivot.ai.domain.vo.AiProviderModelVo;
import cn.org.starpivot.ai.domain.vo.AiProviderPresetVo;
import cn.org.starpivot.ai.domain.vo.AiProviderVo;
import cn.org.starpivot.ai.mapper.AiProviderMapper;
import cn.org.starpivot.ai.provider.AiModelClientFactory;
import cn.org.starpivot.ai.provider.AiModelRef;
import cn.org.starpivot.ai.provider.AiProviderChangedEvent;
import cn.org.starpivot.ai.provider.AiProviderCrypto;
import cn.org.starpivot.ai.provider.AiProviderPresetCatalog;
import cn.org.starpivot.ai.provider.AiProviderSecretUtils;
import cn.org.starpivot.ai.service.AiProviderService;
import cn.org.starpivot.ai.service.AiRuntimeConfigService;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiProviderServiceImpl implements AiProviderService, SmartInitializingSingleton {

    private static final String YES = "0";
    private static final String NO = "1";
    private static final String STATUS_NORMAL = "0";

    private final AiProviderMapper aiProviderMapper;
    private final ObjectMapper objectMapper;
    private final AiProviderPresetCatalog presetCatalog;
    private final AiRuntimeConfigService aiRuntimeConfigService;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectProvider<AiModelClientFactory> aiModelClientFactory;
    private final AiProperties aiProperties;

    private volatile List<AiProvider> enabledCache = List.of();
    private volatile boolean loaded;

    @Override
    public void afterSingletonsInstantiated() {
        refreshRuntime();
    }

    @Override
    public void refreshRuntime() {
        try {
            List<AiProvider> rows = aiProviderMapper.selectList(new LambdaQueryWrapper<AiProvider>()
                    .eq(AiProvider::getStatus, STATUS_NORMAL)
                    .orderByAsc(AiProvider::getProviderId));
            List<AiProvider> unlocked = new ArrayList<>(rows.size());
            for (AiProvider row : rows) {
                unlocked.add(withPlainApiKey(row));
            }
            enabledCache = unlocked;
            migratePlaintextKeysAsync(rows);
        } catch (RuntimeException ex) {
            log.warn("[AI Provider] load skipped (table may not exist yet): {}", ex.getMessage());
            enabledCache = List.of();
            loaded = true;
            return;
        }
        loaded = true;
        eventPublisher.publishEvent(new AiProviderChangedEvent(this));
        log.info("[AI Provider] runtime refreshed, enabled={}", enabledCache.size());
    }

    @Override
    public List<AiProvider> listEnabled() {
        if (!loaded) {
            refreshRuntime();
        }
        List<AiProvider> cached = enabledCache;
        return cached != null ? cached : List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiProviderVo> pageList(AiProviderQueryDto query) {
        Page<AiProvider> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AiProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getProviderName()), AiProvider::getProviderName, query.getProviderName())
                .eq(StringUtils.hasText(query.getProviderCode()), AiProvider::getProviderCode, query.getProviderCode())
                .eq(StringUtils.hasText(query.getStatus()), AiProvider::getStatus, query.getStatus())
                .orderByAsc(AiProvider::getStatus)
                .orderByAsc(AiProvider::getProviderId);
        Page<AiProvider> result = aiProviderMapper.selectPage(page, wrapper);
        PageResponse<AiProviderVo> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setRows(result.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AiProviderVo getById(Long providerId) {
        return toVo(requireProvider(providerId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(AiProviderSaveDto dto) {
        LocalDateTime now = LocalDateTime.now();
        String operator = currentOperator();
        AiProvider entity;
        if (dto.getProviderId() != null) {
            entity = requireProvider(dto.getProviderId());
        } else {
            entity = new AiProvider();
            entity.setCreateBy(operator);
            entity.setCreateTime(now);
        }
        ensureNameUnique(dto.getProviderName(), dto.getProviderId());
        entity.setProviderCode(dto.getProviderCode().trim().toLowerCase());
        entity.setProviderName(dto.getProviderName().trim());
        entity.setBaseUrl(trimSlash(dto.getBaseUrl()));
        if (!AiProviderSecretUtils.isUnchanged(dto.getApiKey())) {
            entity.setApiKey(encryptKey(dto.getApiKey().trim()));
        } else if (dto.getProviderId() == null) {
            throw new BizException("请填写 API Key");
        }
        entity.setCompletionsPath(trimToNull(dto.getCompletionsPath()));
        entity.setEmbeddingsPath(trimToNull(dto.getEmbeddingsPath()));
        entity.setRerankEndpoint(trimToNull(dto.getRerankEndpoint()));
        entity.setChatEnabled(flag(dto.getChatEnabled(), YES));
        entity.setEmbeddingEnabled(flag(dto.getEmbeddingEnabled(), NO));
        entity.setRerankEnabled(flag(dto.getRerankEnabled(), NO));
        entity.setDefaultChatModel(trimToNull(dto.getDefaultChatModel()));
        entity.setDefaultEmbeddingModel(trimToNull(dto.getDefaultEmbeddingModel()));
        entity.setDefaultRerankModel(trimToNull(dto.getDefaultRerankModel()));
        if (YES.equals(entity.getEmbeddingEnabled()) && !supportsEmbedding(entity)) {
            throw new BizException("该供应商不提供向量模型，请改用阿里百炼 / OpenAI / 智谱 / 硅基流动");
        }
        if (YES.equals(entity.getEmbeddingEnabled()) && !StringUtils.hasText(entity.getDefaultEmbeddingModel())) {
            throw new BizException("启用向量能力时请填写默认向量模型（如 text-embedding-v3）");
        }
        entity.setModelsJson(serializeModels(dto.getModels()));
        entity.setStatus(flag(dto.getStatus(), STATUS_NORMAL));
        entity.setRemark(trimToNull(dto.getRemark()));
        entity.setUpdateBy(operator);
        entity.setUpdateTime(now);

        boolean defaultChat = YES.equals(dto.getIsDefaultChat());
        boolean defaultEmbedding = YES.equals(dto.getIsDefaultEmbedding());
        boolean defaultRerank = YES.equals(dto.getIsDefaultRerank());
        if (dto.getProviderId() == null) {
            entity.setIsDefaultChat(defaultChat ? YES : NO);
            entity.setIsDefaultEmbedding(defaultEmbedding ? YES : NO);
            entity.setIsDefaultRerank(defaultRerank ? YES : NO);
            aiProviderMapper.insert(entity);
        } else {
            if (dto.getIsDefaultChat() != null) {
                entity.setIsDefaultChat(defaultChat ? YES : entity.getIsDefaultChat());
            }
            if (dto.getIsDefaultEmbedding() != null) {
                entity.setIsDefaultEmbedding(defaultEmbedding ? YES : entity.getIsDefaultEmbedding());
            }
            if (dto.getIsDefaultRerank() != null) {
                entity.setIsDefaultRerank(defaultRerank ? YES : entity.getIsDefaultRerank());
            }
            aiProviderMapper.updateById(entity);
        }

        if (defaultChat) {
            clearOtherDefaults(entity.getProviderId(), "chat");
        }
        if (defaultEmbedding) {
            clearOtherDefaults(entity.getProviderId(), "embedding");
        }
        if (defaultRerank) {
            clearOtherDefaults(entity.getProviderId(), "rerank");
        }

        refreshRuntime();
        aiRuntimeConfigService.refresh();
        return entity.getProviderId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long providerId) {
        AiProvider entity = requireProvider(providerId);
        aiProviderMapper.deleteById(entity.getProviderId());
        refreshRuntime();
        aiRuntimeConfigService.refresh();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long providerId, String kind) {
        AiProvider entity = requireProvider(providerId);
        if (!STATUS_NORMAL.equals(entity.getStatus())) {
            throw new BizException("停用的供应商不能设为默认");
        }
        String normalized = kind == null ? "chat" : kind.trim().toLowerCase();
        if ("embedding".equals(normalized)) {
            if (!YES.equals(entity.getEmbeddingEnabled()) || !supportsEmbedding(entity)) {
                throw new BizException("该供应商未启用或不支持向量能力，无法设为默认向量供应商");
            }
            if (!StringUtils.hasText(entity.getDefaultEmbeddingModel())) {
                throw new BizException("请先填写默认向量模型");
            }
            entity.setIsDefaultEmbedding(YES);
        } else if ("rerank".equals(normalized)) {
            entity.setIsDefaultRerank(YES);
        } else {
            entity.setIsDefaultChat(YES);
        }
        entity.setUpdateBy(currentOperator());
        entity.setUpdateTime(LocalDateTime.now());
        aiProviderMapper.updateById(entity);
        clearOtherDefaults(providerId, normalized);
        refreshRuntime();
        aiRuntimeConfigService.refresh();
    }

    @Override
    public List<AiProviderPresetVo> presets() {
        return presetCatalog.list();
    }

    @Override
    public List<AiModelVo> listChatModels() {
        Map<String, AiModelVo> unique = new LinkedHashMap<>();
        for (AiProvider provider : chatProvidersByPriority()) {
            for (AiProviderModelVo model : parseModels(provider.getModelsJson())) {
                if (model == null || !StringUtils.hasText(model.getId())) {
                    continue;
                }
                if (StringUtils.hasText(model.getKind()) && !"chat".equalsIgnoreCase(model.getKind())) {
                    continue;
                }
                AiModelVo vo = toChatModelVo(provider, model.getId(), model.getLabel());
                unique.putIfAbsent(vo.getId(), vo);
            }
            if (StringUtils.hasText(provider.getDefaultChatModel())) {
                AiModelVo vo = toChatModelVo(provider, provider.getDefaultChatModel(), provider.getDefaultChatModel());
                unique.putIfAbsent(vo.getId(), vo);
            }
        }
        return new ArrayList<>(unique.values());
    }

    @Override
    public String testConnection(Long providerId, String kind) {
        AiProvider provider = requireProvider(providerId);
        if (!hasApiKey(provider)) {
            throw new BizException("请先填写 API Key");
        }
        String normalized = kind == null ? "chat" : kind.trim().toLowerCase();
        AiModelClientFactory factory = aiModelClientFactory.getObject();
        AiProvider unlocked = withPlainApiKey(provider);
        try {
            if ("embedding".equals(normalized)) {
                float[] vector = factory.embedWith(unlocked, "ping");
                return "向量接口正常，维度=" + vector.length;
            }
            String reply = factory.pingChat(unlocked);
            return StringUtils.hasText(reply) ? "对话接口正常" : "对话接口已连通，但返回为空";
        } catch (RuntimeException ex) {
            throw new BizException("连通性测试失败：" + rootMessage(ex));
        }
    }

    @Override
    public AiProvider findChatProvider(String model) {
        List<AiProvider> enabled = chatProvidersByPriority();
        if (enabled.isEmpty()) {
            return null;
        }
        if (StringUtils.hasText(model)) {
            AiModelRef.Parsed parsed = AiModelRef.parse(model.trim());
            if (parsed != null && parsed.providerId() != null) {
                for (AiProvider provider : enabled) {
                    if (parsed.providerId().equals(provider.getProviderId())
                            && ownsChatModel(provider, parsed.modelId())) {
                        return provider;
                    }
                }
            }
            String bare = AiModelRef.modelId(model);
            for (AiProvider provider : enabled) {
                if (ownsChatModel(provider, bare)) {
                    return provider;
                }
            }
        }
        return findDefaultChatProvider();
    }

    @Override
    public String resolveChatModel(AiProvider provider, String requested) {
        if (provider == null) {
            return AiModelRef.modelId(requested);
        }
        String bare = AiModelRef.modelId(requested);
        if (StringUtils.hasText(bare) && ownsChatModel(provider, bare)) {
            return bare;
        }
        if (StringUtils.hasText(provider.getDefaultChatModel())) {
            return provider.getDefaultChatModel().trim();
        }
        return parseModels(provider.getModelsJson()).stream()
                .filter(item -> item != null && StringUtils.hasText(item.getId()))
                .filter(item -> !StringUtils.hasText(item.getKind()) || "chat".equalsIgnoreCase(item.getKind()))
                .map(item -> item.getId().trim())
                .findFirst()
                .orElse(StringUtils.hasText(bare) ? bare : "gpt-4o-mini");
    }

    @Override
    public AiProvider findFailoverChatProvider(Long excludeProviderId) {
        return chatProvidersByPriority().stream()
                .filter(item -> excludeProviderId == null || !excludeProviderId.equals(item.getProviderId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public AiProvider findDefaultChatProvider() {
        return firstDefault(listEnabled(), AiProvider::getIsDefaultChat, AiProvider::getChatEnabled);
    }

    @Override
    public AiProvider findDefaultEmbeddingProvider() {
        return firstDefault(
                listEnabled().stream().filter(AiProviderServiceImpl::supportsEmbedding).toList(),
                AiProvider::getIsDefaultEmbedding,
                AiProvider::getEmbeddingEnabled);
    }

    @Override
    public AiProvider findDefaultRerankProvider() {
        return firstDefault(listEnabled(), AiProvider::getIsDefaultRerank, AiProvider::getRerankEnabled);
    }

    private AiProvider firstDefault(
            List<AiProvider> providers,
            java.util.function.Function<AiProvider, String> defaultFlag,
            java.util.function.Function<AiProvider, String> capabilityFlag) {
        return providers.stream()
                .filter(item -> YES.equals(capabilityFlag.apply(item)) && hasApiKey(item))
                .filter(item -> YES.equals(defaultFlag.apply(item)))
                .findFirst()
                .orElseGet(() -> providers.stream()
                        .filter(item -> YES.equals(capabilityFlag.apply(item)) && hasApiKey(item))
                        .findFirst()
                        .orElse(null));
    }

    private List<AiProvider> chatProvidersByPriority() {
        return listEnabled().stream()
                .filter(item -> YES.equals(item.getChatEnabled()) && hasApiKey(item))
                .sorted((left, right) -> {
                    int defaultRank = Boolean.compare(
                            YES.equals(right.getIsDefaultChat()), YES.equals(left.getIsDefaultChat()));
                    if (defaultRank != 0) {
                        return defaultRank;
                    }
                    long leftId = left.getProviderId() != null ? left.getProviderId() : 0L;
                    long rightId = right.getProviderId() != null ? right.getProviderId() : 0L;
                    return Long.compare(leftId, rightId);
                })
                .toList();
    }

    private static AiModelVo toChatModelVo(AiProvider provider, String id, String label) {
        String modelId = id.trim();
        String ref = AiModelRef.encode(provider.getProviderId(), modelId);
        return AiModelVo.builder()
                .id(ref)
                .label(StringUtils.hasText(label) ? label.trim() : modelId)
                .providerName(provider.getProviderName())
                .providerCode(provider.getProviderCode())
                .build();
    }

    private boolean ownsChatModel(AiProvider provider, String model) {
        if (!StringUtils.hasText(model)) {
            return false;
        }
        String bare = AiModelRef.modelId(model);
        if (bare.equals(provider.getDefaultChatModel())) {
            return true;
        }
        return parseModels(provider.getModelsJson()).stream()
                .filter(item -> item != null && StringUtils.hasText(item.getId()))
                .filter(item -> !StringUtils.hasText(item.getKind()) || "chat".equalsIgnoreCase(item.getKind()))
                .anyMatch(item -> bare.equals(item.getId().trim()));
    }

    private void clearOtherDefaults(Long providerId, String kind) {
        LambdaUpdateWrapper<AiProvider> wrapper = new LambdaUpdateWrapper<>();
        switch (kind) {
            case "embedding" -> wrapper.set(AiProvider::getIsDefaultEmbedding, NO)
                    .eq(AiProvider::getIsDefaultEmbedding, YES);
            case "rerank" -> wrapper.set(AiProvider::getIsDefaultRerank, NO)
                    .eq(AiProvider::getIsDefaultRerank, YES);
            default -> wrapper.set(AiProvider::getIsDefaultChat, NO)
                    .eq(AiProvider::getIsDefaultChat, YES);
        }
        wrapper.ne(AiProvider::getProviderId, providerId);
        aiProviderMapper.update(null, wrapper);
    }

    private void ensureNameUnique(String providerName, Long excludeId) {
        LambdaQueryWrapper<AiProvider> wrapper = new LambdaQueryWrapper<AiProvider>()
                .eq(AiProvider::getProviderName, providerName.trim());
        if (excludeId != null) {
            wrapper.ne(AiProvider::getProviderId, excludeId);
        }
        if (aiProviderMapper.selectCount(wrapper) > 0) {
            throw new BizException("供应商名称已存在");
        }
    }

    private static boolean supportsEmbedding(AiProvider provider) {
        if (provider == null) {
            return false;
        }
        String code = provider.getProviderCode() != null ? provider.getProviderCode().trim().toLowerCase() : "";
        if ("deepseek".equals(code) || "kimi".equals(code)) {
            return false;
        }
        return StringUtils.hasText(provider.getDefaultEmbeddingModel());
    }

    private AiProvider requireProvider(Long providerId) {
        AiProvider entity = aiProviderMapper.selectById(providerId);
        if (entity == null) {
            throw new BizException("AI 供应商不存在");
        }
        return entity;
    }

    private AiProviderVo toVo(AiProvider entity) {
        AiProviderVo vo = new AiProviderVo();
        vo.setProviderId(entity.getProviderId());
        vo.setProviderCode(entity.getProviderCode());
        vo.setProviderName(entity.getProviderName());
        vo.setBaseUrl(entity.getBaseUrl());
        String plainKey = decryptKey(entity.getApiKey());
        vo.setApiKeyMasked(AiProviderSecretUtils.mask(plainKey));
        vo.setApiKeyConfigured(StringUtils.hasText(plainKey));
        vo.setCompletionsPath(entity.getCompletionsPath());
        vo.setEmbeddingsPath(entity.getEmbeddingsPath());
        vo.setRerankEndpoint(entity.getRerankEndpoint());
        vo.setChatEnabled(entity.getChatEnabled());
        vo.setEmbeddingEnabled(entity.getEmbeddingEnabled());
        vo.setRerankEnabled(entity.getRerankEnabled());
        vo.setDefaultChatModel(entity.getDefaultChatModel());
        vo.setDefaultEmbeddingModel(entity.getDefaultEmbeddingModel());
        vo.setDefaultRerankModel(entity.getDefaultRerankModel());
        vo.setModels(parseModels(entity.getModelsJson()));
        vo.setIsDefaultChat(entity.getIsDefaultChat());
        vo.setIsDefaultEmbedding(entity.getIsDefaultEmbedding());
        vo.setIsDefaultRerank(entity.getIsDefaultRerank());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setUpdateBy(entity.getUpdateBy());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private AiProvider withPlainApiKey(AiProvider source) {
        AiProvider copy = new AiProvider();
        copy.setProviderId(source.getProviderId());
        copy.setProviderCode(source.getProviderCode());
        copy.setProviderName(source.getProviderName());
        copy.setBaseUrl(source.getBaseUrl());
        copy.setApiKey(decryptKey(source.getApiKey()));
        copy.setCompletionsPath(source.getCompletionsPath());
        copy.setEmbeddingsPath(source.getEmbeddingsPath());
        copy.setRerankEndpoint(source.getRerankEndpoint());
        copy.setChatEnabled(source.getChatEnabled());
        copy.setEmbeddingEnabled(source.getEmbeddingEnabled());
        copy.setRerankEnabled(source.getRerankEnabled());
        copy.setDefaultChatModel(source.getDefaultChatModel());
        copy.setDefaultEmbeddingModel(source.getDefaultEmbeddingModel());
        copy.setDefaultRerankModel(source.getDefaultRerankModel());
        copy.setModelsJson(source.getModelsJson());
        copy.setIsDefaultChat(source.getIsDefaultChat());
        copy.setIsDefaultEmbedding(source.getIsDefaultEmbedding());
        copy.setIsDefaultRerank(source.getIsDefaultRerank());
        copy.setStatus(source.getStatus());
        copy.setRemark(source.getRemark());
        return copy;
    }

    private String encryptKey(String plain) {
        return AiProviderCrypto.encrypt(plain, aiProperties.getSecretKey());
    }

    private String decryptKey(String stored) {
        try {
            return AiProviderCrypto.decrypt(stored, aiProperties.getSecretKey());
        } catch (RuntimeException ex) {
            log.warn("[AI Provider] decrypt failed: {}", ex.getMessage());
            return "";
        }
    }

    /** 启动时把历史明文 Key 改写成密文（有 secret-key 时） */
    private void migratePlaintextKeysAsync(List<AiProvider> rows) {
        if (!StringUtils.hasText(aiProperties.getSecretKey())) {
            return;
        }
        for (AiProvider row : rows) {
            if (!StringUtils.hasText(row.getApiKey()) || AiProviderCrypto.isEncrypted(row.getApiKey())) {
                continue;
            }
            try {
                String encrypted = encryptKey(row.getApiKey().trim());
                aiProviderMapper.update(
                        null,
                        new LambdaUpdateWrapper<AiProvider>()
                                .set(AiProvider::getApiKey, encrypted)
                                .eq(AiProvider::getProviderId, row.getProviderId()));
                log.info("[AI Provider] migrated plaintext api key providerId={}", row.getProviderId());
            } catch (RuntimeException ex) {
                log.warn("[AI Provider] migrate key failed providerId={}: {}", row.getProviderId(), ex.getMessage());
            }
        }
    }

    private String serializeModels(List<AiProviderModelVo> models) {
        List<AiProviderModelVo> normalized = models == null
                ? new ArrayList<>()
                : models.stream()
                        .filter(item -> item != null && StringUtils.hasText(item.getId()))
                        .map(item -> AiProviderModelVo.builder()
                                .id(item.getId().trim())
                                .label(StringUtils.hasText(item.getLabel()) ? item.getLabel().trim() : item.getId().trim())
                                .kind(StringUtils.hasText(item.getKind()) ? item.getKind().trim() : "chat")
                                .build())
                        .collect(Collectors.toList());
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            throw new BizException("模型列表格式错误");
        }
    }

    private List<AiProviderModelVo> parseModels(String modelsJson) {
        if (!StringUtils.hasText(modelsJson)) {
            return new ArrayList<>();
        }
        try {
            List<AiProviderModelVo> models = objectMapper.readValue(modelsJson, new TypeReference<>() {});
            return models != null ? models : new ArrayList<>();
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private static boolean hasApiKey(AiProvider provider) {
        return provider != null && StringUtils.hasText(provider.getApiKey());
    }

    private static String flag(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static String trimSlash(String url) {
        String value = url.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        if (value.endsWith("/v1")) {
            value = value.substring(0, value.length() - 3);
        }
        return value;
    }

    private static String rootMessage(Throwable ex) {
        Throwable current = ex;
        String message = ex.getMessage();
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
            if (StringUtils.hasText(current.getMessage())) {
                message = current.getMessage();
            }
        }
        return message != null ? message : ex.getClass().getSimpleName();
    }

    private String currentOperator() {
        String username = SecurityContextUtils.getUsername();
        return StringUtils.hasText(username) ? username : "system";
    }
}
