package cn.org.starpivot.mall.portal.auth.service;

import cn.org.starpivot.mall.portal.auth.domain.bo.PortalWechatLoginBo;
import cn.org.starpivot.mall.portal.auth.domain.bo.PortalWechatMiniLoginBo;
import cn.org.starpivot.mall.portal.auth.domain.model.WechatUserProfile;
import cn.org.starpivot.mall.portal.auth.domain.vo.PortalWechatAuthorizeVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalLoginVo;
import jakarta.servlet.http.HttpServletRequest;

public interface PortalWechatAuthService {

    boolean isWechatAvailable();

    boolean isMiniProgramAvailable();

    PortalLoginVo loginByMiniProgram(PortalWechatMiniLoginBo bo, HttpServletRequest request);

    PortalWechatAuthorizeVo createAuthorizeUrl(String redirect, String mode, Long bindMemberId, String callbackUri);

    PortalLoginVo loginByWechat(PortalWechatLoginBo bo, HttpServletRequest request);

    PortalLoginVo registerByWechat(PortalWechatLoginBo bo, HttpServletRequest request);

    void bindWechat(PortalWechatLoginBo bo, Long memberId);

    WechatUserProfile resolveProfile(PortalWechatLoginBo bo);
}
