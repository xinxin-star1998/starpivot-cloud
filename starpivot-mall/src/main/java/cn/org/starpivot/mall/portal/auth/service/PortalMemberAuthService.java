package cn.org.starpivot.mall.portal.auth.service;

import cn.org.starpivot.mall.portal.auth.PortalAuthType;
import cn.org.starpivot.mall.portal.auth.domain.vo.PortalMemberAuthVo;
import cn.org.starpivot.mall.portal.auth.domain.model.WechatUserProfile;
import cn.org.starpivot.mall.portal.auth.entity.UmsMemberAuth;
import cn.org.starpivot.mall.ums.entity.UmsMember;

import java.util.List;

public interface PortalMemberAuthService {

    UmsMemberAuth findActiveAuth(PortalAuthType type, String identifier);

    List<UmsMemberAuth> findActiveAuthsByMemberId(Long memberId);

    List<PortalMemberAuthVo> listBindingVos(Long memberId);

    void assertIdentifierAvailable(PortalAuthType type, String identifier, Long excludeMemberId);

    UmsMemberAuth insertAuth(Long memberId, PortalAuthType type, String identifier, String credential);

    UmsMemberAuth insertAuth(Long memberId, PortalAuthType type, String identifier, String credential, String extraJson);

    UmsMember bindWechat(Long memberId, WechatUserProfile profile);

    UmsMember registerByWechat(WechatUserProfile profile);

    void syncWechatProfile(UmsMember member, WechatUserProfile profile);

    void touchLastLogin(UmsMemberAuth auth);

    int countActiveAuths(Long memberId);

    void softUnbind(Long memberId, PortalAuthType type);

    void bindMobile(Long memberId, String mobile);

    void setPassword(Long memberId, String password);

    String resolveSmsMobile(Long memberId);

    UmsMember requireActiveMember(Long memberId);
}
