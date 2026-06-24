package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.JwtProperties;
import cn.org.starpivot.common.security.JwtUtils;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.common.security.SecurityUtils;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberLoginBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberRegisterBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalLoginVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberVo;
import cn.org.starpivot.mall.portal.service.PortalMemberService;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PortalMemberServiceImpl implements PortalMemberService {

    private static final int MEMBER_STATUS_NORMAL = 1;

    private final UmsMemberMapper umsMemberMapper;
    private final JwtProperties jwtProperties;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(PortalMemberRegisterBo bo) {
        long usernameCount = umsMemberMapper.selectCount(
                Wrappers.<UmsMember>lambdaQuery().eq(UmsMember::getUsername, bo.getUsername()));
        if (usernameCount > 0) {
            throw new BizException("用户名已存在");
        }
        if (StringUtils.hasText(bo.getMobile())) {
            long mobileCount = umsMemberMapper.selectCount(
                    Wrappers.<UmsMember>lambdaQuery().eq(UmsMember::getMobile, bo.getMobile()));
            if (mobileCount > 0) {
                throw new BizException("手机号已注册");
            }
        }

        UmsMember member = new UmsMember();
        member.setUsername(bo.getUsername());
        member.setPassword(SecurityUtils.encryptPassword(bo.getPassword()));
        member.setNickname(StringUtils.hasText(bo.getNickname()) ? bo.getNickname() : bo.getUsername());
        member.setMobile(bo.getMobile());
        member.setStatus(MEMBER_STATUS_NORMAL);
        member.setIntegration(0);
        member.setGrowth(0);
        member.setSourceType(0);
        member.setCreateTime(LocalDateTime.now());
        umsMemberMapper.insert(member);
    }

    @Override
    @Transactional(readOnly = true)
    public PortalLoginVo login(PortalMemberLoginBo bo) {
        UmsMember member = umsMemberMapper.selectOne(
                Wrappers.<UmsMember>lambdaQuery()
                        .and(w -> w.eq(UmsMember::getUsername, bo.getAccount())
                                .or()
                                .eq(UmsMember::getMobile, bo.getAccount())));
        if (member == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (!Integer.valueOf(MEMBER_STATUS_NORMAL).equals(member.getStatus())) {
            throw new BizException("账号已被禁用");
        }
        if (!securityUtils.matchesPassword(bo.getPassword(), member.getPassword())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }

        LoginUser loginUser = LoginUser.builder()
                .userId(member.getId())
                .username(member.getUsername())
                .roles(List.of(PortalConstants.MEMBER_ROLE))
                .build();
        String token = JwtUtils.createToken(loginUser, jwtProperties);

        PortalLoginVo vo = new PortalLoginVo();
        vo.setToken(token);
        vo.setMember(toVo(member));
        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public PortalMemberVo getCurrentMember() {
        Long memberId = PortalMemberContext.requireMemberId();
        UmsMember member = umsMemberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "会员不存在");
        }
        return toVo(member);
    }

    private PortalMemberVo toVo(UmsMember member) {
        PortalMemberVo vo = new PortalMemberVo();
        BeanUtils.copyProperties(member, vo);
        return vo;
    }
}
