package cn.org.starpivot.mall.portal.auth.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityUtils;
import cn.org.starpivot.mall.portal.auth.PortalAuthConstants;
import cn.org.starpivot.mall.portal.auth.PortalAuthType;
import cn.org.starpivot.mall.portal.auth.config.PortalAuthProperties;
import cn.org.starpivot.mall.portal.auth.domain.bo.PortalPasswordLoginBo;
import cn.org.starpivot.mall.portal.auth.domain.bo.PortalSmsLoginBo;
import cn.org.starpivot.mall.portal.auth.domain.vo.PortalAuthConfigVo;
import cn.org.starpivot.mall.portal.auth.entity.UmsMemberAuth;
import cn.org.starpivot.mall.portal.auth.mapper.UmsMemberAuthMapper;
import cn.org.starpivot.mall.portal.auth.service.*;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberRegisterBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalLoginVo;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PortalAuthServiceImpl implements PortalAuthService {

    private static final int MEMBER_STATUS_NORMAL = 1;
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private final PortalAuthProperties authProperties;
    private final PortalMemberAuthService memberAuthService;
    private final PortalSmsService smsService;
    private final PortalTokenService tokenService;
    private final PortalWechatAuthService wechatAuthService;
    private final UmsMemberMapper memberMapper;
    private final UmsMemberAuthMapper authMapper;
    private final SecurityUtils securityUtils;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public PortalAuthConfigVo getConfig() {
        PortalAuthConfigVo vo = new PortalAuthConfigVo();
        vo.setPasswordLogin(true);
        vo.setSmsLogin(true);
        vo.setWechatLogin(wechatAuthService.isWechatAvailable());
        vo.setMiniProgramLogin(wechatAuthService.isMiniProgramAvailable());
        vo.setQqLogin(false);
        vo.setSmsMockEnabled(authProperties.getSms().isMockEnabled());
        vo.setCaptchaRequired(false);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalLoginVo loginByPassword(PortalPasswordLoginBo bo, HttpServletRequest request) {
        assertPasswordNotLocked(bo.getAccount());
        UmsMember member = resolveMemberForPassword(bo.getAccount());
        if (member == null) {
            recordPasswordFail(bo.getAccount());
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (!verifyPassword(member, bo.getPassword())) {
            recordPasswordFail(bo.getAccount());
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        clearPasswordFail(bo.getAccount());
        return tokenService.issueMemberToken(member, PortalAuthConstants.LOGIN_TYPE_PASSWORD, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalLoginVo loginBySms(PortalSmsLoginBo bo, HttpServletRequest request) {
        smsService.verifyAndConsume(PortalAuthConstants.SMS_SCENE_LOGIN, bo.getMobile(), bo.getCode());

        UmsMemberAuth mobileAuth = memberAuthService.findActiveAuth(PortalAuthType.MOBILE, bo.getMobile());
        if (mobileAuth == null) {
            throw new BizException("该手机号未注册，请先注册");
        }
        UmsMember member = memberAuthService.requireActiveMember(mobileAuth.getMemberId());
        memberAuthService.touchLastLogin(mobileAuth);
        return tokenService.issueMemberToken(member, PortalAuthConstants.LOGIN_TYPE_SMS, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalLoginVo registerBySms(PortalSmsLoginBo bo, HttpServletRequest request) {
        smsService.verifyAndConsume(PortalAuthConstants.SMS_SCENE_REGISTER, bo.getMobile(), bo.getCode());

        UmsMemberAuth mobileAuth = memberAuthService.findActiveAuth(PortalAuthType.MOBILE, bo.getMobile());
        if (mobileAuth != null) {
            UmsMember member = memberAuthService.requireActiveMember(mobileAuth.getMemberId());
            memberAuthService.touchLastLogin(mobileAuth);
            return tokenService.issueMemberToken(member, PortalAuthConstants.LOGIN_TYPE_SMS, request);
        }

        UmsMember member = registerByMobile(bo.getMobile());
        return tokenService.issueMemberToken(member, PortalAuthConstants.LOGIN_TYPE_AUTO_REGISTER, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerWithAuth(PortalMemberRegisterBo bo) {
        long usernameCount = memberMapper.selectCount(
                Wrappers.<UmsMember>lambdaQuery().eq(UmsMember::getUsername, bo.getUsername()));
        if (usernameCount > 0) {
            throw new BizException("用户名已存在");
        }
        if (StringUtils.hasText(bo.getMobile())) {
            memberAuthService.assertIdentifierAvailable(PortalAuthType.MOBILE, bo.getMobile(), null);
        }

        String encrypted = securityUtils.encryptPassword(bo.getPassword());
        UmsMember member = new UmsMember();
        member.setUsername(bo.getUsername());
        member.setPassword(encrypted);
        member.setNickname(StringUtils.hasText(bo.getNickname()) ? bo.getNickname() : bo.getUsername());
        member.setMobile(bo.getMobile());
        member.setStatus(MEMBER_STATUS_NORMAL);
        member.setIntegration(0);
        member.setGrowth(0);
        member.setSourceType(0);
        member.setCreateTime(LocalDateTime.now());
        memberMapper.insert(member);

        memberAuthService.insertAuth(member.getId(), PortalAuthType.PASSWORD, bo.getUsername(), encrypted);
        if (StringUtils.hasText(bo.getMobile())) {
            memberAuthService.insertAuth(member.getId(), PortalAuthType.MOBILE, bo.getMobile(), null);
        }
    }

    private UmsMember registerByMobile(String mobile) {
        UmsMember member = new UmsMember();
        member.setUsername(generateUsername(mobile));
        member.setPassword(securityUtils.encryptPassword(UUID.randomUUID().toString()));
        member.setNickname("用户" + mobile.substring(mobile.length() - 4));
        member.setMobile(mobile);
        member.setStatus(MEMBER_STATUS_NORMAL);
        member.setIntegration(0);
        member.setGrowth(0);
        member.setSourceType(PortalAuthConstants.SOURCE_TYPE_MOBILE);
        member.setCreateTime(LocalDateTime.now());
        memberMapper.insert(member);
        memberAuthService.insertAuth(member.getId(), PortalAuthType.MOBILE, mobile, null);
        return member;
    }

    private UmsMember resolveMemberForPassword(String account) {
        if (MOBILE_PATTERN.matcher(account).matches()) {
            UmsMemberAuth mobileAuth = memberAuthService.findActiveAuth(PortalAuthType.MOBILE, account);
            if (mobileAuth != null) {
                return memberMapper.selectById(mobileAuth.getMemberId());
            }
        }

        UmsMemberAuth passwordAuth = memberAuthService.findActiveAuth(PortalAuthType.PASSWORD, account);
        if (passwordAuth != null) {
            return memberMapper.selectById(passwordAuth.getMemberId());
        }

        UmsMember legacy = memberMapper.selectOne(Wrappers.<UmsMember>lambdaQuery()
                .and(w -> w.eq(UmsMember::getUsername, account).or().eq(UmsMember::getMobile, account)));
        if (legacy == null || !Integer.valueOf(MEMBER_STATUS_NORMAL).equals(legacy.getStatus())) {
            return null;
        }
        return legacy;
    }

    private boolean verifyPassword(UmsMember member, String plainPassword) {
        UmsMemberAuth passwordAuth = authMapper.selectOne(Wrappers.<UmsMemberAuth>lambdaQuery()
                .eq(UmsMemberAuth::getMemberId, member.getId())
                .eq(UmsMemberAuth::getAuthType, PortalAuthType.PASSWORD.getCode())
                .eq(UmsMemberAuth::getStatus, PortalAuthConstants.AUTH_STATUS_ACTIVE));
        if (passwordAuth != null && StringUtils.hasText(passwordAuth.getCredential())) {
            return securityUtils.matchesPassword(plainPassword, passwordAuth.getCredential());
        }
        return StringUtils.hasText(member.getPassword())
                && securityUtils.matchesPassword(plainPassword, member.getPassword());
    }

    private void recordPasswordFail(String account) {
        String key = PortalAuthConstants.loginFailKey(account);
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, Duration.ofMinutes(authProperties.getPassword().getLockMinutes()));
        }
        if (count != null && count >= authProperties.getPassword().getMaxFailCount()) {
            throw new BizException(ErrorCode.UNAUTHORIZED,
                    "登录失败次数过多，请" + authProperties.getPassword().getLockMinutes() + "分钟后再试");
        }
    }

    private void assertPasswordNotLocked(String account) {
        String key = PortalAuthConstants.loginFailKey(account);
        String countStr = stringRedisTemplate.opsForValue().get(key);
        if (countStr == null) {
            return;
        }
        try {
            int count = Integer.parseInt(countStr);
            if (count >= authProperties.getPassword().getMaxFailCount()) {
                throw new BizException(ErrorCode.UNAUTHORIZED,
                        "登录失败次数过多，请" + authProperties.getPassword().getLockMinutes() + "分钟后再试");
            }
        } catch (NumberFormatException ignored) {
            // Redis 计数异常时继续尝试登录
        }
    }

    private void clearPasswordFail(String account) {
        stringRedisTemplate.delete(PortalAuthConstants.loginFailKey(account));
    }

    private static String generateUsername(String mobile) {
        String suffix = mobile.substring(mobile.length() - 4);
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return "m_" + suffix + "_" + random;
    }
}
