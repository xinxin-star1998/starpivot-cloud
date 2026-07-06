package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.config.AiRuntimeSnapshot;
import cn.org.starpivot.ai.domain.vo.RagRetrievalResult;
import cn.org.starpivot.ai.service.AiKnowledgeRetrievalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatPromptAssemblerTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private MessageWindowChatMemory chatMemory;

    @Mock
    private AiKnowledgeRetrievalService aiKnowledgeRetrievalService;

    @Mock
    private SystemPromptResolver systemPromptResolver;

    @InjectMocks
    private ChatPromptAssembler chatPromptAssembler;

    @Test
    void retrieve_skipsWhenUseRagFalse() {
        AiRuntimeSnapshot runtime = AiRuntimeSnapshot.builder()
                .ragEnabled(true)
                .build();

        RagRetrievalResult result = chatPromptAssembler.retrieve(runtime, "question", false);

        assertEquals("", result.getContext());
        verify(aiKnowledgeRetrievalService, never()).retrieve(anyString(), anyInt());
    }

    @Test
    void resolveModel_usesDefaultWhenBlank() {
        AiRuntimeSnapshot runtime = AiRuntimeSnapshot.builder()
                .defaultModel("deepseek-chat")
                .build();

        assertEquals("deepseek-chat", chatPromptAssembler.resolveModel(null, runtime));
        assertEquals("deepseek-chat", chatPromptAssembler.resolveModel("auto", runtime));
    }
}
