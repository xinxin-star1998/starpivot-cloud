package cn.org.starpivot.auth.filter;

import cn.org.starpivot.auth.service.TokenBlacklistService;
import cn.org.starpivot.common.security.JwtProperties;
import cn.org.starpivot.common.security.JwtUtils;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.common.security.SecurityConstants;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = JwtUtils.resolveToken(request.getHeader(SecurityConstants.TOKEN_HEADER));
            if (token != null) {
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
        if (userIdHeader == null || username == null || username.isBlank()) {
            return;
        }
        try {
            Long userId = Long.parseLong(userIdHeader);
            String rolesHeader = request.getHeader(SecurityConstants.ROLES_HEADER);
            List<String> roles = rolesHeader == null || rolesHeader.isBlank()
                    ? Collections.emptyList()
                    : Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
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
        if (tokenBlacklistService.isBlacklisted(token)) {
            return;
        }
        try {
            LoginUser user = JwtUtils.toLoginUser(JwtUtils.parseToken(token, jwtProperties.getSecret()));
            setAuthentication(user);
        } catch (JwtException ignored) {
            SecurityContextHolder.clearContext();
        }
    }

    private void setAuthentication(LoginUser user) {
        var authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
