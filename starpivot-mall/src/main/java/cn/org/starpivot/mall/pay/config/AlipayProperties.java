package cn.org.starpivot.mall.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 支付宝沙箱/生产配置（电脑网站支付）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "starpivot.mall.alipay")
public class AlipayProperties {

    /** 是否启用支付宝支付 */
    private boolean enabled = false;

    /** 应用 APPID */
    private String appId;

    /** 应用私钥（PKCS8，可不含头尾换行） */
    private String privateKey;

    /** 支付宝公钥（可不含头尾换行） */
    private String alipayPublicKey;

    /**
     * 网关地址。沙箱默认：
     * https://openapi-sandbox.dl.alipaydev.com/gateway.do
     */
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    /** 异步通知地址（须公网可达，经网关） */
    private String notifyUrl;

    /** 同步跳转地址（支付完成后浏览器跳回 C 端） */
    private String returnUrl;

    private static String normalizePem(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String trimmed = raw.trim();
        if (trimmed.contains("BEGIN")) {
            return trimmed;
        }
        return trimmed;
    }

    public boolean isConfigured() {
        return enabled
                && StringUtils.hasText(appId)
                && StringUtils.hasText(privateKey)
                && StringUtils.hasText(alipayPublicKey)
                && StringUtils.hasText(notifyUrl);
    }

    public String normalizedPrivateKey() {
        return normalizePem(privateKey);
    }

    public String normalizedAlipayPublicKey() {
        return normalizePem(alipayPublicKey);
    }
}
