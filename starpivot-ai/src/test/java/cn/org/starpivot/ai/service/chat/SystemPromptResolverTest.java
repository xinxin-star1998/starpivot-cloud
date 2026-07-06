package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.config.AiPromptTemplateSnapshot;
import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SystemPromptResolverTest {

    private SystemPromptResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new SystemPromptResolver();
    }

    @Test
    void resolve_usesScenePromptWithPlaceholder() {
        AiRuntimeSnapshot runtime = runtimeWithTemplates(
                template("support", "客服", null, "你是 {botName}。"));

        String prompt = resolver.resolve(runtime, "support");

        assertEquals("你是 StarPivot 客服。", prompt);
    }

    @Test
    void resolve_fallsBackToDefaultSceneWhenBlank() {
        AiRuntimeSnapshot runtime = runtimeWithTemplates(
                template("default", "通用", null, "默认 prompt"));

        assertEquals("默认 prompt", resolver.resolve(runtime, null));
    }

    @Test
    void assertSceneAllowed_rejectsUnknownScene() {
        AiRuntimeSnapshot runtime = runtimeWithTemplates(
                template("default", "通用", null, "默认 prompt"),
                template("support", "客服", null, "客服 prompt"));

        assertThrows(BizException.class, () -> resolver.assertSceneAllowed(runtime, "unknown"));
    }

    @Test
    void assertSceneAllowed_rejectsUnknownSceneWithSingleTemplate() {
        AiRuntimeSnapshot runtime = runtimeWithTemplates(
                template("default", "通用", null, "默认 prompt"));

        assertThrows(BizException.class, () -> resolver.assertSceneAllowed(runtime, "unknown"));
    }

    @Test
    void findTemplate_returnsSceneModelAndTemperature() {
        AiRuntimeSnapshot runtime = runtimeWithTemplates(
                template("developer", "开发", null, "dev prompt", 0.2, "deepseek-reasoner"));

        AiPromptTemplateSnapshot scene = resolver.findTemplate(runtime, "developer");

        assertEquals("deepseek-reasoner", scene.getModel());
        assertEquals(0.2, scene.getTemperature());
    }

    private AiRuntimeSnapshot runtimeWithTemplates(AiPromptTemplateSnapshot... templates) {
        return AiRuntimeSnapshot.builder()
                .botName("StarPivot 客服")
                .systemPrompt("fallback")
                .defaultPromptScene("default")
                .promptTemplates(List.of(templates))
                .build();
    }

    private AiPromptTemplateSnapshot template(
            String id, String label, String description, String prompt) {
        return template(id, label, description, prompt, null, null);
    }

    private AiPromptTemplateSnapshot template(
            String id, String label, String description, String prompt, Double temperature, String model) {
        return AiPromptTemplateSnapshot.builder()
                .id(id)
                .label(label)
                .description(description)
                .prompt(prompt)
                .temperature(temperature)
                .model(model)
                .build();
    }
}
