package cn.org.starpivot.ai.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiProviderCryptoTest {

    @Test
    void encryptDecrypt_roundTrip() {
        String secret = "unit-test-secret-key";
        String plain = "sk-29a3e412f1224cccb53377ac994f551c";
        String encrypted = AiProviderCrypto.encrypt(plain, secret);
        assertTrue(AiProviderCrypto.isEncrypted(encrypted));
        assertEquals(plain, AiProviderCrypto.decrypt(encrypted, secret));
    }

    @Test
    void decrypt_keepsLegacyPlaintext() {
        assertEquals("sk-plain", AiProviderCrypto.decrypt("sk-plain", "any-secret"));
    }

    @Test
    void encrypt_withoutSecret_keepsPlaintext() {
        assertEquals("sk-plain", AiProviderCrypto.encrypt("sk-plain", ""));
    }
}
