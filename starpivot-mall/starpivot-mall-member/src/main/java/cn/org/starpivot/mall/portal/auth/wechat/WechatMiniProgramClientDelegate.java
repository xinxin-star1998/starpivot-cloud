package cn.org.starpivot.mall.portal.auth.wechat;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.portal.auth.config.PortalAuthProperties;
import cn.org.starpivot.mall.portal.auth.domain.model.WechatUserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 按配置选择真实或 Mock 小程序客户端。
 */
@Component
@Primary
@RequiredArgsConstructor
public class WechatMiniProgramClientDelegate implements WechatMiniProgramClient {

    private final PortalAuthProperties authProperties;
    private final WechatMiniProgramClientImpl realClient;
    private final MockWechatMiniProgramClient mockClient;

    @Override
    public boolean isConfigured() {
        PortalAuthProperties.MiniProgram mini = authProperties.getMiniProgram();
        if (mini.isEnabled() && realClient.isConfigured()) {
            return true;
        }
        return mini.isMockEnabled() && mockClient.isConfigured();
    }

    @Override
    public WechatUserProfile code2Session(String code) {
        WechatUserProfile profile = activeClient().code2Session(code);
        if (profile == null || !hasText(profile.bindingIdentifier())) {
            throw new BizException("微信登录码无效或已过期");
        }
        return profile;
    }

    private WechatMiniProgramClient activeClient() {
        PortalAuthProperties.MiniProgram mini = authProperties.getMiniProgram();
        if (mini.isEnabled() && realClient.isConfigured()) {
            return realClient;
        }
        if (mini.isMockEnabled()) {
            return mockClient;
        }
        throw new BizException("微信小程序登录未启用");
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
