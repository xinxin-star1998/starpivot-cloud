package cn.org.starpivot.ai.config;

import cn.org.starpivot.ai.domain.vo.AiModelVo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiRuntimeModelCatalogTest {

    @Test
    void preferProviderModels_ignoresYamlWhenProviderCatalogExists() {
        List<AiModelVo> providers = List.of(model("kimi-k3", "Kimi K3"));
        List<AiModelVo> yaml = List.of(model("deepseek-chat", "DeepSeek Chat"));

        List<AiModelVo> merged = AiRuntimeModelCatalog.preferProviderModels(providers, yaml);

        assertEquals(1, merged.size());
        assertEquals("kimi-k3", merged.get(0).getId());
    }

    @Test
    void preferProviderModels_fallsBackToYaml() {
        List<AiModelVo> merged = AiRuntimeModelCatalog.preferProviderModels(
                List.of(), List.of(model("deepseek-chat", "DeepSeek Chat")));
        assertEquals("deepseek-chat", merged.get(0).getId());
    }

    @Test
    void resolveDefaultModel_usesProviderDefault() {
        List<AiModelVo> models = List.of(model("kimi-k3", "Kimi K3"), model("kimi-k2.6", "Kimi K2.6"));
        assertEquals(
                "kimi-k3",
                AiRuntimeModelCatalog.resolveDefaultModel("kimi-k3", "deepseek-chat", models));
    }

    @Test
    void resolveDefaultModel_skipsStaleConfiguredId() {
        List<AiModelVo> models = List.of(model("qwen-plus", "Qwen Plus"));
        assertEquals("qwen-plus", AiRuntimeModelCatalog.resolveDefaultModel(null, "deepseek-chat", models));
        assertTrue(AiRuntimeModelCatalog.preferProviderModels(models, List.of(model("deepseek-chat", "x")))
                .stream()
                .noneMatch(item -> "deepseek-chat".equals(item.getId())));
    }

    private static AiModelVo model(String id, String label) {
        return AiModelVo.builder().id(id).label(label).build();
    }
}
