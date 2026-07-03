package cn.org.starpivot.mall.member.controller.internal;

import cn.org.starpivot.api.member.dto.*;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.member.internal.MemberIntegrationInternalService;
import cn.org.starpivot.mall.member.internal.MemberRewardInternalService;
import cn.org.starpivot.mall.portal.auth.service.PortalMemberAuthService;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.entity.UmsMemberLevel;
import cn.org.starpivot.mall.ums.entity.UmsMemberReceiveAddress;
import cn.org.starpivot.mall.ums.mapper.UmsMemberLevelMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberReceiveAddressMapper;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
@RequestMapping("/internal/member")
@RequiredArgsConstructor
public class MemberInternalController {

    private final UmsMemberMapper umsMemberMapper;
    private final UmsMemberReceiveAddressMapper addressMapper;
    private final UmsMemberLevelMapper umsMemberLevelMapper;
    private final PortalMemberAuthService portalMemberAuthService;
    private final MemberIntegrationInternalService memberIntegrationInternalService;
    private final MemberRewardInternalService memberRewardInternalService;

    @GetMapping("/{memberId}")
    public Result<MemberDto> getMember(@PathVariable("memberId") Long memberId) {
        UmsMember member = umsMemberMapper.selectById(memberId);
        if (member == null) {
            return Result.notFound("会员不存在");
        }
        return Result.success(toMemberDto(member));
    }

    @GetMapping("/{memberId}/addresses/{addressId}")
    public Result<MemberAddressDto> getAddress(
            @PathVariable("memberId") Long memberId,
            @PathVariable("addressId") Long addressId) {
        UmsMemberReceiveAddress address = addressMapper.selectById(addressId);
        if (address == null || !memberId.equals(address.getMemberId())) {
            return Result.notFound("收货地址不存在");
        }
        return Result.success(toAddressDto(address));
    }

    @GetMapping("/levels/{levelId}")
    public Result<MemberLevelDto> getMemberLevel(@PathVariable("levelId") Long levelId) {
        UmsMemberLevel level = umsMemberLevelMapper.selectById(levelId);
        if (level == null) {
            return Result.notFound("会员等级不存在");
        }
        return Result.success(toLevelDto(level));
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

    private MemberDto toMemberDto(UmsMember member) {
        MemberDto dto = new MemberDto();
        dto.setId(member.getId());
        dto.setUsername(member.getUsername());
        dto.setNickname(member.getNickname());
        dto.setMobile(member.getMobile());
        dto.setHeader(member.getHeader());
        dto.setStatus(member.getStatus());
        dto.setLevelId(member.getLevelId());
        dto.setIntegration(member.getIntegration());
        dto.setGrowth(member.getGrowth());
        return dto;
    }

    private MemberAddressDto toAddressDto(UmsMemberReceiveAddress address) {
        MemberAddressDto dto = new MemberAddressDto();
        BeanUtils.copyProperties(address, dto);
        return dto;
    }

    private MemberLevelDto toLevelDto(UmsMemberLevel level) {
        MemberLevelDto dto = new MemberLevelDto();
        dto.setId(level.getId());
        dto.setName(level.getName());
        dto.setGrowthPoint(level.getGrowthPoint());
        dto.setFreeFreightPoint(level.getFreeFreightPoint());
        dto.setPriviledgeFreeFreight(level.getPrivilegeFreeFreight());
        dto.setPriviledgeMemberPrice(level.getPrivilegeMemberPrice());
        dto.setPriviledgeBirthday(level.getPrivilegeBirthday());
        return dto;
    }
}
