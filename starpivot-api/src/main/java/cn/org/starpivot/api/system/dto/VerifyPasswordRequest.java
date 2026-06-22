package cn.org.starpivot.api.system.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 内部密码校验请求（明文密码仅在服务间 POST 传递，不写入日志）。
 */
@Data
public class VerifyPasswordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
}
