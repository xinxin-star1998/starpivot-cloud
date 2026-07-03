package cn.org.starpivot.api.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 内部用户注册请求 DTO。
 * <p>
 * 由 auth 服务通过 Feign 传递给 system 模块完成用户创建。
 * <p>
 * 注解说明：
 * <ul>
 *   <li>{@link Data} — Lombok 自动生成 getter/setter 等方法</li>
 * </ul>
 */
@Data
public class RegisterUserRequest {

    /** 注册用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 注册密码（明文，仅在服务间传递） */
    @NotBlank(message = "密码不能为空")
    private String password;
}
