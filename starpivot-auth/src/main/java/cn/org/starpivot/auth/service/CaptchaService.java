package cn.org.starpivot.auth.service;

import cn.org.starpivot.auth.domain.CaptchaResponse;
import cn.org.starpivot.auth.domain.CaptchaVerifyRequest;
import cn.org.starpivot.auth.domain.CaptchaVerifyResponse;
import cn.org.starpivot.common.cache.CacheConstants;
import cn.org.starpivot.common.exception.BusinessException;
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

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final long CAPTCHA_TTL_MINUTES = CacheConstants.TTL_CAPTCHA.toMinutes();
    private static final long PROOF_TTL_MINUTES = CacheConstants.TTL_CAPTCHA_PROOF.toMinutes();

    private final DefaultKaptcha captchaProducer;
    private final StringRedisTemplate redisTemplate;

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
            throw new BusinessException("验证码生成失败");
        }
    }

    public CaptchaVerifyResponse verify(CaptchaVerifyRequest request) {
        if (!StringUtils.hasText(request.getCaptchaToken()) || !StringUtils.hasText(request.getCode())) {
            throw new BusinessException(401, "验证码不能为空");
        }

        String scene = StringUtils.hasText(request.getScene()) ? request.getScene() : "login";
        String redisKey = buildCaptchaKey(scene, request.getCaptchaToken());
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        redisTemplate.delete(redisKey);

        if (storedCode == null || !storedCode.equalsIgnoreCase(request.getCode().trim())) {
            throw new BusinessException(401, "验证码错误或已过期");
        }

        String proof = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(CacheConstants.captchaProofKey(proof), "1", PROOF_TTL_MINUTES, TimeUnit.MINUTES);
        return CaptchaVerifyResponse.builder().captchaProof(proof).build();
    }

    private String buildCaptchaKey(String scene, String token) {
        return CacheConstants.captchaKey(scene, token);
    }
}
