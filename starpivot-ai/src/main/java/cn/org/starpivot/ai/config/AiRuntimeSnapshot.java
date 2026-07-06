package cn.org.starpivot.ai.config;

import cn.org.starpivot.ai.domain.vo.AiModelVo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AiRuntimeSnapshot {

    private final String botName;

    private final String botAvatar;

    private final String welcomeMessage;

    private final String systemPrompt;

    private final String defaultModel;

    private final Double defaultTemperature;

    private final int maxMemoryMessages;

    private final List<AiModelVo> models;

    private final boolean ragEnabled;

    private final int ragTopK;

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
        if (models == null || models.size() <= 1) {
            return true;
        }
        String normalized = model.trim();
        return models.stream().anyMatch(item -> normalized.equals(item.getId()));
    }
}
