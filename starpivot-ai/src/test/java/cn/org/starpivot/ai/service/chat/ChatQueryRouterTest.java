package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.config.AiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatQueryRouterTest {

    private ChatQueryRouter router;

    @BeforeEach
    void setUp() {
        AiProperties properties = new AiProperties();
        properties.getQueryRouter().setEnabled(true);
        properties.getQueryRouter().setChatModel("deepseek-chat");
        properties.getQueryRouter().setReasonerModel("deepseek-reasoner");
        router = new ChatQueryRouter(properties);
    }

    @Test
    void classify_chitchat() {
        assertEquals(ChatIntent.CHITCHAT, router.classify("你好"));
        assertEquals(ChatIntent.CHITCHAT, router.classify("谢谢！"));
    }

    @Test
    void classify_knowledge() {
        assertEquals(ChatIntent.KNOWLEDGE, router.classify("如何配置 RAG 知识库？"));
    }

    @Test
    void classify_developer() {
        assertEquals(ChatIntent.DEVELOPER, router.classify("这段 Java 代码报 NullPointerException"));
    }

    @Test
    void classify_analyst() {
        assertEquals(ChatIntent.ANALYST, router.classify("帮我分析销售指标同比趋势"));
    }

    @Test
    void classify_reasoning() {
        assertEquals(ChatIntent.REASONING, router.classify("请对比微服务与单体架构的优劣"));
    }

    @Test
    void suggest_knowledgeUsesSupportAndRag() {
        ChatQueryRouter.RoutedSuggestion suggestion = router.suggest(ChatIntent.KNOWLEDGE);
        assertEquals("support", suggestion.promptScene());
        assertTrue(suggestion.useRag());
    }

    @Test
    void suggest_reasoningUsesReasonerWithoutRag() {
        ChatQueryRouter.RoutedSuggestion suggestion = router.suggest(ChatIntent.REASONING);
        assertEquals("developer", suggestion.promptScene());
        assertEquals("deepseek-reasoner", suggestion.model());
        assertFalse(suggestion.useRag());
    }

    @Test
    void isAuto_acceptsBlankAndAutoKeyword() {
        assertTrue(router.isAuto(null));
        assertTrue(router.isAuto(""));
        assertTrue(router.isAuto("auto"));
        assertFalse(router.isAuto("deepseek-chat"));
    }
}
