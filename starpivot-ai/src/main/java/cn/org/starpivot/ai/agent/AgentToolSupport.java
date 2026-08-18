package cn.org.starpivot.ai.agent;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.provider.AiModelRef;
import cn.org.starpivot.ai.service.chat.ChatIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AgentToolSupport {

    private final AiProperties aiProperties;

    public boolean shouldEnable(ChatIntent intent, String model) {
        if (!aiProperties.getAgent().isEnabled()) {
            return false;
        }
        if (intent == ChatIntent.CHITCHAT) {
            return false;
        }
        return supportsFunctionCalling(model);
    }

    public boolean supportsFunctionCalling(String model) {
        if (!StringUtils.hasText(model)) {
            return true;
        }
        String normalized = AiModelRef.matchKey(model).toLowerCase();
        return !normalized.contains("reasoner")
                && !normalized.contains("-r1")
                && !normalized.contains("o1-");
    }
}
