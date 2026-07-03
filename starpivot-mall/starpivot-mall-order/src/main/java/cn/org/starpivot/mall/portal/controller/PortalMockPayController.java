package cn.org.starpivot.mall.portal.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import cn.org.starpivot.mall.portal.service.PortalOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Mock 支付（仅 {@code starpivot.mall.mock-pay-enabled=true} 时注册，生产环境默认关闭）。
 */
@RestController
@RequestMapping("/portal/order")
@ConditionalOnProperty(prefix = "starpivot.mall", name = "mock-pay-enabled", havingValue = "true")
@RequiredArgsConstructor
@Tag(name = "C端-订单", description = "开发联调 Mock 支付")
public class PortalMockPayController {

    private final PortalOrderService portalOrderService;

    @Operation(summary = "Mock 支付", description = "开发联调用：待付款 → 待发货")
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> mockPay(@PathVariable("id") Long id) {
        portalOrderService.mockPay(PortalMemberContext.requireMemberId(), id);
        return Result.success("支付成功");
    }
}
