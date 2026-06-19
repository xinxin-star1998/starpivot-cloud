package cn.org.starpivot.monitor.filter;

import cn.org.starpivot.common.security.JwtProperties;
import cn.org.starpivot.common.security.JwtUtils;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.common.security.SecurityConstants;
import cn.org.starpivot.monitor.security.mapper.AuthMenuMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MonitorAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final AuthMenuMapper authMenuMapper;

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

    private void authenticateWithToken(String token) {
        try {
            LoginUser user = JwtUtils.toLoginUser(JwtUtils.parseToken(token, jwtProperties.getSecret()));
            setAuthentication(user);
        } catch (JwtException ignored) {
            SecurityContextHolder.clearContext();
        }
    }

    private void setAuthentication(LoginUser user) {
        Set<String> authoritySet = new LinkedHashSet<>();
        if (user.getRoles() != null) {
            authoritySet.addAll(user.getRoles());
        }
        List<String> permissions = authMenuMapper.selectPermissionsByUserId(user.getUserId());
        if (permissions != null) {
            permissions.stream()
                    .filter(StringUtils::hasText)
                    .forEach(authoritySet::add);
        }
        List<SimpleGrantedAuthority> authorities = authoritySet.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

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
