package cn.org.starpivot.mall.portal.auth;

/**
 * C 端认证常量。
 */
public final class PortalAuthConstants {

    public static final int AUTH_STATUS_ACTIVE = 1;
    public static final int AUTH_STATUS_UNBOUND = 0;

    public static final int LOGIN_TYPE_PASSWORD = 1;
    public static final int LOGIN_TYPE_SMS = 2;
    public static final int LOGIN_TYPE_WECHAT = 3;
    public static final int LOGIN_TYPE_AUTO_REGISTER = 10;

    public static final String SMS_SCENE_LOGIN = "login";
    public static final String SMS_SCENE_REGISTER = "register";
    public static final String SMS_SCENE_BIND = "bind";
    public static final String SMS_SCENE_SET_PASSWORD = "set_password";
    public static final String SMS_SCENE_UNBIND = "unbind";

    public static final String OAUTH_MODE_LOGIN = "login";
    public static final String OAUTH_MODE_REGISTER = "register";
    public static final String OAUTH_MODE_BIND = "bind";

    /** 手机注册来源 */
    public static final int SOURCE_TYPE_MOBILE = 3;
    /** 微信注册来源 */
    public static final int SOURCE_TYPE_WECHAT = 2;

    private PortalAuthConstants() {
    }

    public static String oauthStateKey(String state) {
        return "mall:portal:oauth:state:" + state;
    }

    public static String smsCodeKey(String scene, String mobile) {
        return "mall:portal:sms:code:" + scene + ":" + mobile;
    }

    public static String smsIntervalKey(String mobile) {
        return "mall:portal:sms:interval:" + mobile;
    }

    public static String smsDailyMobileKey(String mobile, String day) {
        return "mall:portal:sms:daily:mobile:" + mobile + ":" + day;
    }

    public static String smsDailyIpKey(String ip, String day) {
        return "mall:portal:sms:daily:ip:" + ip + ":" + day;
    }

    public static String loginFailKey(String identifier) {
        return "mall:portal:login:fail:" + identifier;
    }
}
