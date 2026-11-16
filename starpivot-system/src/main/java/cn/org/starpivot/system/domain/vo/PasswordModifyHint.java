package cn.org.starpivot.system.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录后密码修改提示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordModifyHint {

    /** 是否需要在登录后修改密码 */
    private boolean required;

    /** 原因：INIT_PASSWORD / PASSWORD_EXPIRED */
    private String reason;

    public static PasswordModifyHint none() {
        return new PasswordModifyHint(false, null);
    }

    public static PasswordModifyHint required(String reason) {
        return new PasswordModifyHint(true, reason);
    }
}
