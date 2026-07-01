package cn.org.starpivot.mall.portal.auth.service;

import cn.org.starpivot.mall.portal.auth.domain.bo.PortalSmsSendBo;
import cn.org.starpivot.mall.portal.auth.domain.vo.PortalSmsSendVo;
import jakarta.servlet.http.HttpServletRequest;

public interface PortalSmsService {

    PortalSmsSendVo sendCode(PortalSmsSendBo bo, HttpServletRequest request);

    void verifyAndConsume(String scene, String mobile, String code);
}
