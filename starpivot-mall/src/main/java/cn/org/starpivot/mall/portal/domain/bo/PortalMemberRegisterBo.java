package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Memberregister请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalMemberRegisterBo {

    /**
     * 用户名
     */
    /**
     * username
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 32, message = "用户名长度为2-32")
    /**
     * username
     */
    private String username;

    /**
     * 密码
     */
    /**
     * password
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度为6-32")
    /**
     * password
     */
    private String password;

    /**
     * nickname
     */
    private String nickname;

    /**
     * mobile
     */
    private String mobile;
}
