package cn.org.starpivot.mall.oms.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.oms.domain.bo.PaymentReqBo;
import cn.org.starpivot.mall.oms.domain.vo.PaymentVo;
import cn.org.starpivot.mall.oms.service.OmsPaymentInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付流水（只读）
 */
@RestController
@RequestMapping("/mall/payment")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-支付流水", description = "支付流水只读查询")
public class OmsPaymentController {

    private final OmsPaymentInfoService omsPaymentInfoService;

    @Operation(summary = "支付流水分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAnyAuthority('mall:payment:list', 'mall:payment:query')")
    public Result<PageResponse<PaymentVo>> pageList(@RequestBody PaymentReqBo reqBo) {
        return Result.success(omsPaymentInfoService.pageList(reqBo));
    }
}
