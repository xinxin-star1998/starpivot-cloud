package cn.org.starpivot.mall.portal.auth.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.JwtProperties;
import cn.org.starpivot.common.security.JwtUtils;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.domain.vo.PortalLoginVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberVo;
import cn.org.starpivot.mall.portal.auth.service.PortalMemberLoginLogService;
import cn.org.starpivot.mall.portal.auth.service.PortalTokenService;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortalTokenServiceImpl implements PortalTokenService {

    private final JwtProperties jwtProperties;
    private final PortalMemberLoginLogService memberLoginLogService;

    @Override
    public PortalLoginVo issueMemberToken(UmsMember member, int loginType, HttpServletRequest request) {
        LoginUser loginUser = LoginUser.builder()
                .userId(member.getId())
                .username(member.getUsername())
                .roles(List.of(PortalConstants.MEMBER_ROLE))
                .build();
        String token = JwtUtils.createToken(loginUser, jwtProperties);

        memberLoginLogService.record(member.getId(), loginType, request);

        PortalLoginVo vo = new PortalLoginVo();
        vo.setToken(token);
        vo.setMember(toMemberVo(member));
        return vo;
    }

    @Override
    public PortalMemberVo toMemberVo(UmsMember member) {
        PortalMemberVo vo = new PortalMemberVo();
        BeanUtils.copyProperties(member, vo);
        return vo;
    }

}
