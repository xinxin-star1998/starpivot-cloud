package cn.org.starpivot.mall.portal.auth.sms;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.portal.auth.config.PortalAuthProperties;
import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Mock 或阿里云号码认证短信发送。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MockSmsSender implements SmsSender {

    private final PortalAuthProperties authProperties;

    @Override
    public String provider() {
        return useAliyun() ? "aliyun" : "mock";
    }

    @Override
    public boolean cloudVerification() {
        return useAliyun() && "cloud".equalsIgnoreCase(authProperties.getSms().getAliyun().getVerifyMode());
    }

    @Override
    public void sendVerificationCode(String mobile, String code, String scene) {
        if (authProperties.getSms().isMockEnabled()) {
            // 禁止将手机号/验证码明文写入日志
            log.info("[MockSMS] scene={} mobile={} code=******", scene, maskMobile(mobile));
            return;
        }
        if (!useAliyun()) {
            throw new BizException("SMS provider is not configured");
        }
        sendByAliyun(mobile, code);
    }

    @Override
    public void verifyCloudCode(String mobile, String code, String scene) {
        throw new BizException("cloud verify mode is not enabled in this build");
    }

    private boolean useAliyun() {
        PortalAuthProperties.Sms sms = authProperties.getSms();
        PortalAuthProperties.Sms.Aliyun aliyun = sms.getAliyun();
        return !sms.isMockEnabled()
                && aliyun.isEnabled()
                && StringUtils.hasText(aliyun.getSignName())
                && StringUtils.hasText(aliyun.getTemplateCode())
                && hasAliyunCredentials(aliyun);
    }

    private void sendByAliyun(String mobile, String code) {
        PortalAuthProperties.Sms sms = authProperties.getSms();
        PortalAuthProperties.Sms.Aliyun aliyun = sms.getAliyun();
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setPhoneNumber(mobile)
                .setSignName(aliyun.getSignName())
                .setTemplateCode(aliyun.getTemplateCode())
                .setCountryCode(aliyun.getCountryCode())
                .setValidTime((long) sms.getCodeTtlSeconds())
                .setInterval((long) sms.getSendIntervalSeconds());
        if (StringUtils.hasText(aliyun.getSchemeName())) {
            request.setSchemeName(aliyun.getSchemeName());
        }
        if (cloudVerification()) {
            request.setTemplateParam(String.format("{\"code\":\"##code##\",\"min\":\"%d\"}", aliyun.getTemplateMinMinutes()))
                    .setCodeType(1L)
                    .setCodeLength((long) sms.getCodeLength());
        } else {
            request.setTemplateParam(String.format("{\"code\":\"%s\",\"min\":\"%d\"}", code, aliyun.getTemplateMinMinutes()));
        }
        try {
            SendSmsVerifyCodeResponse response = createAliyunClient().sendSmsVerifyCodeWithOptions(request, new RuntimeOptions());
            SendSmsVerifyCodeResponseBody body = response.getBody();
            if (body == null || !"OK".equalsIgnoreCase(body.getCode())) {
                throw new BizException(body != null && StringUtils.hasText(body.getMessage()) ? body.getMessage() : "SMS send failed");
            }
            log.info("[AliyunSMS] sent mobile={}", maskMobile(mobile));
        } catch (BizException ex) {
            throw ex;
        } catch (TeaException ex) {
            log.warn("[AliyunSMS] send failed mobile={} message={}", maskMobile(mobile), ex.getMessage());
            throw new BizException(StringUtils.hasText(ex.getMessage()) ? ex.getMessage() : "SMS send failed");
        } catch (Exception ex) {
            log.error("[AliyunSMS] send error mobile={}", maskMobile(mobile), ex);
            throw new BizException("SMS send failed, please retry later");
        }
    }

    /** 日志脱敏：138****8000 */
    static String maskMobile(String mobile) {
        if (!StringUtils.hasText(mobile) || mobile.length() < 7) {
            return "***";
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }

    private Client createAliyunClient() throws Exception {
        PortalAuthProperties.Sms.Aliyun aliyun = authProperties.getSms().getAliyun();
        Config config = new Config()
                .setAccessKeyId(resolveAccessKeyId(aliyun))
                .setAccessKeySecret(resolveAccessKeySecret(aliyun));
        config.endpoint = aliyun.getEndpoint();
        return new Client(config);
    }

    private static boolean hasAliyunCredentials(PortalAuthProperties.Sms.Aliyun aliyun) {
        return StringUtils.hasText(resolveAccessKeyId(aliyun))
                && StringUtils.hasText(resolveAccessKeySecret(aliyun));
    }

    private static String resolveAccessKeyId(PortalAuthProperties.Sms.Aliyun aliyun) {
        if (StringUtils.hasText(aliyun.getAccessKeyId())) {
            return aliyun.getAccessKeyId();
        }
        return System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
    }

    private static String resolveAccessKeySecret(PortalAuthProperties.Sms.Aliyun aliyun) {
        if (StringUtils.hasText(aliyun.getAccessKeySecret())) {
            return aliyun.getAccessKeySecret();
        }
        return System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
    }
}
