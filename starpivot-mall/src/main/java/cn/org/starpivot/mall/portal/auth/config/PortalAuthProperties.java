package cn.org.starpivot.mall.portal.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * C 端认证配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "starpivot.mall.portal-auth")
public class PortalAuthProperties {

    private Sms sms = new Sms();
    private Password password = new Password();
    private Wechat wechat = new Wechat();
    private MiniProgram miniProgram = new MiniProgram();

    @Data
    public static class Wechat {
        /** 是否启用（真实微信或 Mock） */
        private boolean enabled = false;
        /** 开发 Mock：无微信开放平台凭证时可联调 */
        private boolean mockEnabled = true;
        private String appId = "";
        private String appSecret = "";
        /** 前端回调页，如 http://localhost:3000/portal/login/wechat/callback */
        private String redirectUri = "http://localhost:3000/portal/login/wechat/callback";
        /** open=网站应用扫码；mp=公众号网页授权 */
        private String mode = "open";
        private boolean autoRegister = true;
        private String mockCode = "mock_wx";
        private String mockUnionId = "mock_union_001";
        private String mockOpenId = "mock_open_001";
        private String mockNickname = "微信测试用户";
        private String mockAvatar = "";
    }

    @Data
    public static class Sms {
        private int codeLength = 6;
        private int codeTtlSeconds = 300;
        private int sendIntervalSeconds = 60;
        private int dailyLimitPerMobile = 10;
        private int dailyLimitPerIp = 50;
        private boolean mockEnabled = true;
        private String mockCode = "123456";
    }

    @Data
    public static class Password {
        private int maxFailCount = 5;
        private int lockMinutes = 30;
    }

    @Data
    public static class MiniProgram {
        /** 是否启用真实 jscode2session */
        private boolean enabled = false;
        /** 开发 Mock：无小程序凭证时可联调 */
        private boolean mockEnabled = true;
        private String appId = "";
        private String appSecret = "";
        private boolean autoRegister = true;
        private String mockCode = "mock_mini_code";
        private String mockUnionId = "mock_mini_union_001";
        private String mockOpenId = "mock_mini_open_001";
        private String mockNickname = "小程序测试用户";
        private String mockAvatar = "";
    }
}
