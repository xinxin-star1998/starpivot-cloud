package cn.org.starpivot.mall.portal.auth.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * 微信 OAuth 用户资料（unionid 优先作为绑定标识）。
 */
@Data
@Builder
public class WechatUserProfile {

    private String unionId;
    private String openId;
    private String nickname;
    private String avatar;
    private String appId;

    public String bindingIdentifier() {
        if (unionId != null && !unionId.isBlank()) {
            return unionId;
        }
        return openId;
    }
}
