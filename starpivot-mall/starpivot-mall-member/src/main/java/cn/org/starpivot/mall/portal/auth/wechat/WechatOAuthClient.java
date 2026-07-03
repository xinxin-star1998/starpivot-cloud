package cn.org.starpivot.mall.portal.auth.wechat;

import cn.org.starpivot.mall.portal.auth.domain.model.WechatUserProfile;

/**
 * 微信 OAuth 客户端。
 */
public interface WechatOAuthClient {

    boolean isConfigured();

    String buildAuthorizeUrl(String state, String callbackUri);

    WechatUserProfile exchangeCode(String code);
}
