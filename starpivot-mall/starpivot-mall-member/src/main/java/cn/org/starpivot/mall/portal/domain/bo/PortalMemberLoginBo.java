package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Memberlogin请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalMemberLoginBo {

    /**
     * account
     */
    /**
     * account
     */
    @NotBlank(message = "账号不能为空")
    /**
     * account
     */
    private String account;

    /**
     * 密码
     */
    /**
     * password
     */
    @NotBlank(message = "密码不能为空")
    /**
     * password
     */
    private String password;
}
