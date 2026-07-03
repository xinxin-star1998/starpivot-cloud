package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberLoginBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberProfileBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalMemberRegisterBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalLoginVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberCenterVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberVo;
import cn.org.starpivot.mall.portal.service.PortalMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * C端-会员控制器。
 * <p>
 * 注册、登录、个人信息。
 * </p>
 * <ul>
 *   <li>{@link RestController} — REST 控制器，响应体自动序列化为 JSON</li>
 *   <li>{@link RequestMapping} — 基础路径 {@code /portal/member}</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入服务依赖</li>
 *   <li>{@link Validated} — 启用方法级参数校验</li>
 *   <li>{@link Tag} — OpenAPI 分组「C端-会员」</li>
 * </ul>
 *
 * @see PortalMemberService
 */

@RestController
@RequestMapping("/portal/member")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-会员", description = "注册、登录、个人信息")
public class PortalMemberController {

    private final PortalMemberService portalMemberService;

    /**
     * 会员注册。
     *
     * @param bo 业务请求参数
     * @return 操作结果
     */
    @Operation(summary = "会员注册")
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody PortalMemberRegisterBo bo) {
        portalMemberService.register(bo);
        return Result.success("注册成功");
    }

    /**
     * 会员登录。
     *
     * @param bo 业务请求参数
     * @return 业务数据
     */
    @Operation(summary = "会员登录")
    @PostMapping("/login")
    public Result<PortalLoginVo> login(@Valid @RequestBody PortalMemberLoginBo bo) {
        return Result.success(portalMemberService.login(bo));
    }

    /**
     * 当前会员信息。
     * @return 业务数据
     */
    @Operation(summary = "当前会员信息")
    @GetMapping("/info")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalMemberVo> info() {
        return Result.success(portalMemberService.getCurrentMember());
    }

    @Operation(summary = "会员中心概览")
    @GetMapping("/center")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalMemberCenterVo> center() {
        return Result.success(portalMemberService.getCenter());
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalMemberVo> updateProfile(@Valid @RequestBody PortalMemberProfileBo bo) {
        return Result.success(portalMemberService.updateProfile(bo));
    }
}
