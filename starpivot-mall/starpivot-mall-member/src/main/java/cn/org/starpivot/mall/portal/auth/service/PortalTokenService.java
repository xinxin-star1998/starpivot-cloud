package cn.org.starpivot.mall.portal.auth.service;

import cn.org.starpivot.mall.portal.domain.vo.PortalLoginVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberVo;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import jakarta.servlet.http.HttpServletRequest;

public interface PortalTokenService {

    PortalLoginVo issueMemberToken(UmsMember member, int loginType, HttpServletRequest request);

    PortalMemberVo toMemberVo(UmsMember member);
}
