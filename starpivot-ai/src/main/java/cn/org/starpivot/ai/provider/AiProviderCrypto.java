package cn.org.starpivot.ai.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 供应商 API Key 落库加密（AES-GCM）。格式：{@code ENC:v1:}{@code + Base64(iv + ciphertext)。
 * 未带前缀的值视为历史明文，解密时原样返回以便平滑迁移。
 */
@Slf4j
public final class AiProviderCrypto {

    public static final String PREFIX = "ENC:v1:";

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private AiProviderCrypto() {}

    public static boolean isEncrypted(String value) {
        return StringUtils.hasText(value) && value.startsWith(PREFIX);
    }

    public static String encrypt(String plain, String secret) {
        if (!StringUtils.hasText(plain)) {
            return plain;
        }
        if (!StringUtils.hasText(secret)) {
            log.warn("[AI Provider] secret-key 未配置，API Key 将以明文保存");
            return plain.trim();
        }
        if (isEncrypted(plain)) {
            return plain.trim();
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keyFrom(secret), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherBytes = cipher.doFinal(plain.trim().getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherBytes.length);
            buffer.put(iv);
            buffer.put(cipherBytes);
            return PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception ex) {
            throw new IllegalStateException("API Key 加密失败", ex);
        }
    }

    public static String decrypt(String stored, String secret) {
        if (!StringUtils.hasText(stored)) {
            return stored;
        }
        String value = stored.trim();
        if (!isEncrypted(value)) {
            return value;
        }
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("已加密的 API Key 需要配置 starpivot.ai.secret-key 才能解密");
        }
        try {
            byte[] raw = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            if (raw.length <= IV_BYTES) {
                throw new IllegalStateException("密文格式无效");
            }
            byte[] iv = new byte[IV_BYTES];
            byte[] cipherBytes = new byte[raw.length - IV_BYTES];
            System.arraycopy(raw, 0, iv, 0, IV_BYTES);
            System.arraycopy(raw, IV_BYTES, cipherBytes, 0, cipherBytes.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keyFrom(secret), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("API Key 解密失败", ex);
        }
    }

    private static SecretKey keyFrom(String secret) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(hash, "AES");
    }
}
