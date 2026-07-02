package cn.org.starpivot.mall.pay.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.pay.config.WxPayProperties;
import cn.org.starpivot.mall.pay.domain.vo.WxJsapiPayVo;
import cn.org.starpivot.mall.pay.domain.vo.WxNativePayVo;
import cn.org.starpivot.mall.pay.service.PortalOrderPayService;
import cn.org.starpivot.mall.pay.service.WxPayService;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.auth.service.PortalMemberAuthService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class WxPayServiceImpl implements WxPayService {

    private final WxPayProperties wxPayProperties;
    private final OmsOrderMapper omsOrderMapper;
    private final PortalOrderPayService portalOrderPayService;
    private final PortalMemberAuthService memberAuthService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean isAvailable() {
        return wxPayProperties.isUsable();
    }

    @Override
    public boolean isMockMode() {
        return wxPayProperties.isMockAvailable();
    }

    @Override
    @Transactional(readOnly = true)
    public WxNativePayVo createNativePay(Long memberId, Long orderId) {
        if (!isAvailable()) {
            throw new BizException("微信支付未启用");
        }
        OmsOrder order = requireUnpaidOrder(memberId, orderId);
        if (wxPayProperties.isMockAvailable()) {
            WxNativePayVo vo = new WxNativePayVo();
            vo.setOrderSn(order.getOrderSn());
            vo.setMock(true);
            vo.setCodeUrl("MOCK-WX-NATIVE://" + order.getOrderSn());
            return vo;
        }
        try {
            NativePayService nativePayService = new NativePayService.Builder().config(buildConfig()).build();
            PrepayRequest request = new PrepayRequest();
            request.setAppid(wxPayProperties.getAppId());
            request.setMchid(wxPayProperties.getMchId());
            request.setDescription("StarPivot商城-" + order.getOrderSn());
            request.setOutTradeNo(order.getOrderSn());
            request.setNotifyUrl(wxPayProperties.getNotifyUrl());

            Amount amount = new Amount();
            amount.setCurrency("CNY");
            amount.setTotal(toFen(order.getPayAmount()));
            request.setAmount(amount);

            PrepayResponse response = nativePayService.prepay(request);
            WxNativePayVo vo = new WxNativePayVo();
            vo.setOrderSn(order.getOrderSn());
            vo.setCodeUrl(response.getCodeUrl());
            vo.setMock(false);
            return vo;
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Wx native pay failed, orderId={}", orderId, ex);
            throw new BizException("微信下单失败：" + ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WxJsapiPayVo createJsapiPay(Long memberId, Long orderId) {
        if (!isAvailable()) {
            throw new BizException("微信支付未启用");
        }
        OmsOrder order = requireUnpaidOrder(memberId, orderId);
        if (wxPayProperties.isMockAvailable()) {
            WxJsapiPayVo vo = new WxJsapiPayVo();
            vo.setOrderSn(order.getOrderSn());
            vo.setMock(true);
            vo.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
            vo.setNonceStr("MOCK" + order.getOrderSn());
            vo.setPackageValue("prepay_id=MOCK-" + order.getOrderSn());
            vo.setSignType("RSA");
            vo.setPaySign("MOCK_SIGN");
            return vo;
        }
        String openId = memberAuthService.resolveWechatOpenId(memberId);
        try {
            JsapiServiceExtension jsapiService = new JsapiServiceExtension.Builder().config(buildConfig()).build();
            com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest request =
                    new com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest();
            request.setAppid(wxPayProperties.getAppId());
            request.setMchid(wxPayProperties.getMchId());
            request.setDescription("StarPivot商城-" + order.getOrderSn());
            request.setOutTradeNo(order.getOrderSn());
            request.setNotifyUrl(wxPayProperties.getNotifyUrl());

            com.wechat.pay.java.service.payments.jsapi.model.Amount amount =
                    new com.wechat.pay.java.service.payments.jsapi.model.Amount();
            amount.setCurrency("CNY");
            amount.setTotal(toFen(order.getPayAmount()));
            request.setAmount(amount);

            Payer payer = new Payer();
            payer.setOpenid(openId);
            request.setPayer(payer);

            PrepayWithRequestPaymentResponse response = jsapiService.prepayWithRequestPayment(request);
            WxJsapiPayVo vo = new WxJsapiPayVo();
            vo.setOrderSn(order.getOrderSn());
            vo.setMock(false);
            vo.setTimeStamp(response.getTimeStamp());
            vo.setNonceStr(response.getNonceStr());
            vo.setPackageValue(response.getPackageVal());
            vo.setSignType(response.getSignType());
            vo.setPaySign(response.getPaySign());
            return vo;
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Wx jsapi pay failed, orderId={}", orderId, ex);
            throw new BizException("微信下单失败：" + ex.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mockConfirmPaid(Long memberId, Long orderId) {
        if (!wxPayProperties.isMockAvailable()) {
            throw new BizException("微信 Mock 支付未启用");
        }
        OmsOrder order = requireUnpaidOrder(memberId, orderId);
        portalOrderPayService.confirmPaid(
                order,
                "WXMOCK" + System.currentTimeMillis(),
                PortalConstants.PAYMENT_STATUS_SUCCESS,
                "{\"mock\":true,\"channel\":\"wechat\"}");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleNotify(HttpServletRequest request, String body) {
        if (!wxPayProperties.isConfigured()) {
            return failureBody("NOT_CONFIGURED");
        }
        try {
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(request.getHeader("Wechatpay-Serial"))
                    .nonce(request.getHeader("Wechatpay-Nonce"))
                    .signature(request.getHeader("Wechatpay-Signature"))
                    .timestamp(request.getHeader("Wechatpay-Timestamp"))
                    .body(body)
                    .build();

            NotificationParser parser = new NotificationParser((NotificationConfig) buildConfig());
            Transaction transaction = parser.parse(requestParam, Transaction.class);
            if (transaction == null
                    || transaction.getTradeState() != Transaction.TradeStateEnum.SUCCESS) {
                return successBody();
            }

            String orderSn = transaction.getOutTradeNo();
            OmsOrder order = omsOrderMapper.selectOne(
                    Wrappers.<OmsOrder>lambdaQuery().eq(OmsOrder::getOrderSn, orderSn).last("LIMIT 1"));
            if (order == null) {
                log.warn("Wx notify order not found: {}", orderSn);
                return failureBody("ORDER_NOT_FOUND");
            }
            if (!verifyPayAmount(order, transaction.getAmount() != null ? transaction.getAmount().getTotal() : null)) {
                log.warn("Wx notify amount mismatch, orderSn={}", orderSn);
                return failureBody("AMOUNT_MISMATCH");
            }

            String callbackJson = objectMapper.writeValueAsString(transaction);
            portalOrderPayService.confirmPaid(
                    order,
                    transaction.getTransactionId(),
                    transaction.getTradeState().name(),
                    callbackJson);
            return successBody();
        } catch (Exception ex) {
            log.error("Wx notify handle failed", ex);
            return failureBody("HANDLE_ERROR");
        }
    }

    private Config buildConfig() {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(wxPayProperties.getMchId())
                .privateKey(wxPayProperties.normalizedPrivateKey())
                .merchantSerialNumber(wxPayProperties.getMerchantSerialNumber())
                .apiV3Key(wxPayProperties.getApiV3Key())
                .build();
    }

    private OmsOrder requireUnpaidOrder(Long memberId, Long orderId) {
        if (orderId == null) {
            throw new BizException("订单ID不能为空");
        }
        OmsOrder order = omsOrderMapper.selectById(orderId);
        if (order == null
                || Integer.valueOf(1).equals(order.getDeleteStatus())
                || !memberId.equals(order.getMemberId())) {
            throw new BizException("订单不存在");
        }
        if (!Integer.valueOf(PortalConstants.ORDER_STATUS_UNPAID).equals(order.getStatus())) {
            throw new BizException("仅待付款订单可支付");
        }
        if (order.getPayAmount() == null || order.getPayAmount().signum() <= 0) {
            throw new BizException("订单金额无效");
        }
        return order;
    }

    private int toFen(BigDecimal yuan) {
        return yuan.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private boolean verifyPayAmount(OmsOrder order, Integer totalFen) {
        if (order.getPayAmount() == null || totalFen == null) {
            return true;
        }
        return toFen(order.getPayAmount()) == totalFen;
    }

    private String successBody() {
        return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
    }

    private String failureBody(String message) {
        return "{\"code\":\"FAIL\",\"message\":\"" + message + "\"}";
    }

    /** 读取原始通知 body */
    public static String readBody(HttpServletRequest request) throws java.io.IOException {
        return StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
    }
}
