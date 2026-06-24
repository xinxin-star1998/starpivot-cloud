package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.portal.domain.bo.PortalMemberLoginBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberRegisterBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalLoginVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberVo;

public interface PortalMemberService {

    void register(PortalMemberRegisterBo bo);

    PortalLoginVo login(PortalMemberLoginBo bo);

    PortalMemberVo getCurrentMember();
}
