package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.portal.domain.bo.PortalMemberLoginBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberProfileBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberRegisterBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalLoginVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberCenterVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberVo;

/**
 * Memberservice服务接口。
 * <p>
 * 封装会员相关业务逻辑。
 * </p>
 */

public interface PortalMemberService {

    /**
     * 会员注册。
     */
    void register(PortalMemberRegisterBo bo);

    /**
     * 会员登录。
     */
    PortalLoginVo login(PortalMemberLoginBo bo);

    /**
     * 获取CurrentMember。
     */
    PortalMemberVo getCurrentMember();

    /**
     * 会员中心概览。
     */
    PortalMemberCenterVo getCenter();

    /**
     * 更新个人资料。
     */
    PortalMemberVo updateProfile(PortalMemberProfileBo bo);
}
