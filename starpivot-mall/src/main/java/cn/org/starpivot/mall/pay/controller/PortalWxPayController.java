package cn.org.starpivot.mall.pay.controller;

import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.mall.pay.domain.vo.WxJsapiPayVo;
import cn.org.starpivot.mall.pay.domain.vo.WxNativePayVo;
import cn.org.starpivot.mall.pay.service.WxPayService;
import cn.org.starpivot.mall.pay.service.impl.WxPayServiceImpl;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.PortalMemberContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * C 端微信 Native 支付。
 */
@RestController
@RequestMapping("/portal/pay/wx")
@RequiredArgsConstructor
@Tag(name = "C端-微信支付", description = "Native 扫码支付与异步回调")
public class PortalWxPayController {

    private final WxPayService wxPayService;

    @Operation(summary = "是否启用微信支付")
    @GetMapping("/enabled")
    public Result<Map<String, Object>> enabled() {
        Map<String, Object> data = new HashMap<>();
        data.put("enabled", wxPayService.isAvailable());
        data.put("mock", wxPayService.isMockMode());
        data.put("jsapi", wxPayService.isAvailable());
        return Result.success(data);
    }

    @Operation(summary = "发起微信 Native 支付")
    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<WxNativePayVo> nativePay(@PathVariable("orderId") Long orderId) {
        return Result.success(wxPayService.createNativePay(PortalMemberContext.requireMemberId(), orderId));
    }

    @Operation(summary = "发起微信 JSAPI 支付（小程序）")
    @PostMapping("/jsapi/{orderId}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<WxJsapiPayVo> jsapiPay(@PathVariable("orderId") Long orderId) {
        return Result.success(wxPayService.createJsapiPay(PortalMemberContext.requireMemberId(), orderId));
    }

    @Operation(summary = "Mock 微信确认支付（仅 mock 模式）")
    @PostMapping("/mock/{orderId}")
    @PreAuthorize("hasAuthority('" + PortalConstants.MEMBER_ROLE + "')")
    public Result<?> mockPay(@PathVariable("orderId") Long orderId) {
        wxPayService.mockConfirmPaid(PortalMemberContext.requireMemberId(), orderId);
        return Result.success("支付成功");
    }

    @Operation(summary = "微信支付异步通知")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) throws Exception {
        String body = WxPayServiceImpl.readBody(request);
        return wxPayService.handleNotify(request, body);
    }
}
