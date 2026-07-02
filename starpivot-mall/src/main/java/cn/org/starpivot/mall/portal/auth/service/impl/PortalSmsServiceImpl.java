package cn.org.starpivot.mall.portal.auth.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.util.LogUtils;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.auth.PortalAuthConstants;
import cn.org.starpivot.mall.portal.auth.config.PortalAuthProperties;
import cn.org.starpivot.mall.portal.auth.domain.bo.PortalSmsSendBo;
import cn.org.starpivot.mall.portal.auth.domain.vo.PortalSmsSendVo;
import cn.org.starpivot.mall.portal.auth.entity.UmsMemberSmsLog;
import cn.org.starpivot.mall.portal.auth.mapper.UmsMemberSmsLogMapper;
import cn.org.starpivot.mall.portal.auth.service.PortalMemberAuthService;
import cn.org.starpivot.mall.portal.auth.service.PortalSmsService;
import cn.org.starpivot.mall.portal.auth.sms.SmsSender;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PortalSmsServiceImpl implements PortalSmsService {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.BASIC_ISO_DATE;

    private final PortalAuthProperties authProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final SmsSender smsSender;
    private final UmsMemberSmsLogMapper smsLogMapper;
    private final PortalMemberAuthService memberAuthService;

    @Override
    public PortalSmsSendVo sendCode(PortalSmsSendBo bo, HttpServletRequest request) {
        String mobile = bo.getMobile();
        String scene = bo.getScene();
        assertSceneAllowed(scene, mobile);

        String intervalKey = PortalAuthConstants.smsIntervalKey(mobile);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(intervalKey))) {
            throw new BizException("发送过于频繁，请稍后再试");
        }

        String day = LocalDate.now().format(DAY_FMT);
        String mobileDailyKey = PortalAuthConstants.smsDailyMobileKey(mobile, day);
        long mobileCount = incrementWithTtl(mobileDailyKey, Duration.ofHours(25));
        if (mobileCount > authProperties.getSms().getDailyLimitPerMobile()) {
            throw new BizException("今日发送次数已达上限");
        }

        String ip = request != null ? LogUtils.getClientIp(request) : "";
        if (StringUtils.hasText(ip)) {
            String ipDailyKey = PortalAuthConstants.smsDailyIpKey(ip, day);
            long ipCount = incrementWithTtl(ipDailyKey, Duration.ofHours(25));
            if (ipCount > authProperties.getSms().getDailyLimitPerIp()) {
                throw new BizException("今日发送次数已达上限");
            }
        }

        String code = generateCode();
        String codeKey = PortalAuthConstants.smsCodeKey(scene, mobile);
        int ttl = authProperties.getSms().getCodeTtlSeconds();
        if (!smsSender.cloudVerification()) {
            stringRedisTemplate.opsForValue().set(codeKey, code, Duration.ofSeconds(ttl));
        }
        stringRedisTemplate.opsForValue().set(
                intervalKey, "1", Duration.ofSeconds(authProperties.getSms().getSendIntervalSeconds()));

        boolean sent = false;
        try {
            smsSender.sendVerificationCode(mobile, code, scene);
            sent = true;
        } finally {
            writeSmsLog(mobile, scene, smsSender.provider(), sent ? 1 : 0, ip);
        }

        return new PortalSmsSendVo(ttl);
    }

    @Override
    public void verifyAndConsume(String scene, String mobile, String code) {
        if (!StringUtils.hasText(code)) {
            throw new BizException("验证码不能为空");
        }
        if (smsSender.cloudVerification()) {
            smsSender.verifyCloudCode(mobile, code.trim(), scene);
            return;
        }
        String codeKey = PortalAuthConstants.smsCodeKey(scene, mobile);
        String cached = stringRedisTemplate.opsForValue().get(codeKey);
        if (!StringUtils.hasText(cached)) {
            throw new BizException("验证码已过期，请重新获取");
        }
        if (!cached.equals(code.trim())) {
            throw new BizException("验证码错误");
        }
        stringRedisTemplate.delete(codeKey);
    }

    private void assertSceneAllowed(String scene, String mobile) {
        if (PortalAuthConstants.SMS_SCENE_LOGIN.equals(scene)) {
            return;
        }
        if (PortalAuthConstants.SMS_SCENE_REGISTER.equals(scene)) {
            memberAuthService.assertIdentifierAvailable(
                    cn.org.starpivot.mall.portal.auth.PortalAuthType.MOBILE, mobile, null);
            return;
        }
        Long memberId = PortalMemberContext.requireMemberId();
        if (PortalAuthConstants.SMS_SCENE_BIND.equals(scene)) {
            memberAuthService.assertIdentifierAvailable(
                    cn.org.starpivot.mall.portal.auth.PortalAuthType.MOBILE, mobile, memberId);
            return;
        }
        String boundMobile = memberAuthService.resolveSmsMobile(memberId);
        if (!boundMobile.equals(mobile)) {
            throw new BizException("请使用已绑定的手机号接收验证码");
        }
    }

    private String generateCode() {
        if (authProperties.getSms().isMockEnabled() && StringUtils.hasText(authProperties.getSms().getMockCode())) {
            return authProperties.getSms().getMockCode();
        }
        int length = authProperties.getSms().getCodeLength();
        int bound = (int) Math.pow(10, length);
        int code = ThreadLocalRandom.current().nextInt(bound / 10, bound);
        return String.valueOf(code);
    }

    private long incrementWithTtl(String key, Duration ttl) {
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, ttl);
        }
        return count != null ? count : 0L;
    }

    private void writeSmsLog(String mobile, String scene, String provider, int status, String ip) {
        UmsMemberSmsLog log = new UmsMemberSmsLog();
        log.setMobile(mobile);
        log.setScene(scene);
        log.setProvider(provider);
        log.setSendStatus(status);
        log.setIp(ip);
        log.setCreateTime(LocalDateTime.now());
        smsLogMapper.insert(log);
    }
}
