package cn.org.starpivot.mall.portal.auth.wechat;

import cn.org.starpivot.mall.portal.auth.config.PortalAuthProperties;
import cn.org.starpivot.mall.portal.auth.domain.model.WechatUserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 微信 OAuth Mock（开发联调）。
 */
@Component
@RequiredArgsConstructor
public class MockWechatOAuthClient implements WechatOAuthClient {

    private final PortalAuthProperties authProperties;

    @Override
    public boolean isConfigured() {
        return authProperties.getWechat().isMockEnabled();
    }

    @Override
    public String buildAuthorizeUrl(String state, String callbackUri) {
        PortalAuthProperties.Wechat wechat = authProperties.getWechat();
        String redirect = StringUtils.hasText(callbackUri) ? callbackUri : wechat.getRedirectUri();
        String separator = redirect.contains("?") ? "&" : "?";
        return redirect + separator + "code=" + wechat.getMockCode() + "&state=" + state;
    }

    @Override
    public WechatUserProfile exchangeCode(String code) {
        PortalAuthProperties.Wechat wechat = authProperties.getWechat();
        if (!wechat.getMockCode().equals(code)) {
            return null;
        }
        return WechatUserProfile.builder()
                .unionId(wechat.getMockUnionId())
                .openId(wechat.getMockOpenId())
                .nickname(wechat.getMockNickname())
                .avatar(wechat.getMockAvatar())
                .appId("mock")
                .build();
    }
}
