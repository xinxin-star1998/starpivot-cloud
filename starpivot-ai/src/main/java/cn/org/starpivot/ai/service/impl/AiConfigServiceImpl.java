package cn.org.starpivot.ai.service.impl;

import cn.org.starpivot.ai.domain.dto.AiConfigQueryDto;
import cn.org.starpivot.ai.domain.dto.AiConfigSaveDto;
import cn.org.starpivot.ai.domain.entity.AiConfig;
import cn.org.starpivot.ai.domain.vo.AiConfigVo;
import cn.org.starpivot.ai.domain.vo.AiModelVo;
import cn.org.starpivot.ai.mapper.AiConfigMapper;
import cn.org.starpivot.ai.service.AiConfigService;
import cn.org.starpivot.ai.service.AiRuntimeConfigService;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.security.SecurityContextUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiConfigServiceImpl implements AiConfigService {

    private static final String STATUS_NORMAL = "0";
    private static final String DEFAULT_FLAG = "0";
    private static final String NOT_DEFAULT_FLAG = "1";

    private final AiConfigMapper aiConfigMapper;
    private final AiRuntimeConfigService aiRuntimeConfigService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiConfigVo> pageList(AiConfigQueryDto query) {
        Page<AiConfig> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getConfigName()), AiConfig::getConfigName, query.getConfigName())
                .like(StringUtils.hasText(query.getBotName()), AiConfig::getBotName, query.getBotName())
                .eq(StringUtils.hasText(query.getStatus()), AiConfig::getStatus, query.getStatus())
                .orderByAsc(AiConfig::getIsDefault)
                .orderByDesc(AiConfig::getUpdateTime);
        Page<AiConfig> result = aiConfigMapper.selectPage(page, wrapper);
        PageResponse<AiConfigVo> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setRows(result.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AiConfigVo getById(Long configId) {
        AiConfig entity = requireConfig(configId);
        return toVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(AiConfigSaveDto dto) {
        LocalDateTime now = LocalDateTime.now();
        String operator = currentOperator();
        AiConfig entity;
        if (dto.getConfigId() != null) {
            entity = requireConfig(dto.getConfigId());
        } else {
            entity = new AiConfig();
            entity.setCreateBy(operator);
            entity.setCreateTime(now);
        }
        ensureConfigNameUnique(dto.getConfigName(), dto.getConfigId());
        entity.setConfigName(dto.getConfigName().trim());
        entity.setBotName(dto.getBotName().trim());
        entity.setBotAvatar(trimToNull(dto.getBotAvatar()));
        entity.setWelcomeMessage(trimToNull(dto.getWelcomeMessage()));
        entity.setSystemPrompt(dto.getSystemPrompt().trim());
        entity.setDefaultModel(dto.getDefaultModel().trim());
        entity.setDefaultTemperature(
                dto.getDefaultTemperature() != null ? dto.getDefaultTemperature() : new BigDecimal("0.70"));
        entity.setMaxMemoryMessages(dto.getMaxMemoryMessages() != null ? dto.getMaxMemoryMessages() : 30);
        entity.setModelsJson(serializeModels(dto.getModels()));
        entity.setRagEnabled(StringUtils.hasText(dto.getRagEnabled()) ? dto.getRagEnabled() : NOT_DEFAULT_FLAG);
        entity.setRagTopK(dto.getRagTopK() != null ? dto.getRagTopK() : 5);
        entity.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : STATUS_NORMAL);
        entity.setRemark(trimToNull(dto.getRemark()));
        entity.setUpdateBy(operator);
        entity.setUpdateTime(now);

        boolean setDefault = DEFAULT_FLAG.equals(dto.getIsDefault());
        if (dto.getConfigId() == null) {
            entity.setIsDefault(setDefault ? DEFAULT_FLAG : NOT_DEFAULT_FLAG);
            aiConfigMapper.insert(entity);
        } else {
            entity.setIsDefault(setDefault ? DEFAULT_FLAG : entity.getIsDefault());
            aiConfigMapper.updateById(entity);
        }

        if (setDefault) {
            clearOtherDefaults(entity.getConfigId());
        } else if (!hasDefaultConfig()) {
            entity.setIsDefault(DEFAULT_FLAG);
            aiConfigMapper.updateById(entity);
        }

        aiRuntimeConfigService.refresh();
        return entity.getConfigId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long configId) {
        AiConfig entity = requireConfig(configId);
        if (DEFAULT_FLAG.equals(entity.getIsDefault())) {
            throw new BizException("默认配置不能删除，请先指定其他配置为默认");
        }
        aiConfigMapper.deleteById(configId);
        aiRuntimeConfigService.refresh();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long configId) {
        AiConfig entity = requireConfig(configId);
        if (STATUS_NORMAL.equals(entity.getStatus())) {
            entity.setIsDefault(DEFAULT_FLAG);
            entity.setUpdateBy(currentOperator());
            entity.setUpdateTime(LocalDateTime.now());
            aiConfigMapper.updateById(entity);
            clearOtherDefaults(configId);
            aiRuntimeConfigService.refresh();
            return;
        }
        throw new BizException("停用的配置不能设为默认");
    }

    private void clearOtherDefaults(Long configId) {
        aiConfigMapper.update(
                null,
                new LambdaUpdateWrapper<AiConfig>()
                        .set(AiConfig::getIsDefault, NOT_DEFAULT_FLAG)
                        .eq(AiConfig::getIsDefault, DEFAULT_FLAG)
                        .ne(AiConfig::getConfigId, configId));
    }

    private boolean hasDefaultConfig() {
        return aiConfigMapper.selectCount(new LambdaQueryWrapper<AiConfig>()
                        .eq(AiConfig::getIsDefault, DEFAULT_FLAG))
                > 0;
    }

    private void ensureConfigNameUnique(String configName, Long excludeId) {
        LambdaQueryWrapper<AiConfig> wrapper = new LambdaQueryWrapper<AiConfig>()
                .eq(AiConfig::getConfigName, configName.trim());
        if (excludeId != null) {
            wrapper.ne(AiConfig::getConfigId, excludeId);
        }
        if (aiConfigMapper.selectCount(wrapper) > 0) {
            throw new BizException("配置名称已存在");
        }
    }

    private AiConfig requireConfig(Long configId) {
        AiConfig entity = aiConfigMapper.selectById(configId);
        if (entity == null) {
            throw new BizException("AI 配置不存在");
        }
        return entity;
    }

    private String serializeModels(List<AiModelVo> models) {
        List<AiModelVo> normalized = models == null
                ? new ArrayList<>()
                : models.stream()
                        .filter(item -> item != null && StringUtils.hasText(item.getId()))
                        .map(item -> AiModelVo.builder()
                                .id(item.getId().trim())
                                .label(StringUtils.hasText(item.getLabel())
                                        ? item.getLabel().trim()
                                        : item.getId().trim())
                                .build())
                        .collect(Collectors.toList());
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            throw new BizException("模型列表格式错误");
        }
    }

    private AiConfigVo toVo(AiConfig entity) {
        AiConfigVo vo = new AiConfigVo();
        vo.setConfigId(entity.getConfigId());
        vo.setConfigName(entity.getConfigName());
        vo.setBotName(entity.getBotName());
        vo.setBotAvatar(entity.getBotAvatar());
        vo.setWelcomeMessage(entity.getWelcomeMessage());
        vo.setSystemPrompt(entity.getSystemPrompt());
        vo.setDefaultModel(entity.getDefaultModel());
        vo.setDefaultTemperature(entity.getDefaultTemperature());
        vo.setMaxMemoryMessages(entity.getMaxMemoryMessages());
        vo.setModels(parseModels(entity.getModelsJson()));
        vo.setRagEnabled(entity.getRagEnabled());
        vo.setRagTopK(entity.getRagTopK());
        vo.setIsDefault(entity.getIsDefault());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setUpdateBy(entity.getUpdateBy());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private List<AiModelVo> parseModels(String modelsJson) {
        if (!StringUtils.hasText(modelsJson)) {
            return new ArrayList<>();
        }
        try {
            List<AiModelVo> models = objectMapper.readValue(modelsJson, new TypeReference<>() {});
            return models != null ? models : new ArrayList<>();
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String currentOperator() {
        String username = SecurityContextUtils.getUsername();
        return StringUtils.hasText(username) ? username : "system";
    }
}
