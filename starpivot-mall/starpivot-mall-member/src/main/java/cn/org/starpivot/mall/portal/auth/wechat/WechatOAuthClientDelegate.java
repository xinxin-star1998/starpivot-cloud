package cn.org.starpivot.mall.portal.auth.wechat;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.portal.auth.config.PortalAuthProperties;
import cn.org.starpivot.mall.portal.auth.domain.model.WechatUserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 按配置选择真实或 Mock 微信客户端。
 */
@Component
@Primary
@RequiredArgsConstructor
public class WechatOAuthClientDelegate implements WechatOAuthClient {

    private final PortalAuthProperties authProperties;
    private final WechatOAuthClientImpl realClient;
    private final MockWechatOAuthClient mockClient;

    @Override
    public boolean isConfigured() {
        PortalAuthProperties.Wechat wechat = authProperties.getWechat();
        if (wechat.isEnabled() && realClient.isConfigured()) {
            return true;
        }
        return wechat.isMockEnabled() && mockClient.isConfigured();
    }

    @Override
    public String buildAuthorizeUrl(String state, String callbackUri) {
        return activeClient().buildAuthorizeUrl(state, callbackUri);
    }

    @Override
    public WechatUserProfile exchangeCode(String code) {
        WechatUserProfile profile = activeClient().exchangeCode(code);
        if (profile == null || !hasText(profile.bindingIdentifier())) {
            throw new BizException("微信授权码无效或已过期");
        }
        return profile;
    }

    private WechatOAuthClient activeClient() {
        PortalAuthProperties.Wechat wechat = authProperties.getWechat();
        if (wechat.isEnabled() && realClient.isConfigured()) {
            return realClient;
        }
        if (wechat.isMockEnabled()) {
            return mockClient;
        }
        throw new BizException("微信登录未启用");
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
