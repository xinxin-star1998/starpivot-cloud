package cn.org.starpivot.ai.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiProviderSecretUtilsTest {

    @Test
    void mask_hidesMiddleOfKey() {
        assertEquals("", AiProviderSecretUtils.mask(null));
        assertEquals("****", AiProviderSecretUtils.mask("short"));
        assertEquals("sk-****551c", AiProviderSecretUtils.mask("sk-29a3e412f1224cccb53377ac994f551c"));
    }

    @Test
    void isUnchanged_whenBlankOrMasked() {
        assertTrue(AiProviderSecretUtils.isUnchanged(null));
        assertTrue(AiProviderSecretUtils.isUnchanged(""));
        assertTrue(AiProviderSecretUtils.isUnchanged("sk-****551c"));
        assertFalse(AiProviderSecretUtils.isUnchanged("sk-new-key"));
    }

    @Test
    void normalizeBaseUrl_stripsSlashAndV1() {
        assertEquals("https://api.deepseek.com", AiModelClientFactory.normalizeBaseUrl("https://api.deepseek.com/v1/"));
        assertEquals(
                "https://dashscope.aliyuncs.com/compatible-mode",
                AiModelClientFactory.normalizeBaseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1"));
    }
}
