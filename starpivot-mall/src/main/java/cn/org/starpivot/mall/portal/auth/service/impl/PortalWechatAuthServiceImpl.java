package cn.org.starpivot.mall.portal.auth.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.portal.auth.PortalAuthConstants;
import cn.org.starpivot.mall.portal.auth.PortalAuthType;
import cn.org.starpivot.mall.portal.auth.config.PortalAuthProperties;
import cn.org.starpivot.mall.portal.auth.domain.bo.PortalWechatLoginBo;
import cn.org.starpivot.mall.portal.auth.domain.model.PortalOAuthState;
import cn.org.starpivot.mall.portal.auth.domain.model.WechatUserProfile;
import cn.org.starpivot.mall.portal.auth.domain.vo.PortalWechatAuthorizeVo;
import cn.org.starpivot.mall.portal.auth.entity.UmsMemberAuth;
import cn.org.starpivot.mall.portal.auth.service.PortalMemberAuthService;
import cn.org.starpivot.mall.portal.auth.service.PortalTokenService;
import cn.org.starpivot.mall.portal.auth.service.PortalWechatAuthService;
import cn.org.starpivot.mall.portal.auth.wechat.WechatOAuthClient;
import cn.org.starpivot.mall.portal.domain.vo.PortalLoginVo;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortalWechatAuthServiceImpl implements PortalWechatAuthService {

    private static final Duration STATE_TTL = Duration.ofMinutes(5);

    private final PortalAuthProperties authProperties;
    private final WechatOAuthClient wechatOAuthClient;
    private final PortalMemberAuthService memberAuthService;
    private final PortalTokenService tokenService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean isWechatAvailable() {
        PortalAuthProperties.Wechat wechat = authProperties.getWechat();
        return (wechat.isEnabled() || wechat.isMockEnabled()) && wechatOAuthClient.isConfigured();
    }

    @Override
    public PortalWechatAuthorizeVo createAuthorizeUrl(String redirect, String mode, Long bindMemberId, String callbackUri) {
        if (!isWechatAvailable()) {
            throw new BizException("微信登录未启用");
        }
        String safeMode = PortalAuthConstants.OAUTH_MODE_BIND.equals(mode)
                ? PortalAuthConstants.OAUTH_MODE_BIND
                : PortalAuthConstants.OAUTH_MODE_REGISTER.equals(mode)
                ? PortalAuthConstants.OAUTH_MODE_REGISTER
                : PortalAuthConstants.OAUTH_MODE_LOGIN;
        if (PortalAuthConstants.OAUTH_MODE_BIND.equals(safeMode) && bindMemberId == null) {
            throw new BizException("请先登录后再绑定微信");
        }

        String resolvedCallback = resolveCallbackUri(callbackUri);

        String state = UUID.randomUUID().toString().replace("-", "");
        PortalOAuthState payload = new PortalOAuthState();
        payload.setMode(safeMode);
        payload.setRedirect(StringUtils.hasText(redirect) ? redirect : "/portal");
        payload.setMemberId(bindMemberId);
        saveState(state, payload);

        String authorizeUrl = wechatOAuthClient.buildAuthorizeUrl(state, resolvedCallback);
        return new PortalWechatAuthorizeVo(authorizeUrl, state);
    }

    private String resolveCallbackUri(String callbackUri) {
        String configured = authProperties.getWechat().getRedirectUri();
        if (!StringUtils.hasText(callbackUri)) {
            return configured;
        }
        if (!callbackUri.contains("/portal/login/wechat/callback")) {
            throw new BizException("无效的微信回调地址");
        }
        if (authProperties.getWechat().isMockEnabled()) {
            return callbackUri;
        }
        return configured;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalLoginVo loginByWechat(PortalWechatLoginBo bo, HttpServletRequest request) {
        PortalOAuthState oauthState = consumeState(bo.getState());
        if (!PortalAuthConstants.OAUTH_MODE_LOGIN.equals(oauthState.getMode())) {
            throw new BizException("无效的微信登录状态");
        }

        WechatUserProfile profile = wechatOAuthClient.exchangeCode(bo.getCode());
        String identifier = profile.bindingIdentifier();

        UmsMemberAuth existing = memberAuthService.findActiveAuth(PortalAuthType.WECHAT, identifier);
        if (existing != null) {
            UmsMember member = memberAuthService.requireActiveMember(existing.getMemberId());
            memberAuthService.syncWechatProfile(member, profile);
            memberAuthService.touchLastLogin(existing);
            return tokenService.issueMemberToken(member, PortalAuthConstants.LOGIN_TYPE_WECHAT, request);
        }

        throw new BizException("该微信未注册，请先注册");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalLoginVo registerByWechat(PortalWechatLoginBo bo, HttpServletRequest request) {
        PortalOAuthState oauthState = consumeState(bo.getState());
        if (!PortalAuthConstants.OAUTH_MODE_REGISTER.equals(oauthState.getMode())) {
            throw new BizException("无效的微信注册状态");
        }

        WechatUserProfile profile = wechatOAuthClient.exchangeCode(bo.getCode());
        String identifier = profile.bindingIdentifier();

        UmsMemberAuth existing = memberAuthService.findActiveAuth(PortalAuthType.WECHAT, identifier);
        if (existing != null) {
            UmsMember member = memberAuthService.requireActiveMember(existing.getMemberId());
            memberAuthService.syncWechatProfile(member, profile);
            memberAuthService.touchLastLogin(existing);
            return tokenService.issueMemberToken(member, PortalAuthConstants.LOGIN_TYPE_WECHAT, request);
        }

        if (!authProperties.getWechat().isAutoRegister()) {
            throw new BizException("微信注册未启用");
        }

        UmsMember member = memberAuthService.registerByWechat(profile);
        return tokenService.issueMemberToken(member, PortalAuthConstants.LOGIN_TYPE_AUTO_REGISTER, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindWechat(PortalWechatLoginBo bo, Long memberId) {
        PortalOAuthState oauthState = consumeState(bo.getState());
        if (!PortalAuthConstants.OAUTH_MODE_BIND.equals(oauthState.getMode())) {
            throw new BizException("无效的微信绑定状态");
        }
        if (oauthState.getMemberId() != null && !oauthState.getMemberId().equals(memberId)) {
            throw new BizException("绑定状态与当前会员不一致");
        }

        WechatUserProfile profile = wechatOAuthClient.exchangeCode(bo.getCode());
        memberAuthService.bindWechat(memberId, profile);
    }

    @Override
    public WechatUserProfile resolveProfile(PortalWechatLoginBo bo) {
        consumeState(bo.getState());
        return wechatOAuthClient.exchangeCode(bo.getCode());
    }

    private void saveState(String state, PortalOAuthState payload) {
        try {
            stringRedisTemplate.opsForValue().set(
                    PortalAuthConstants.oauthStateKey(state),
                    objectMapper.writeValueAsString(payload),
                    STATE_TTL);
        } catch (JsonProcessingException e) {
            throw new BizException("创建微信授权状态失败");
        }
    }

    private PortalOAuthState consumeState(String state) {
        if (!StringUtils.hasText(state)) {
            throw new BizException("state 无效");
        }
        String key = PortalAuthConstants.oauthStateKey(state);
        String json = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(json)) {
            throw new BizException("微信授权已过期，请重新发起");
        }
        stringRedisTemplate.delete(key);
        try {
            return objectMapper.readValue(json, PortalOAuthState.class);
        } catch (JsonProcessingException e) {
            throw new BizException("微信授权状态无效");
        }
    }
}
