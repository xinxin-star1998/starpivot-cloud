package cn.org.starpivot.mall.portal.auth.service;

import cn.org.starpivot.mall.portal.auth.domain.bo.PortalPasswordLoginBo;
import cn.org.starpivot.mall.portal.auth.domain.bo.PortalSmsLoginBo;
import cn.org.starpivot.mall.portal.auth.domain.vo.PortalAuthConfigVo;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberRegisterBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalLoginVo;
import jakarta.servlet.http.HttpServletRequest;

public interface PortalAuthService {

    PortalAuthConfigVo getConfig();

    PortalLoginVo loginByPassword(PortalPasswordLoginBo bo, HttpServletRequest request);

    PortalLoginVo loginBySms(PortalSmsLoginBo bo, HttpServletRequest request);

    PortalLoginVo registerBySms(PortalSmsLoginBo bo, HttpServletRequest request);

    void registerWithAuth(PortalMemberRegisterBo bo);
}
