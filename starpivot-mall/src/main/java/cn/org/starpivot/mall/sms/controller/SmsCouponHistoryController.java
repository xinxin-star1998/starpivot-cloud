package cn.org.starpivot.mall.sms.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.CouponHistoryReqBo;
import cn.org.starpivot.mall.sms.domain.vo.CouponHistoryVo;
import cn.org.starpivot.mall.sms.service.SmsCouponHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mall/coupon-history")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-优惠券发放记录")
public class SmsCouponHistoryController {

    private final SmsCouponHistoryService smsCouponHistoryService;

    @Operation(summary = "优惠券发放记录分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:coupon:history')")
    public Result<PageResponse<CouponHistoryVo>> pageList(@RequestBody CouponHistoryReqBo reqBo) {
        return Result.success(smsCouponHistoryService.pageList(reqBo));
    }
}
