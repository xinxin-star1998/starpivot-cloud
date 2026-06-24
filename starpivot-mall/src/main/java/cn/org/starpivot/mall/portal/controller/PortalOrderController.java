package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderQueryBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalOrderSubmitBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderSubmitVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalOrderVo;
import cn.org.starpivot.mall.portal.service.PortalOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/order")
@RequiredArgsConstructor
@Validated
@Tag(name = "C端-订单", description = "提交订单、我的订单")
public class PortalOrderController {

    private final PortalOrderService portalOrderService;

    @Operation(summary = "提交订单")
    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalOrderSubmitVo> submit(@Valid @RequestBody PortalOrderSubmitBo bo) {
        return Result.success(portalOrderService.submit(PortalMemberContext.requireMemberId(), bo));
    }

    @Operation(summary = "我的订单分页")
    @PostMapping("/list")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PageResponse<PortalOrderVo>> pageList(@RequestBody PortalOrderQueryBo bo) {
        return Result.success(portalOrderService.pageMyOrders(PortalMemberContext.requireMemberId(), bo));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<PortalOrderVo> detail(@PathVariable("id") Long id) {
        return Result.success(portalOrderService.getMyOrder(PortalMemberContext.requireMemberId(), id));
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> cancel(@PathVariable("id") Long id) {
        portalOrderService.cancel(PortalMemberContext.requireMemberId(), id);
        return Result.success("取消成功");
    }

    @Operation(summary = "Mock 支付", description = "开发联调用：待付款 → 待发货，写入 oms_payment_info")
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> mockPay(@PathVariable("id") Long id) {
        portalOrderService.mockPay(PortalMemberContext.requireMemberId(), id);
        return Result.success("支付成功");
    }
}
