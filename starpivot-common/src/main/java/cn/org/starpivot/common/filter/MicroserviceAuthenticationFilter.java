package cn.org.starpivot.common.filter;

import cn.org.starpivot.common.security.AuthorityResolver;
import cn.org.starpivot.common.security.JwtProperties;
import cn.org.starpivot.common.security.JwtUtils;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.common.security.SecurityConstants;
import cn.org.starpivot.common.security.TokenBlacklistChecker;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 微服务统一鉴权 Servlet 过滤器。
 * <p>
 * 继承 {@link OncePerRequestFilter}，在 {@link SecurityContextHolder} 尚无认证信息时，
 * 按以下顺序尝试构建 {@link org.springframework.security.core.Authentication}：
 * <ol>
 *   <li>Bearer JWT — 从 {@link SecurityConstants#TOKEN_HEADER} 解析并校验</li>
 *   <li>网关透传 Header — 用户 ID、用户名、角色 CSV（直连经网关转发的场景）</li>
 * </ol>
 * 权限字符串由 {@link AuthorityResolver} 按 {@link cn.org.starpivot.common.config.MicroserviceSecurityProperties} 策略解析。
 * <p>
 * 通常由 {@link cn.org.starpivot.common.config.MicroserviceAuthenticationAutoConfiguration} 注册为 Bean，
 * 并插入各微服务 {@code SecurityFilterChain}。
 *
 * @see AuthorityResolver
 * @see JwtUtils
 * @see LoginUser
 */
public class MicroserviceAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final AuthorityResolver authorityResolver;
    private final TokenBlacklistChecker tokenBlacklistChecker;

    /**
     * 构造过滤器（不含 Token 黑名单检查）。
     *
     * @param jwtProperties     JWT 配置（含密钥）
     * @param authorityResolver 权限解析策略
     */
    public MicroserviceAuthenticationFilter(
            JwtProperties jwtProperties,
            AuthorityResolver authorityResolver) {
        this(jwtProperties, authorityResolver, null);
    }

    /**
     * 构造过滤器（完整依赖）。
     *
     * @param jwtProperties           JWT 配置（含密钥）
     * @param authorityResolver         权限解析策略
     * @param tokenBlacklistChecker     Token 黑名单检查器，可为 {@code null} 表示不校验黑名单
     */
    public MicroserviceAuthenticationFilter(
            JwtProperties jwtProperties,
            AuthorityResolver authorityResolver,
            TokenBlacklistChecker tokenBlacklistChecker) {
        this.jwtProperties = jwtProperties;
        this.authorityResolver = authorityResolver;
        this.tokenBlacklistChecker = tokenBlacklistChecker;
    }

    /**
     * 尝试 JWT 或网关 Header 认证，并继续过滤器链。
     * <p>
     * 若 {@link SecurityContextHolder} 已有 {@code Authentication}，则不再覆盖。
     *
     * @param request     当前 HTTP 请求
     * @param response    当前 HTTP 响应
     * @param filterChain 后续过滤器链
     * @throws ServletException Servlet 处理异常
     * @throws IOException      I/O 异常
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = JwtUtils.resolveToken(request.getHeader(SecurityConstants.TOKEN_HEADER));
            if (StringUtils.hasText(token)) {
                authenticateWithToken(token);
            }
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateWithGatewayHeaders(request);
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 根据网关透传 Header 构建认证上下文。
     * <p>
     * 需要同时存在 {@link SecurityConstants#USER_ID_HEADER} 与
     * {@link SecurityConstants#USER_NAME_HEADER}；用户 ID 非数字时清空上下文。
     *
     * @param request 含网关透传 Header 的 HTTP 请求
     */
    private void authenticateWithGatewayHeaders(HttpServletRequest request) {
        String userIdHeader = request.getHeader(SecurityConstants.USER_ID_HEADER);
        String username = request.getHeader(SecurityConstants.USER_NAME_HEADER);
        if (!StringUtils.hasText(userIdHeader) || !StringUtils.hasText(username)) {
            return;
        }
        try {
            Long userId = Long.parseLong(userIdHeader);
            List<String> roles = parseCsv(request.getHeader(SecurityConstants.ROLES_HEADER));
            LoginUser user = LoginUser.builder()
                    .userId(userId)
                    .username(username)
                    .roles(roles)
                    .build();
            setAuthentication(user);
        } catch (NumberFormatException ignored) {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * 解析 JWT 并构建认证上下文。
     * <p>
     * 若配置了 {@link TokenBlacklistChecker} 且 Token 在黑名单中则跳过；
     * 解析失败时清空 {@link SecurityContextHolder}，不抛出异常。
     *
     * @param token 已剥离 Bearer 前缀的 JWT 字符串
     */
    private void authenticateWithToken(String token) {
        if (tokenBlacklistChecker != null && tokenBlacklistChecker.isBlacklisted(token)) {
            return;
        }
        try {
            LoginUser user = JwtUtils.toLoginUser(JwtUtils.parseToken(token, jwtProperties.getSecret()));
            setAuthentication(user);
        } catch (JwtException ignored) {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * 将 {@link LoginUser} 转为 {@link UsernamePasswordAuthenticationToken} 并写入安全上下文。
     *
     * @param user 已解析的登录用户
     */
    private void setAuthentication(LoginUser user) {
        Collection<String> authorities = authorityResolver.resolveAuthorities(user);
        List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        var authentication = new UsernamePasswordAuthenticationToken(user, null, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 将逗号分隔字符串解析为非空 trim 后的列表。
     *
     * @param value CSV 原始值，可为 {@code null} 或空白
     * @return 角色/权限名列表；无有效项时返回空列表
     */
    private static List<String> parseCsv(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }
}
