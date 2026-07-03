package cn.org.starpivot.api.member;

import cn.org.starpivot.api.fallback.MemberInternalClientFallbackFactory;
import cn.org.starpivot.api.member.dto.*;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 会员服务内部 Feign 客户端（供 starpivot-mall 等调用）。
 */
@FeignClient(
        name = "starpivot-mall-member",
        contextId = "memberInternalClient",
        path = "/api/${starpivot.api.version:v1}",
        fallbackFactory = MemberInternalClientFallbackFactory.class)
public interface MemberInternalClient {

    @GetMapping("/internal/member/{memberId}")
    Result<MemberDto> getMember(@PathVariable("memberId") Long memberId);

    @GetMapping("/internal/member/{memberId}/addresses/{addressId}")
    Result<MemberAddressDto> getAddress(
            @PathVariable("memberId") Long memberId,
            @PathVariable("addressId") Long addressId);

    @GetMapping("/internal/member/levels/{levelId}")
    Result<MemberLevelDto> getMemberLevel(@PathVariable("levelId") Long levelId);

    @GetMapping("/internal/member/{memberId}/wechat-open-id")
    Result<String> resolveWechatOpenId(@PathVariable("memberId") Long memberId);

    @PostMapping("/internal/member/integration/deduct-for-order")
    Result<Void> deductIntegrationForOrder(@RequestBody MemberOrderIntegrationRequest request);

    @PostMapping("/internal/member/integration/restore-for-order")
    Result<Void> restoreIntegrationForOrder(@RequestBody MemberOrderIntegrationRequest request);

    @PostMapping("/internal/member/reward/grant-on-paid")
    Result<Void> grantRewardOnPaid(@RequestBody MemberOrderRewardRequest request);

    @PostMapping("/internal/member/reward/clawback-on-return")
    Result<Void> clawbackRewardOnReturn(@RequestBody MemberOrderReturnRewardRequest request);
}
