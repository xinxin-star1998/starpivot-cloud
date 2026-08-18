package cn.org.starpivot.ai.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiModelRefTest {

    @Test
    void encodeAndParse() {
        assertEquals("p12:kimi-k3", AiModelRef.encode(12L, "kimi-k3"));
        assertEquals(12L, AiModelRef.providerId("p12:kimi-k3"));
        assertEquals("kimi-k3", AiModelRef.modelId("p12:kimi-k3"));
        assertEquals("kimi-k3", AiModelRef.matchKey("p12:kimi-k3"));
    }

    @Test
    void bareModelCompatible() {
        assertNull(AiModelRef.providerId("deepseek-chat"));
        assertEquals("deepseek-chat", AiModelRef.modelId("deepseek-chat"));
    }
}
