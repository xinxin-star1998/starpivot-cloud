package cn.org.starpivot.mall.portal.auth.wechat;

import cn.org.starpivot.mall.portal.auth.domain.model.WechatUserProfile;

/**
 * 微信小程序 code2session。
 */
public interface WechatMiniProgramClient {

    boolean isConfigured();

    WechatUserProfile code2Session(String code);
}
