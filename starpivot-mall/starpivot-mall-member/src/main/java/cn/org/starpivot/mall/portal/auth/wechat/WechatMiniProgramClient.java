package cn.org.starpivot.mall.portal.auth.wechat;

import cn.org.starpivot.mall.portal.auth.domain.model.WechatUserProfile;

import java.util.Map;

/**
 * 微信小程序能力：登录、手机号、订阅消息。
 */
public interface WechatMiniProgramClient {

    boolean isConfigured();

    WechatUserProfile code2Session(String code);

    /**
     * 手机号快速验证组件返回的 code → 国内纯手机号。
     */
    String getPhoneNumber(String phoneCode);

    /**
     * 发送订阅消息（失败抛业务异常由上层决定是否吞掉）。
     */
    void sendSubscribeMessage(String openId, String templateId, String page, Map<String, String> data);
}
