package cn.org.starpivot.mall.pay.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.pay.domain.vo.AlipayPagePayVo;
import cn.org.starpivot.mall.pay.service.AlipayPayService;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * C 端支付宝支付（电脑网站支付 + 异步通知）。
 */
@RestController
@RequestMapping("/portal/pay/alipay")
@RequiredArgsConstructor
@Tag(name = "C端-支付宝", description = "电脑网站支付与异步回调")
public class PortalAlipayController {

    private final AlipayPayService alipayPayService;

    @Operation(summary = "是否启用支付宝")
    @GetMapping("/enabled")
    public Result<Boolean> enabled() {
        return Result.success(alipayPayService.isAvailable());
    }

    @Operation(summary = "发起支付宝电脑网站支付")
    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<AlipayPagePayVo> pay(@PathVariable("orderId") Long orderId) {
        return Result.success(alipayPayService.createPagePay(PortalMemberContext.requireMemberId(), orderId));
    }

    /**
     * 支付宝异步通知（须公网可达，网关/Mall 安全白名单放行）。
     */
    @Operation(summary = "支付宝异步通知")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, String> params = alipayPayService.extractNotifyParams(request);
        return alipayPayService.handleNotify(params) ? "success" : "failure";
    }
}
