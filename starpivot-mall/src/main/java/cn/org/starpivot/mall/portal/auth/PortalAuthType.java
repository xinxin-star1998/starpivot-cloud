package cn.org.starpivot.mall.portal.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * C 端会员登录方式。
 */
@Getter
@RequiredArgsConstructor
public enum PortalAuthType {

    PASSWORD(1, "密码"),
    MOBILE(2, "手机号"),
    WECHAT(3, "微信"),
    QQ(4, "QQ"),
    ALIPAY(5, "支付宝"),
    APPLE(6, "Apple"),
    EMAIL(7, "邮箱");

    private final int code;
    private final String label;

    public static PortalAuthType fromCode(int code) {
        for (PortalAuthType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown auth type: " + code);
    }
}
