package cn.org.starpivot.mall.portal.auth.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 开发环境 Mock 短信（仅打日志）。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "starpivot.mall.portal-auth.sms.mock-enabled", havingValue = "true", matchIfMissing = true)
public class MockSmsSender implements SmsSender {

    @Override
    public String provider() {
        return "mock";
    }

    @Override
    public void sendVerificationCode(String mobile, String code, String scene) {
        log.info("[MockSMS] scene={} mobile={} code={}", scene, mobile, code);
    }
}
