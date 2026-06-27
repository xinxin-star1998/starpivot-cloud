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
}
