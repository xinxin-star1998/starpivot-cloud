package cn.org.starpivot.api.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 登录鉴权所需的用户信息（不含密码，密码校验走 verify-password 内部接口）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserAuthDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String nickName;
    private String status;
    private List<String> roles;
    private String avatar;
}
