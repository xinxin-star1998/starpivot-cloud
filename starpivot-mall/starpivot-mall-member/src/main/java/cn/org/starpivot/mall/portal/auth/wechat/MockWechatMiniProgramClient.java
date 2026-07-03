package cn.org.starpivot.mall.portal.auth.wechat;

import cn.org.starpivot.mall.portal.auth.config.PortalAuthProperties;
import cn.org.starpivot.mall.portal.auth.domain.model.WechatUserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 微信小程序登录 Mock（开发联调）。
 */
@Component
@RequiredArgsConstructor
public class MockWechatMiniProgramClient implements WechatMiniProgramClient {

    private final PortalAuthProperties authProperties;

    @Override
    public boolean isConfigured() {
        return authProperties.getMiniProgram().isMockEnabled();
    }

    @Override
    public WechatUserProfile code2Session(String code) {
        PortalAuthProperties.MiniProgram mini = authProperties.getMiniProgram();
        if (!mini.getMockCode().equals(code)) {
            return null;
        }
        return WechatUserProfile.builder()
                .unionId(mini.getMockUnionId())
                .openId(mini.getMockOpenId())
                .nickname(mini.getMockNickname())
                .avatar(mini.getMockAvatar())
                .appId("mock-mini")
                .build();
    }
}
