package cn.org.starpivot.ai.service.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatLlmIntentClassifierTest {

    @Test
    void parseIntent_readsPlainName() {
        assertEquals(ChatIntent.KNOWLEDGE, ChatLlmIntentClassifier.parseIntent("KNOWLEDGE"));
        assertEquals(ChatIntent.DEVELOPER, ChatLlmIntentClassifier.parseIntent("intent: developer"));
    }

    @Test
    void parseIntent_readsJsonFence() {
        assertEquals(ChatIntent.ANALYST, ChatLlmIntentClassifier.parseIntent("""
                ```json
                {"intent":"ANALYST"}
                ```
                """));
    }

    @Test
    void parseIntent_fallsBackToGeneral() {
        assertEquals(ChatIntent.GENERAL, ChatLlmIntentClassifier.parseIntent(""));
        assertEquals(ChatIntent.GENERAL, ChatLlmIntentClassifier.parseIntent("something else"));
    }
}
