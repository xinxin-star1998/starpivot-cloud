package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.domain.vo.AiHealthVo;
import cn.org.starpivot.ai.service.AiRuntimeConfigService;
import cn.org.starpivot.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ChatHealthService {

    private final AiRuntimeConfigService aiRuntimeConfigService;
    private final AiProperties aiProperties;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

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
                .queryRouterEnabled(aiProperties.getQueryRouter().isEnabled());

        if (!StringUtils.hasText(apiKey)) {
            return builder
                    .online(false)
                    .message("未配置 AI API Key，请在 Nacos 中设置 spring.ai.openai.api-key")
                    .build();
        }
        return builder.online(true).message("在线").build();
    }

    public void assertConfigured() {
        if (!StringUtils.hasText(apiKey)) {
            throw new BizException("AI 服务未配置，请联系管理员设置 API Key");
        }
    }
}
