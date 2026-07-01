package cn.org.starpivot.mall.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 微信支付 Native（扫码）配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "starpivot.mall.wxpay")
public class WxPayProperties {

    private boolean enabled = false;

    /** 开发 Mock：无商户凭证时可联调 Native 下单与 Mock 确认 */
    private boolean mockEnabled = true;

    /** 公众号 / 开放平台 AppId */
    private String appId;

    /** 商户号 */
    private String mchId;

    /** APIv3 密钥 */
    private String apiV3Key;

    /** 商户 API 证书序列号 */
    private String merchantSerialNumber;

    /** 商户私钥（PKCS8 PEM 内容，可不含头尾） */
    private String privateKey;

    /** 异步通知地址（须公网可达，经网关） */
    private String notifyUrl;

    public boolean isConfigured() {
        return enabled
                && StringUtils.hasText(appId)
                && StringUtils.hasText(mchId)
                && StringUtils.hasText(apiV3Key)
                && StringUtils.hasText(merchantSerialNumber)
                && StringUtils.hasText(privateKey)
                && StringUtils.hasText(notifyUrl);
    }

    public boolean isMockAvailable() {
        return mockEnabled && !isConfigured();
    }

    public boolean isUsable() {
        return isConfigured() || isMockAvailable();
    }

    public String normalizedPrivateKey() {
        if (!StringUtils.hasText(privateKey)) {
            return privateKey;
        }
        String trimmed = privateKey.trim();
        if (trimmed.contains("BEGIN")) {
            return trimmed;
        }
        return "-----BEGIN PRIVATE KEY-----\n" + trimmed + "\n-----END PRIVATE KEY-----";
    }
}
