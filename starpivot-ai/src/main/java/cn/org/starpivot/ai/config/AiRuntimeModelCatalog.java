package cn.org.starpivot.ai.config;

import cn.org.starpivot.ai.domain.vo.AiModelVo;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 有可用供应商时，模型目录与默认模型以供应商为准，不再混入 YAML / 基础配置里的过期 ID。
 */
public final class AiRuntimeModelCatalog {

    private AiRuntimeModelCatalog() {}

    public static List<AiModelVo> preferProviderModels(List<AiModelVo> providerModels, List<AiModelVo> fallback) {
        if (providerModels != null && !providerModels.isEmpty()) {
            return List.copyOf(providerModels);
        }
        if (fallback == null || fallback.isEmpty()) {
            return List.of();
        }
        return List.copyOf(fallback);
    }

    public static String resolveDefaultModel(
            String providerDefaultModel, String configured, List<AiModelVo> models) {
        Set<String> ids = idsOf(models);
        if (StringUtils.hasText(providerDefaultModel)) {
            String preferred = providerDefaultModel.trim();
            if (ids.isEmpty() || ids.contains(preferred)) {
                return preferred;
            }
        }
        if (!ids.isEmpty()) {
            return models.get(0).getId();
        }
        return StringUtils.hasText(configured) ? configured.trim() : null;
    }

    private static Set<String> idsOf(List<AiModelVo> models) {
        if (models == null || models.isEmpty()) {
            return Set.of();
        }
        Set<String> ids = new LinkedHashSet<>();
        for (AiModelVo model : models) {
            if (model != null && StringUtils.hasText(model.getId())) {
                ids.add(model.getId().trim());
            }
        }
        return ids;
    }

    public static List<AiModelVo> copy(List<AiModelVo> source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source);
    }
}
