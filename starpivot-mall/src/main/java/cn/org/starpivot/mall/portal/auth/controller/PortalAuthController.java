package cn.org.starpivot.mall.portal.auth.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.portal.auth.domain.bo.*;
import cn.org.starpivot.mall.portal.auth.domain.vo.PortalAuthConfigVo;
import cn.org.starpivot.mall.portal.auth.domain.vo.PortalSmsSendVo;
import cn.org.starpivot.mall.portal.auth.domain.vo.PortalWechatAuthorizeVo;
import cn.org.starpivot.mall.portal.auth.service.PortalAuthService;
import cn.org.starpivot.mall.portal.auth.service.PortalSmsService;
import cn.org.starpivot.mall.portal.auth.service.PortalWechatAuthService;
import cn.org.starpivot.mall.portal.domain.vo.PortalLoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-认证", description = "登录、短信验证码")
public class PortalAuthController {

    private final PortalAuthService portalAuthService;
    private final PortalSmsService portalSmsService;
    private final PortalWechatAuthService wechatAuthService;

    @Operation(summary = "登录页能力配置")
    @GetMapping("/config")
    public Result<PortalAuthConfigVo> config() {
        return Result.success(portalAuthService.getConfig());
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms/send")
    public Result<PortalSmsSendVo> sendSms(@Valid @RequestBody PortalSmsSendBo bo, HttpServletRequest request) {
        return Result.success(portalSmsService.sendCode(bo, request));
    }

    @Operation(summary = "短信验证码登录")
    @PostMapping("/sms/login")
    public Result<PortalLoginVo> smsLogin(@Valid @RequestBody PortalSmsLoginBo bo, HttpServletRequest request) {
        return Result.success(portalAuthService.loginBySms(bo, request));
    }

    @Operation(summary = "短信验证码注册")
    @PostMapping("/sms/register")
    public Result<PortalLoginVo> smsRegister(@Valid @RequestBody PortalSmsLoginBo bo, HttpServletRequest request) {
        return Result.success(portalAuthService.registerBySms(bo, request));
    }

    @Operation(summary = "密码登录")
    @PostMapping("/login/password")
    public Result<PortalLoginVo> passwordLogin(@Valid @RequestBody PortalPasswordLoginBo bo,
                                               HttpServletRequest request) {
        return Result.success(portalAuthService.loginByPassword(bo, request));
    }

    @Operation(summary = "微信授权 URL")
    @GetMapping("/wechat/authorize")
    public Result<PortalWechatAuthorizeVo> wechatAuthorize(
            @RequestParam(required = false, defaultValue = "/portal") String redirect,
            @RequestParam(required = false, defaultValue = "login") String mode,
            @RequestParam(required = false) String callbackUri) {
        return Result.success(wechatAuthService.createAuthorizeUrl(redirect, mode, null, callbackUri));
    }

    @Operation(summary = "微信登录")
    @PostMapping("/wechat/login")
    public Result<PortalLoginVo> wechatLogin(@Valid @RequestBody PortalWechatLoginBo bo,
                                             HttpServletRequest request) {
        return Result.success(wechatAuthService.loginByWechat(bo, request));
    }

    @Operation(summary = "微信小程序登录")
    @PostMapping("/wechat/mini/login")
    public Result<PortalLoginVo> wechatMiniLogin(@Valid @RequestBody PortalWechatMiniLoginBo bo,
                                                 HttpServletRequest request) {
        return Result.success(wechatAuthService.loginByMiniProgram(bo, request));
    }

    @Operation(summary = "微信注册")
    @PostMapping("/wechat/register")
    public Result<PortalLoginVo> wechatRegister(@Valid @RequestBody PortalWechatLoginBo bo,
                                                HttpServletRequest request) {
        return Result.success(wechatAuthService.registerByWechat(bo, request));
    }
}
