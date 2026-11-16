package cn.org.starpivot.system.constants;

/**
 * 系统参数配置键常量类。
 * <p>
 * 集中定义 {@link cn.org.starpivot.system.domain.entity.SysConfig} 表中使用的配置键名，
 * 避免魔法字符串散落各处。
 * </p>
 */
public final class SysConfigKeys {

    private SysConfigKeys() {
    }

    /** 是否开放用户自助注册（值为 {@code true}/{@code false}） */
    public static final String REGISTER_USER = "sys.account.registerUser";

    /** 是否开放忘记密码（值为 {@code true}/{@code false}） */
    public static final String FORGET_PASSWORD = "sys.account.forgetPassword";

    /** 用户管理-账号初始密码 */
    public static final String USER_INIT_PASSWORD = "sys.user.initPassword";

    /** 初始密码修改策略（{@code 1} 表示登录时强制提醒修改初始密码） */
    public static final String ACCOUNT_INIT_PASSWORD_MODIFY = "sys.account.initPasswordModify";

    /** 密码有效期天数（{@code 0} 表示不限制） */
    public static final String ACCOUNT_PASSWORD_VALIDATE_DAYS = "sys.account.passwordValidateDays";

    /** 初始密码修改策略默认值：关闭 */
    public static final int DEFAULT_INIT_PASSWORD_MODIFY = 0;

    /** 密码有效期默认值：不限制 */
    public static final int DEFAULT_PASSWORD_VALIDATE_DAYS = 0;
}
