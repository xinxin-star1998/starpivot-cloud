package cn.org.starpivot.system.constants;

/**
 * 登录后需修改密码的原因
 */
public final class PasswordModifyReason {

    private PasswordModifyReason() {
    }

    /** 仍在使用系统初始密码 */
    public static final String INIT_PASSWORD = "INIT_PASSWORD";

    /** 密码已超过配置的有效期 */
    public static final String PASSWORD_EXPIRED = "PASSWORD_EXPIRED";
}
