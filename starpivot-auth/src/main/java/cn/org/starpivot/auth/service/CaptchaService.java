package cn.org.starpivot.auth.service;

import cn.org.starpivot.auth.captcha.CaptchaImageRenderer;
import cn.org.starpivot.auth.domain.CaptchaResponse;
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
import java.util.Set;
import java.util.UUID;

/**
 * 验证码业务服务：Kaptcha 生成字符，自绘彩色图形，Redis 存储；登录/忘记密码在业务接口内联校验。
 */
@Service
@RequiredArgsConstructor
public class CaptchaService {

    public static final String SCENE_LOGIN = "login";
    public static final String SCENE_FORGET_PASSWORD = "forget-password";

    private static final Set<String> ALLOWED_SCENES = Set.of(SCENE_LOGIN, SCENE_FORGET_PASSWORD);

    private final DefaultKaptcha captchaProducer;
    private final StringRedisTemplate redisTemplate;

    public CaptchaResponse generate(String scene) {
        String normalizedScene = normalizeScene(scene);
        String token = UUID.randomUUID().toString();
        String code = captchaProducer.createText();
        BufferedImage image = CaptchaImageRenderer.render(code);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());

            String redisKey = captchaKey(normalizedScene, token);
            redisTemplate.opsForValue().set(redisKey, code.toLowerCase(), CacheConstants.TTL_CAPTCHA);

            return CaptchaResponse.builder()
                    .captchaToken(token)
                    .captchaImage("data:image/png;base64," + base64)
                    .build();
        } catch (Exception e) {
            throw new BizException("验证码生成失败");
        }
    }

    /**
     * 校验验证码（不消费），供登录/忘记密码在业务成功后再 {@link #consume}。
     */
    public void check(String scene, String token, String code) {
        assertCaptchaInput(token, code);
        String normalizedScene = normalizeScene(scene);
        String storedCode = redisTemplate.opsForValue().get(captchaKey(normalizedScene, token));
        if (storedCode == null || !storedCode.equals(normalizeInput(code))) {
            throw new BizException(401, "验证码错误或已过期");
        }
    }

    /**
     * 消费已校验通过的验证码（一次性删除 Redis 键）。
     */
    public void consume(String scene, String token) {
        if (!StringUtils.hasText(token)) {
            throw new BizException(401, "验证码无效或已过期");
        }
        redisTemplate.delete(captchaKey(normalizeScene(scene), token));
    }

    private void assertCaptchaInput(String token, String code) {
        if (!StringUtils.hasText(token) || !StringUtils.hasText(code)) {
            throw new BizException(401, "验证码不能为空");
        }
    }

    private String normalizeScene(String scene) {
        if (!StringUtils.hasText(scene)) {
            throw new BizException(400, "验证码场景无效");
        }
        String normalized = scene.trim();
        if (!ALLOWED_SCENES.contains(normalized)) {
            throw new BizException(400, "验证码场景无效");
        }
        return normalized;
    }

    private String normalizeInput(String code) {
        return code.trim().toLowerCase();
    }

    private String captchaKey(String scene, String token) {
        return CacheConstants.captchaKey(scene, token);
    }
}
