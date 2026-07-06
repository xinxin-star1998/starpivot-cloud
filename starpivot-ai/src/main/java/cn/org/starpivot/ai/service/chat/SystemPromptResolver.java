package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.config.AiPromptTemplateSnapshot;
import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.common.exception.BizException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class SystemPromptResolver {

    public String resolve(AiRuntimeSnapshot runtime, String promptScene) {
        AiPromptTemplateSnapshot template = findTemplate(runtime, promptScene);
        if (template == null || !StringUtils.hasText(template.getPrompt())) {
            return applyPlaceholders(fallbackPrompt(runtime), runtime);
        }
        return applyPlaceholders(template.getPrompt(), runtime);
    }

    public AiPromptTemplateSnapshot findTemplate(AiRuntimeSnapshot runtime, String promptScene) {
        List<AiPromptTemplateSnapshot> templates = runtime.getPromptTemplates();
        if (templates == null || templates.isEmpty()) {
            return null;
        }
        String sceneId = normalizeSceneId(runtime, promptScene);
        return templates.stream()
                .filter(item -> sceneId.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }

    public String normalizeSceneId(AiRuntimeSnapshot runtime, String promptScene) {
        if (StringUtils.hasText(promptScene)) {
            return promptScene.trim();
        }
        return StringUtils.hasText(runtime.getDefaultPromptScene())
                ? runtime.getDefaultPromptScene().trim()
                : "default";
    }

    public void assertSceneAllowed(AiRuntimeSnapshot runtime, String promptScene) {
        if (!StringUtils.hasText(promptScene)) {
            return;
        }
        List<AiPromptTemplateSnapshot> templates = runtime.getPromptTemplates();
        if (templates == null || templates.isEmpty()) {
            return;
        }
        String normalized = promptScene.trim();
        boolean allowed = templates.stream().anyMatch(item -> normalized.equals(item.getId()));
        if (!allowed) {
            throw new BizException("不支持的对话场景：" + normalized);
        }
    }

    private String fallbackPrompt(AiRuntimeSnapshot runtime) {
        if (StringUtils.hasText(runtime.getSystemPrompt())) {
            return runtime.getSystemPrompt();
        }
        return "你是一个智能 AI 助手。";
    }

    private String applyPlaceholders(String prompt, AiRuntimeSnapshot runtime) {
        String botName = StringUtils.hasText(runtime.getBotName()) ? runtime.getBotName().trim() : "AI 助手";
        return prompt.replace("{botName}", botName);
    }
}
