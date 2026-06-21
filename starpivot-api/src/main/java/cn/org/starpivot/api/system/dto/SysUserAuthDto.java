package cn.org.starpivot.api.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 登录鉴权所需的用户信息（含密码哈希，仅服务间调用）
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
    private String password;
    private String nickName;
    private String status;
    private List<String> roles;
    private String avatar;
}
