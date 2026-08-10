package cn.org.starpivot.mall.portal.auth.wechat;

import cn.org.starpivot.mall.portal.auth.config.PortalAuthProperties;
import cn.org.starpivot.mall.portal.auth.domain.model.WechatUserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 微信小程序登录 / 手机号 / 订阅消息 Mock（开发联调）。
 */
@Slf4j
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

    @Override
    public String getPhoneNumber(String phoneCode) {
        PortalAuthProperties.MiniProgram mini = authProperties.getMiniProgram();
        if (StringUtils.hasText(mini.getMockPhoneCode())
                && !mini.getMockPhoneCode().equals(phoneCode)
                && !"mock_any".equals(mini.getMockPhoneCode())) {
            // mockPhoneCode 非空且不等于入参时仍允许：开发态任意 code 返回 mockMobile
            log.debug("Mock getPhoneNumber accept code={}", phoneCode);
        }
        return mini.getMockMobile();
    }

    @Override
    public void sendSubscribeMessage(String openId, String templateId, String page, Map<String, String> data) {
        log.info("Mock subscribe message openId={}, templateId={}, page={}, data={}",
                openId, templateId, page, data);
    }
}
