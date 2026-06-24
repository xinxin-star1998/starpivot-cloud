package cn.org.starpivot.mall.oms.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.mall.oms.domain.bo.OmsDeliverBo;
import cn.org.starpivot.mall.oms.domain.bo.OmsOrderCloseBo;
import cn.org.starpivot.mall.oms.domain.bo.OmsOrderReqBo;
import cn.org.starpivot.mall.oms.domain.vo.OmsOrderVo;
import cn.org.starpivot.mall.oms.service.OmsOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理
 */
@RestController
@RequestMapping("/mall/order")
@RequiredArgsConstructor
@Validated
@Tag(name = "商城-订单", description = "订单查询、发货、关闭")
public class OmsOrderController {

    private final OmsOrderService omsOrderService;

    @Operation(summary = "订单分页列表")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('mall:order:query')")
    public Result<PageResponse<OmsOrderVo>> pageList(@RequestBody OmsOrderReqBo reqBo) {
        return Result.success(omsOrderService.pageList(reqBo));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:order:query')")
    public Result<OmsOrderVo> getById(@PathVariable("id") Long id) {
        return Result.success(omsOrderService.getDetailById(id));
    }

    @Log(title = "订单发货", businessType = BusinessType.UPDATE)
    @Operation(summary = "订单发货")
    @PutMapping("/deliver")
    @PreAuthorize("hasAuthority('mall:order:deliver')")
    public Result<?> deliver(@Valid @RequestBody OmsDeliverBo bo) {
        omsOrderService.deliver(bo);
        return Result.success("发货成功");
    }

    @Log(title = "关闭订单", businessType = BusinessType.UPDATE)
    @Operation(summary = "关闭订单")
    @PutMapping("/close")
    @PreAuthorize("hasAuthority('mall:order:close')")
    public Result<?> close(@Valid @RequestBody OmsOrderCloseBo bo) {
        omsOrderService.close(bo);
        return Result.success("关闭成功");
    }
}
