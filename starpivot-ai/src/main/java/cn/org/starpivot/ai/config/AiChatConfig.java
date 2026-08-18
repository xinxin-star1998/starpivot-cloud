package cn.org.starpivot.ai.config;

import cn.org.starpivot.ai.memory.MysqlChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatConfig {

    private static final int MEMORY_HARD_CAP = 200;

    @Bean
    public MessageWindowChatMemory chatMemory(MysqlChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(MEMORY_HARD_CAP)
                .build();
    }

    /**
     * 可选 YAML 回退客户端：仅当 Spring AI 自动配置出 ChatModel 时创建。
     * 日常对话走 {@code AiModelClientFactory}，密钥来自「模型供应商」表。
     */
    @Bean
    @ConditionalOnBean(ChatModel.class)
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
