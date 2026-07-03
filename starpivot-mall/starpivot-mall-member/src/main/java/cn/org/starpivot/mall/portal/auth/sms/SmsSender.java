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

    /** 是否由云厂商侧校验验证码（如阿里云 CheckSmsVerifyCode）。 */
    default boolean cloudVerification() {
        return false;
    }

    /** 云侧校验验证码；仅当 {@link #cloudVerification()} 为 true 时调用。 */
    default void verifyCloudCode(String mobile, String code, String scene) {
        throw new UnsupportedOperationException("cloud verification not supported by " + provider());
    }
}
