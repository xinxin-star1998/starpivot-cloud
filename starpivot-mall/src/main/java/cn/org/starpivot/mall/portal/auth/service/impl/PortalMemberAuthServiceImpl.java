package cn.org.starpivot.mall.portal.auth.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityUtils;
import cn.org.starpivot.mall.portal.auth.PortalAuthConstants;
import cn.org.starpivot.mall.portal.auth.PortalAuthType;
import cn.org.starpivot.mall.portal.auth.domain.model.WechatUserProfile;
import cn.org.starpivot.mall.portal.auth.domain.vo.PortalMemberAuthVo;
import cn.org.starpivot.mall.portal.auth.entity.UmsMemberAuth;
import cn.org.starpivot.mall.portal.auth.mapper.UmsMemberAuthMapper;
import cn.org.starpivot.mall.portal.auth.service.PortalMemberAuthService;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalMemberAuthServiceImpl implements PortalMemberAuthService {

    private static final int MEMBER_STATUS_NORMAL = 1;

    private final UmsMemberAuthMapper authMapper;
    private final UmsMemberMapper memberMapper;
    private final SecurityUtils securityUtils;

    @Override
    public UmsMemberAuth findActiveAuth(PortalAuthType type, String identifier) {
        return authMapper.selectOne(Wrappers.<UmsMemberAuth>lambdaQuery()
                .eq(UmsMemberAuth::getAuthType, type.getCode())
                .eq(UmsMemberAuth::getIdentifier, identifier)
                .eq(UmsMemberAuth::getStatus, PortalAuthConstants.AUTH_STATUS_ACTIVE));
    }

    @Override
    public List<UmsMemberAuth> findActiveAuthsByMemberId(Long memberId) {
        return authMapper.selectList(Wrappers.<UmsMemberAuth>lambdaQuery()
                .eq(UmsMemberAuth::getMemberId, memberId)
                .eq(UmsMemberAuth::getStatus, PortalAuthConstants.AUTH_STATUS_ACTIVE)
                .orderByAsc(UmsMemberAuth::getAuthType));
    }

    @Override
    public List<PortalMemberAuthVo> listBindingVos(Long memberId) {
        return findActiveAuthsByMemberId(memberId).stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    @Override
    public void assertIdentifierAvailable(PortalAuthType type, String identifier, Long excludeMemberId) {
        UmsMemberAuth existing = findActiveAuth(type, identifier);
        if (existing != null && (excludeMemberId == null || !existing.getMemberId().equals(excludeMemberId))) {
            throw new BizException("该" + type.getLabel() + "已绑定其他账号");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UmsMemberAuth insertAuth(Long memberId, PortalAuthType type, String identifier, String credential) {
        return insertAuth(memberId, type, identifier, credential, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UmsMemberAuth insertAuth(Long memberId, PortalAuthType type, String identifier, String credential,
                                    String extraJson) {
        assertIdentifierAvailable(type, identifier, memberId);
        LocalDateTime now = LocalDateTime.now();
        UmsMemberAuth auth = new UmsMemberAuth();
        auth.setMemberId(memberId);
        auth.setAuthType(type.getCode());
        auth.setIdentifier(identifier);
        auth.setCredential(credential);
        auth.setExtraJson(extraJson);
        auth.setBindTime(now);
        auth.setStatus(PortalAuthConstants.AUTH_STATUS_ACTIVE);
        auth.setCreateTime(now);
        auth.setUpdateTime(now);
        authMapper.insert(auth);
        return auth;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UmsMember bindWechat(Long memberId, WechatUserProfile profile) {
        UmsMember member = requireActiveMember(memberId);
        String identifier = profile.bindingIdentifier();
        insertAuth(memberId, PortalAuthType.WECHAT, identifier, null, buildWechatExtraJson(profile));
        syncWechatProfile(member, profile);
        return member;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UmsMember registerByWechat(WechatUserProfile profile) {
        String identifier = profile.bindingIdentifier();
        assertIdentifierAvailable(PortalAuthType.WECHAT, identifier, null);

        UmsMember member = new UmsMember();
        member.setUsername(generateWechatUsername());
        member.setPassword(securityUtils.encryptPassword(UUID.randomUUID().toString()));
        member.setNickname(StringUtils.hasText(profile.getNickname()) ? profile.getNickname() : "微信用户");
        member.setHeader(profile.getAvatar());
        member.setStatus(MEMBER_STATUS_NORMAL);
        member.setIntegration(0);
        member.setGrowth(0);
        member.setSourceType(PortalAuthConstants.SOURCE_TYPE_WECHAT);
        member.setSocialUid(identifier);
        member.setCreateTime(LocalDateTime.now());
        memberMapper.insert(member);

        insertAuth(member.getId(), PortalAuthType.WECHAT, identifier, null, buildWechatExtraJson(profile));
        return member;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncWechatProfile(UmsMember member, WechatUserProfile profile) {
        if (StringUtils.hasText(profile.getNickname())) {
            member.setNickname(profile.getNickname());
        }
        if (StringUtils.hasText(profile.getAvatar())) {
            member.setHeader(profile.getAvatar());
        }
        member.setSocialUid(profile.bindingIdentifier());
        memberMapper.updateById(member);
    }

    private static String buildWechatExtraJson(WechatUserProfile profile) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"unionid\":\"").append(escapeJson(profile.getUnionId())).append("\",");
        sb.append("\"openid\":\"").append(escapeJson(profile.getOpenId())).append("\",");
        sb.append("\"nickname\":\"").append(escapeJson(profile.getNickname())).append("\",");
        sb.append("\"avatar\":\"").append(escapeJson(profile.getAvatar())).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String generateWechatUsername() {
        return "wx_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void touchLastLogin(UmsMemberAuth auth) {
        auth.setLastLogin(LocalDateTime.now());
        auth.setUpdateTime(LocalDateTime.now());
        authMapper.updateById(auth);
    }

    @Override
    public int countActiveAuths(Long memberId) {
        return Math.toIntExact(authMapper.selectCount(Wrappers.<UmsMemberAuth>lambdaQuery()
                .eq(UmsMemberAuth::getMemberId, memberId)
                .eq(UmsMemberAuth::getStatus, PortalAuthConstants.AUTH_STATUS_ACTIVE)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void softUnbind(Long memberId, PortalAuthType type) {
        if (countActiveAuths(memberId) <= 1) {
            throw new BizException("至少保留一种登录方式");
        }
        UmsMemberAuth auth = authMapper.selectOne(Wrappers.<UmsMemberAuth>lambdaQuery()
                .eq(UmsMemberAuth::getMemberId, memberId)
                .eq(UmsMemberAuth::getAuthType, type.getCode())
                .eq(UmsMemberAuth::getStatus, PortalAuthConstants.AUTH_STATUS_ACTIVE));
        if (auth == null) {
            throw new BizException("未绑定该登录方式");
        }
        auth.setStatus(PortalAuthConstants.AUTH_STATUS_UNBOUND);
        auth.setUpdateTime(LocalDateTime.now());
        authMapper.updateById(auth);

        if (type == PortalAuthType.MOBILE) {
            UmsMember member = memberMapper.selectById(memberId);
            if (member != null && auth.getIdentifier().equals(member.getMobile())) {
                member.setMobile(null);
                memberMapper.updateById(member);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindMobile(Long memberId, String mobile) {
        assertIdentifierAvailable(PortalAuthType.MOBILE, mobile, memberId);
        insertAuth(memberId, PortalAuthType.MOBILE, mobile, null);
        UmsMember member = requireActiveMember(memberId);
        member.setMobile(mobile);
        memberMapper.updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPassword(Long memberId, String plainPassword) {
        UmsMember member = requireActiveMember(memberId);
        String encrypted = securityUtils.encryptPassword(plainPassword);
        member.setPassword(encrypted);
        memberMapper.updateById(member);

        UmsMemberAuth passwordAuth = authMapper.selectOne(Wrappers.<UmsMemberAuth>lambdaQuery()
                .eq(UmsMemberAuth::getMemberId, memberId)
                .eq(UmsMemberAuth::getAuthType, PortalAuthType.PASSWORD.getCode())
                .eq(UmsMemberAuth::getStatus, PortalAuthConstants.AUTH_STATUS_ACTIVE));
        if (passwordAuth == null) {
            insertAuth(memberId, PortalAuthType.PASSWORD, member.getUsername(), encrypted);
        } else {
            passwordAuth.setCredential(encrypted);
            passwordAuth.setUpdateTime(LocalDateTime.now());
            authMapper.updateById(passwordAuth);
        }
    }

    @Override
    public String resolveSmsMobile(Long memberId) {
        List<UmsMemberAuth> auths = findActiveAuthsByMemberId(memberId);
        for (UmsMemberAuth auth : auths) {
            if (PortalAuthType.MOBILE.getCode() == auth.getAuthType()) {
                return auth.getIdentifier();
            }
        }
        UmsMember member = memberMapper.selectById(memberId);
        if (member != null && StringUtils.hasText(member.getMobile())) {
            return member.getMobile();
        }
        throw new BizException("请先绑定手机号");
    }

    @Override
    public UmsMember requireActiveMember(Long memberId) {
        UmsMember member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "会员不存在");
        }
        if (!Integer.valueOf(MEMBER_STATUS_NORMAL).equals(member.getStatus())) {
            throw new BizException("账号已被禁用");
        }
        return member;
    }

    @Override
    public String resolveWechatOpenId(Long memberId) {
        List<UmsMemberAuth> auths = findActiveAuthsByMemberId(memberId);
        for (UmsMemberAuth auth : auths) {
            if (PortalAuthType.WECHAT.getCode() != auth.getAuthType()) {
                continue;
            }
            String openId = parseOpenIdFromExtra(auth.getExtraJson());
            if (StringUtils.hasText(openId)) {
                return openId;
            }
        }
        throw new BizException("请先使用微信登录后再支付");
    }

    private static String parseOpenIdFromExtra(String extraJson) {
        if (!StringUtils.hasText(extraJson)) {
            return null;
        }
        int idx = extraJson.indexOf("\"openid\":\"");
        if (idx < 0) {
            return null;
        }
        int start = idx + "\"openid\":\"".length();
        int end = extraJson.indexOf('"', start);
        if (end <= start) {
            return null;
        }
        return extraJson.substring(start, end);
    }

    private PortalMemberAuthVo toVo(UmsMemberAuth auth) {
        PortalAuthType type = PortalAuthType.fromCode(auth.getAuthType());
        PortalMemberAuthVo vo = new PortalMemberAuthVo();
        vo.setAuthType(type.getCode());
        vo.setAuthTypeLabel(type.getLabel());
        vo.setIdentifier(auth.getIdentifier());
        vo.setMaskedIdentifier(maskIdentifier(type, auth.getIdentifier()));
        vo.setBindTime(auth.getBindTime());
        return vo;
    }

    static String maskIdentifier(PortalAuthType type, String identifier) {
        if (!StringUtils.hasText(identifier)) {
            return "";
        }
        if (type == PortalAuthType.MOBILE && identifier.length() >= 11) {
            return identifier.substring(0, 3) + "****" + identifier.substring(7);
        }
        if (identifier.length() <= 3) {
            return identifier.charAt(0) + "***";
        }
        return identifier.substring(0, Math.min(3, identifier.length())) + "***";
    }
}
