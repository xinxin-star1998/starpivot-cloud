package cn.org.starpivot.mall.portal.auth.domain.model;

import lombok.Data;

/**
 * OAuth state 缓存载荷。
 */
@Data
public class PortalOAuthState {

    /** login | bind */
    private String mode;

    /** 登录/绑定完成后前端跳转路径 */
    private String redirect;

    /** bind 模式下的会员 ID */
    private Long memberId;
}
