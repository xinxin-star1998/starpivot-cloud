package cn.org.starpivot.auth.security;

import cn.org.starpivot.auth.service.TokenBlacklistService;
import cn.org.starpivot.common.security.TokenBlacklistChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthTokenBlacklistChecker implements TokenBlacklistChecker {

    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public boolean isBlacklisted(String token) {
        return tokenBlacklistService.isBlacklisted(token);
    }
}
