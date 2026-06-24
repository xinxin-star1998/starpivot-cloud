package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberLoginBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberRegisterBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalLoginVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberVo;
import cn.org.starpivot.mall.portal.service.PortalMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/member")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-会员", description = "注册、登录、个人信息")
public class PortalMemberController {

    private final PortalMemberService portalMemberService;

    @Operation(summary = "会员注册")
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody PortalMemberRegisterBo bo) {
        portalMemberService.register(bo);
        return Result.success("注册成功");
    }

    @Operation(summary = "会员登录")
    @PostMapping("/login")
    public Result<PortalLoginVo> login(@Valid @RequestBody PortalMemberLoginBo bo) {
        return Result.success(portalMemberService.login(bo));
    }

    @Operation(summary = "当前会员信息")
    @GetMapping("/info")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalMemberVo> info() {
        return Result.success(portalMemberService.getCurrentMember());
    }
}
