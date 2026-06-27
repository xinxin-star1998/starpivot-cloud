package cn.org.starpivot.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 当前登录用户快照，作为 Spring Security {@code Authentication} 的 principal。
 * <p>
 * 由网关 JWT 过滤器或 {@link cn.org.starpivot.common.filter.MicroserviceAuthenticationFilter} 解析后写入上下文，
 * 供 {@link SecurityContextUtils} 及 {@link AuthorityResolver} 使用。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long userId;

    /** 登录用户名 */
    private String username;

    /** 角色标识列表（如 {@code admin}、{@code ROLE_admin}） */
    private List<String> roles;

    /** 设备会话 ID，写入 JWT 供登出/会话管理使用 */
    private String sessionId;
}
