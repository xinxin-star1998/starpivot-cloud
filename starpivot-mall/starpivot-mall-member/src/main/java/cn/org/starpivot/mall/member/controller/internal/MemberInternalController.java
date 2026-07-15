package cn.org.starpivot.mall.member.controller.internal;

import cn.org.starpivot.api.member.dto.*;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.member.internal.MemberIntegrationInternalService;
import cn.org.starpivot.mall.member.internal.MemberInternalService;
import cn.org.starpivot.mall.member.internal.MemberRewardInternalService;
import cn.org.starpivot.mall.portal.auth.service.PortalMemberAuthService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
@RequestMapping("/internal/member")
@RequiredArgsConstructor
public class MemberInternalController {

    private final MemberInternalService memberInternalService;
    private final PortalMemberAuthService portalMemberAuthService;
    private final MemberIntegrationInternalService memberIntegrationInternalService;
    private final MemberRewardInternalService memberRewardInternalService;

    @GetMapping("/{memberId}")
    public Result<MemberDto> getMember(@PathVariable("memberId") Long memberId) {
        MemberDto dto = memberInternalService.getMember(memberId);
        if (dto == null) {
            return Result.notFound("会员不存在");
        }
        return Result.success(dto);
    }

    @GetMapping("/{memberId}/addresses/{addressId}")
    public Result<MemberAddressDto> getAddress(
            @PathVariable("memberId") Long memberId,
            @PathVariable("addressId") Long addressId) {
        MemberAddressDto dto = memberInternalService.getAddress(memberId, addressId);
        if (dto == null) {
            return Result.notFound("收货地址不存在");
        }
        return Result.success(dto);
    }

    @GetMapping("/levels/{levelId}")
    public Result<MemberLevelDto> getMemberLevel(@PathVariable("levelId") Long levelId) {
        MemberLevelDto dto = memberInternalService.getMemberLevel(levelId);
        if (dto == null) {
            return Result.notFound("会员等级不存在");
        }
        return Result.success(dto);
    }

    @GetMapping("/{memberId}/wechat-open-id")
    public Result<String> resolveWechatOpenId(@PathVariable("memberId") Long memberId) {
        return Result.success(portalMemberAuthService.resolveWechatOpenId(memberId));
    }

    @PostMapping("/integration/deduct-for-order")
    public Result<Void> deductIntegrationForOrder(@Valid @RequestBody MemberOrderIntegrationRequest request) {
        memberIntegrationInternalService.deductForOrder(request);
        return Result.success();
    }

    @PostMapping("/integration/restore-for-order")
    public Result<Void> restoreIntegrationForOrder(@Valid @RequestBody MemberOrderIntegrationRequest request) {
        memberIntegrationInternalService.restoreForOrder(request);
        return Result.success();
    }

    @PostMapping("/reward/grant-on-paid")
    public Result<Void> grantRewardOnPaid(@Valid @RequestBody MemberOrderRewardRequest request) {
        memberRewardInternalService.grantOnPaid(request);
        return Result.success();
    }

    @PostMapping("/reward/clawback-on-return")
    public Result<Void> clawbackRewardOnReturn(@Valid @RequestBody MemberOrderReturnRewardRequest request) {
        memberRewardInternalService.clawbackOnReturn(request);
        return Result.success();
    }
}
