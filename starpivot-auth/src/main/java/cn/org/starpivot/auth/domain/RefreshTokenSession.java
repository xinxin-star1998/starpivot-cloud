package cn.org.starpivot.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 刷新令牌与会话 ID 绑定结果。
 */
@Getter
@AllArgsConstructor
public class RefreshTokenSession {

    private final String refreshToken;
    private final String sessionId;
}
