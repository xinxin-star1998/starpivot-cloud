package cn.org.starpivot.auth.service;

import cn.org.starpivot.auth.domain.CaptchaResponse;
import cn.org.starpivot.auth.domain.CaptchaVerifyRequest;
import cn.org.starpivot.auth.domain.CaptchaVerifyResponse;
import cn.org.starpivot.common.cache.CacheConstants;
import cn.org.starpivot.common.exception.BizException;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码业务服务类。
 * <p>
 * 基于 Kaptcha 生成图形验证码，将验证码文本存入 Redis，校验通过后颁发短期有效的验证码凭证（captchaProof）。
 * </p>
 * <ul>
 *   <li>{@link Service} — 注册为 Spring 业务服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — Lombok 生成含 {@code final} 字段的构造器，注入 Kaptcha 与 Redis 模板</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final long CAPTCHA_TTL_MINUTES = CacheConstants.TTL_CAPTCHA.toMinutes();
    private static final long PROOF_TTL_MINUTES = CacheConstants.TTL_CAPTCHA_PROOF.toMinutes();

    private final DefaultKaptcha captchaProducer;
    private final StringRedisTemplate redisTemplate;

    /**
     * 生成图形验证码并返回 Base64 图片及令牌。
     *
     * @param scene 业务场景标识，用于 Redis 键隔离
     * @return 含 captchaToken 与 captchaImage 的响应
     * @throws BizException 图片编码失败时抛出
     */
    public CaptchaResponse generate(String scene) {
        String token = UUID.randomUUID().toString();
        String code = captchaProducer.createText();
        BufferedImage image = captchaProducer.createImage(code);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", outputStream);
            String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());

            String redisKey = buildCaptchaKey(scene, token);
            redisTemplate.opsForValue().set(redisKey, code.toLowerCase(), CAPTCHA_TTL_MINUTES, TimeUnit.MINUTES);

            return CaptchaResponse.builder()
                    .captchaToken(token)
                    .captchaImage("data:image/jpeg;base64," + base64)
                    .build();
        } catch (Exception e) {
            throw new BizException("验证码生成失败");
        }
    }

    /**
     * 校验用户输入的验证码，成功后颁发 captchaProof 凭证。
     *
     * @param request 校验请求，含 captchaToken、code 及可选 scene
     * @return 含 captchaProof 的响应
     * @throws BizException 参数缺失、验证码错误或已过期时抛出 401
     */
    public CaptchaVerifyResponse verify(CaptchaVerifyRequest request) {
        if (!StringUtils.hasText(request.getCaptchaToken()) || !StringUtils.hasText(request.getCode())) {
            throw new BizException(401, "验证码不能为空");
        }

        String scene = StringUtils.hasText(request.getScene()) ? request.getScene() : "login";
        String redisKey = buildCaptchaKey(scene, request.getCaptchaToken());
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        redisTemplate.delete(redisKey);

        if (storedCode == null || !storedCode.equalsIgnoreCase(request.getCode().trim())) {
            throw new BizException(401, "验证码错误或已过期");
        }

        String proof = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(CacheConstants.captchaProofKey(proof), "1", PROOF_TTL_MINUTES, TimeUnit.MINUTES);
        return CaptchaVerifyResponse.builder().captchaProof(proof).build();
    }

    /**
     * 消费验证码凭证（一次性）。
     */
    public void consumeProof(String proof) {
        if (!StringUtils.hasText(proof)) {
            throw new BizException(401, "验证码凭证无效或已过期");
        }
        String key = CacheConstants.captchaProofKey(proof);
        Boolean deleted = redisTemplate.delete(key);
        if (!Boolean.TRUE.equals(deleted)) {
            throw new BizException(401, "验证码凭证无效或已过期");
        }
    }

    private String buildCaptchaKey(String scene, String token) {
        return CacheConstants.captchaKey(scene, token);
    }
}
