package cn.org.starpivot.ai.config;

import cn.org.starpivot.ai.domain.vo.AiModelVo;
import cn.org.starpivot.ai.domain.vo.AiPromptTemplateVo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class AiRuntimeSnapshot {

    private final String botName;

    private final String botAvatar;

    private final String welcomeMessage;

    /** 兜底系统提示词（无场景模板或未匹配时使用） */
    private final String systemPrompt;

    private final String defaultModel;

    private final Double defaultTemperature;

    private final int maxMemoryMessages;

    private final List<AiModelVo> models;

    private final boolean ragEnabled;

    private final int ragTopK;

    /** 默认场景 ID */
    private final String defaultPromptScene;

    /** 场景化 Prompt 模板（含完整 prompt 文本） */
    private final List<AiPromptTemplateSnapshot> promptTemplates;

    public boolean isRagEnabled() {
        return ragEnabled;
    }

    public String resolvedWelcomeMessage() {
        String template = welcomeMessage != null && !welcomeMessage.isBlank()
                ? welcomeMessage.trim()
                : "你好！我是 **{botName}**，有什么我可以帮你的吗？";
        String name = botName != null && !botName.isBlank() ? botName.trim() : "AI 助手";
        return template.replace("{botName}", name);
    }

    public boolean isModelAllowed(String model) {
        if (model == null || model.isBlank()) {
            return true;
        }
        if (models == null || models.isEmpty()) {
            return true;
        }
        String normalized = model.trim();
        return models.stream().anyMatch(item -> normalized.equals(item.getId()));
    }

    public List<AiPromptTemplateVo> resolvedPromptTemplateOptions() {
        if (promptTemplates == null || promptTemplates.isEmpty()) {
            return List.of();
        }
        return promptTemplates.stream()
                .map(item -> AiPromptTemplateVo.builder()
                        .id(item.getId())
                        .label(item.getLabel())
                        .description(item.getDescription())
                        .build())
                .collect(Collectors.toList());
    }
}
