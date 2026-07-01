package cn.org.starpivot.mall.portal.auth.sms;

/**
 * 短信发送抽象。
 */
public interface SmsSender {

    /**
     * @return 提供商标识，如 mock / aliyun
     */
    String provider();

    /**
     * 发送验证码短信。
     */
    void sendVerificationCode(String mobile, String code, String scene);
}
