package cn.org.starpivot.ai.agent;

import cn.org.starpivot.ai.config.AiProperties;
import cn.org.starpivot.ai.service.chat.ChatIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentToolSupportTest {

    private AgentToolSupport support;
    private AiProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AiProperties();
        properties.getAgent().setEnabled(true);
        support = new AgentToolSupport(properties);
    }

    @Test
    void shouldEnable_forKnowledgeOnChatModel() {
        assertTrue(support.shouldEnable(ChatIntent.KNOWLEDGE, "deepseek-chat"));
        assertTrue(support.shouldEnable(ChatIntent.GENERAL, "deepseek-chat"));
    }

    @Test
    void shouldEnable_skipsChitchatAndReasoner() {
        assertFalse(support.shouldEnable(ChatIntent.CHITCHAT, "deepseek-chat"));
        assertFalse(support.shouldEnable(ChatIntent.REASONING, "deepseek-reasoner"));
        assertFalse(support.shouldEnable(ChatIntent.DEVELOPER, "deepseek-reasoner"));
    }

    @Test
    void shouldEnable_respectsGlobalSwitch() {
        properties.getAgent().setEnabled(false);
        assertFalse(support.shouldEnable(ChatIntent.KNOWLEDGE, "deepseek-chat"));
    }
}
