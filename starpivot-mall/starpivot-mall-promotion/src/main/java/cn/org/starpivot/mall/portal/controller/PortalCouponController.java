package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.domain.bo.PortalCouponTrialBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCheckoutCouponVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalClaimableCouponVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMemberCouponVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalMyCouponVo;
import cn.org.starpivot.mall.portal.service.PortalCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal/coupon")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-优惠券", description = "领取、我的优惠券、结算试算")
public class PortalCouponController {

    private final PortalCouponService portalCouponService;

    @GetMapping("/claimable")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    @Operation(summary = "可领取优惠券列表")
    public Result<List<PortalClaimableCouponVo>> listClaimable() {
        Long memberId = PortalMemberContext.requireMemberId();
        return Result.success(portalCouponService.listClaimable(memberId));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    @Operation(summary = "我的优惠券")
    public Result<List<PortalMyCouponVo>> listMine(@RequestParam(required = false) Integer status) {
        Long memberId = PortalMemberContext.requireMemberId();
        return Result.success(portalCouponService.listMine(memberId, status));
    }

    @PostMapping("/receive/{couponId}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    @Operation(summary = "领取优惠券")
    public Result<Long> receive(@PathVariable Long couponId) {
        Long memberId = PortalMemberContext.requireMemberId();
        return Result.success(portalCouponService.receive(memberId, couponId));
    }

    @PostMapping("/usable")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    @Operation(summary = "查询当前订单可用优惠券")
    public Result<List<PortalMemberCouponVo>> listUsable(@Valid @RequestBody PortalCouponTrialBo bo) {
        Long memberId = PortalMemberContext.requireMemberId();
        return Result.success(portalCouponService.listUsable(memberId, bo));
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    @Operation(summary = "结算页优惠券列表（含不可用原因）")
    public Result<List<PortalCheckoutCouponVo>> listCheckoutCoupons(@Valid @RequestBody PortalCouponTrialBo bo) {
        Long memberId = PortalMemberContext.requireMemberId();
        return Result.success(portalCouponService.listCheckoutCoupons(memberId, bo));
    }
}
