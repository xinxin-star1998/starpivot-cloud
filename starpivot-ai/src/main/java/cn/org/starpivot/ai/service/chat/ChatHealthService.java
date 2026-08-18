package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.domain.vo.AiHealthVo;
import cn.org.starpivot.ai.provider.AiModelClientFactory;
import cn.org.starpivot.ai.service.AiRuntimeConfigService;
import cn.org.starpivot.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ChatHealthService {

    private final AiRuntimeConfigService aiRuntimeConfigService;
    private final AiProperties aiProperties;
    private final AiModelClientFactory aiModelClientFactory;

    public AiHealthVo health() {
        AiRuntimeSnapshot runtime = aiRuntimeConfigService.current();
        String botAvatar = StringUtils.hasText(runtime.getBotAvatar())
                ? runtime.getBotAvatar().trim()
                : null;
        String botName = StringUtils.hasText(runtime.getBotName())
                ? runtime.getBotName().trim()
                : "AI 助手";
        AiHealthVo.AiHealthVoBuilder builder = AiHealthVo.builder()
                .botAvatar(botAvatar)
                .botName(botName)
                .welcomeMessage(runtime.resolvedWelcomeMessage())
                .models(runtime.getModels())
                .defaultModel(runtime.getDefaultModel())
                .defaultTemperature(runtime.getDefaultTemperature())
                .maxMemoryMessages(runtime.getMaxMemoryMessages())
                .promptTemplates(runtime.resolvedPromptTemplateOptions())
                .defaultPromptScene(runtime.getDefaultPromptScene())
                .queryRouterEnabled(aiProperties.getQueryRouter().isEnabled())
                .agentEnabled(aiProperties.getAgent().isEnabled());

        if (!isChatReady()) {
            return builder
                    .online(false)
                    .message("未配置对话 API，请在「AI 中心 → 模型供应商」中填写 DeepSeek / Kimi / 百炼等 Key")
                    .build();
        }
        return builder.online(true).message("在线").build();
    }

    public void assertConfigured() {
        if (!isChatReady()) {
            throw new BizException("AI 服务未配置，请在「AI 中心 → 模型供应商」中设置 API Key");
        }
    }

    private boolean isChatReady() {
        return aiModelClientFactory.hasChatCredential();
    }
}
