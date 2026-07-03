package cn.org.starpivot.mall.portal.auth.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.auth.PortalAuthConstants;
import cn.org.starpivot.mall.portal.auth.PortalAuthType;
import cn.org.starpivot.mall.portal.auth.domain.bo.PortalBindMobileBo;
import cn.org.starpivot.mall.portal.auth.domain.bo.PortalSetPasswordBo;
import cn.org.starpivot.mall.portal.auth.domain.bo.PortalUnbindAuthBo;
import cn.org.starpivot.mall.portal.auth.domain.bo.PortalWechatLoginBo;
import cn.org.starpivot.mall.portal.auth.domain.vo.PortalMemberAuthVo;
import cn.org.starpivot.mall.portal.auth.domain.vo.PortalWechatAuthorizeVo;
import cn.org.starpivot.mall.portal.auth.service.PortalMemberAuthService;
import cn.org.starpivot.mall.portal.auth.service.PortalSmsService;
import cn.org.starpivot.mall.portal.auth.service.PortalWechatAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal/member/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-账号绑定", description = "登录方式绑定管理")
public class PortalMemberAuthController {

    private final PortalMemberAuthService memberAuthService;
    private final PortalSmsService smsService;
    private final PortalWechatAuthService wechatAuthService;

    @Operation(summary = "已绑定登录方式列表")
    @GetMapping("/bindings")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<List<PortalMemberAuthVo>> bindings() {
        Long memberId = PortalMemberContext.requireMemberId();
        return Result.success(memberAuthService.listBindingVos(memberId));
    }

    @Operation(summary = "绑定手机号")
    @PostMapping("/bind/mobile")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> bindMobile(@Valid @RequestBody PortalBindMobileBo bo) {
        Long memberId = PortalMemberContext.requireMemberId();
        smsService.verifyAndConsume(PortalAuthConstants.SMS_SCENE_BIND, bo.getMobile(), bo.getCode());
        memberAuthService.bindMobile(memberId, bo.getMobile());
        return Result.success("绑定成功");
    }

    @Operation(summary = "设置登录密码")
    @PostMapping("/set-password")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> setPassword(@Valid @RequestBody PortalSetPasswordBo bo) {
        Long memberId = PortalMemberContext.requireMemberId();
        String mobile = memberAuthService.resolveSmsMobile(memberId);
        smsService.verifyAndConsume(PortalAuthConstants.SMS_SCENE_SET_PASSWORD, mobile, bo.getCode());
        memberAuthService.setPassword(memberId, bo.getPassword());
        return Result.success("密码设置成功");
    }

    @Operation(summary = "绑定微信")
    @PostMapping("/bind/wechat")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> bindWechat(@Valid @RequestBody PortalWechatLoginBo bo) {
        Long memberId = PortalMemberContext.requireMemberId();
        wechatAuthService.bindWechat(bo, memberId);
        return Result.success("绑定成功");
    }

    @Operation(summary = "微信绑定授权 URL")
    @GetMapping("/bind/wechat/authorize")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalWechatAuthorizeVo> wechatBindAuthorize(
            @RequestParam(required = false, defaultValue = "/portal/account/security") String redirect,
            @RequestParam(required = false) String callbackUri) {
        Long memberId = PortalMemberContext.requireMemberId();
        return Result.success(
                wechatAuthService.createAuthorizeUrl(
                        redirect, PortalAuthConstants.OAUTH_MODE_BIND, memberId, callbackUri));
    }

    @Operation(summary = "解绑登录方式")
    @DeleteMapping("/unbind/{authType}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> unbind(@PathVariable Integer authType,
                          @RequestBody(required = false) PortalUnbindAuthBo bo) {
        PortalAuthType type = PortalAuthType.fromCode(authType);
        Long memberId = PortalMemberContext.requireMemberId();

        if (type == PortalAuthType.MOBILE) {
            if (bo == null || !StringUtils.hasText(bo.getCode())) {
                throw new BizException("请输入验证码");
            }
            String mobile = memberAuthService.resolveSmsMobile(memberId);
            smsService.verifyAndConsume(PortalAuthConstants.SMS_SCENE_UNBIND, mobile, bo.getCode());
        } else if (type == PortalAuthType.WECHAT) {
            // 第三方解绑无需短信，保留至少一种方式即可
        } else {
            throw new BizException("当前不支持解绑该登录方式");
        }

        memberAuthService.softUnbind(memberId, type);
        return Result.success("解绑成功");
    }
}
