package cn.org.starpivot.api.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 内部密码校验请求 DTO。
 * <p>
 * 明文密码仅在服务间 POST 传递，不写入日志或持久化。
 * <p>
 * 注解说明：
 * <ul>
 *   <li>{@link Data} — Lombok 自动生成 getter/setter 等方法</li>
 * </ul>
 */
@Data
public class VerifyPasswordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 待校验的用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;
    /** 待校验的明文密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
