package cn.org.starpivot.mall.pay.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.common.MemberFeignSupport;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.service.OmsRefundNotifyService;
import cn.org.starpivot.mall.pay.config.WxPayProperties;
import cn.org.starpivot.mall.pay.domain.vo.WxJsapiPayVo;
import cn.org.starpivot.mall.pay.domain.vo.WxNativePayVo;
import cn.org.starpivot.mall.pay.domain.vo.WxRefundResult;
import cn.org.starpivot.mall.pay.service.PortalOrderPayService;
import cn.org.starpivot.mall.pay.service.WxPayService;
import cn.org.starpivot.mall.portal.PortalConstants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ServiceException;
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
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import com.wechat.pay.java.service.refund.model.Status;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

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
    private final MemberFeignSupport memberFeignSupport;
    private final ObjectMapper objectMapper;
    private final OmsRefundNotifyService omsRefundNotifyService;

    @Override
    public boolean isAvailable() {
        return wxPayProperties.isUsable();
    }

    @Override
    public boolean isConfigured() {
        return wxPayProperties.isConfigured();
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
        String openId = memberFeignSupport.requireWechatOpenId(memberId);
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
    @Transactional(readOnly = true)
    public WxRefundResult refund(
            String orderSn,
            String wxTransactionId,
            BigDecimal refundAmount,
            BigDecimal orderPayAmount,
            String outRefundNo) {
        if (!isConfigured()) {
            throw new BizException("微信支付未配置，无法原路退款");
        }
        if (!StringUtils.hasText(orderSn)) {
            throw new BizException("商户订单号不能为空");
        }
        if (refundAmount == null || refundAmount.signum() <= 0) {
            throw new BizException("退款金额无效");
        }
        if (orderPayAmount == null || orderPayAmount.signum() <= 0) {
            throw new BizException("订单原支付金额无效");
        }
        if (!StringUtils.hasText(outRefundNo)) {
            throw new BizException("退款请求号不能为空");
        }
        try {
            RefundService refundService = new RefundService.Builder().config(buildConfig()).build();
            CreateRequest request = new CreateRequest();
            if (StringUtils.hasText(wxTransactionId)) {
                request.setTransactionId(wxTransactionId);
            } else {
                request.setOutTradeNo(orderSn);
            }
            request.setOutRefundNo(outRefundNo);
            request.setReason("商城退货退款");
            if (StringUtils.hasText(wxPayProperties.getRefundNotifyUrl())) {
                request.setNotifyUrl(wxPayProperties.getRefundNotifyUrl());
            }

            AmountReq amount = new AmountReq();
            amount.setCurrency("CNY");
            amount.setRefund((long) toFen(refundAmount));
            amount.setTotal((long) toFen(orderPayAmount));
            request.setAmount(amount);

            Refund response = refundService.create(request);
            if (response == null) {
                throw new BizException("微信退款失败：无响应");
            }
            Status status = response.getStatus();
            if (status != Status.SUCCESS && status != Status.PROCESSING) {
                throw new BizException("微信退款失败：status=" + (status != null ? status.name() : "UNKNOWN"));
            }

            WxRefundResult result = new WxRefundResult();
            result.setRefundId(response.getRefundId());
            result.setOutRefundNo(response.getOutRefundNo());
            result.setTransactionId(response.getTransactionId());
            result.setStatus(status != null ? status.name() : null);
            result.setRawResponse(objectMapper.writeValueAsString(response));
            return result;
        } catch (BizException ex) {
            throw ex;
        } catch (ServiceException ex) {
            log.error("Wx refund service failed, orderSn={}, outRefundNo={}", orderSn, outRefundNo, ex);
            throw new BizException("微信退款失败：" + ex.getErrorMessage());
        } catch (Exception ex) {
            log.error("Wx refund failed, orderSn={}, outRefundNo={}", orderSn, outRefundNo, ex);
            throw new BizException("微信退款失败：" + ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WxRefundResult queryRefund(String outRefundNo) {
        if (!isConfigured()) {
            throw new BizException("微信支付未配置，无法查询退款");
        }
        if (!StringUtils.hasText(outRefundNo)) {
            throw new BizException("退款请求号不能为空");
        }
        try {
            RefundService refundService = new RefundService.Builder().config(buildConfig()).build();
            QueryByOutRefundNoRequest request = new QueryByOutRefundNoRequest();
            request.setOutRefundNo(outRefundNo);
            Refund response = refundService.queryByOutRefundNo(request);
            if (response == null) {
                throw new BizException("微信退款查询失败：无响应");
            }
            Status status = response.getStatus();
            WxRefundResult result = new WxRefundResult();
            result.setRefundId(response.getRefundId());
            result.setOutRefundNo(response.getOutRefundNo());
            result.setTransactionId(response.getTransactionId());
            result.setStatus(status != null ? status.name() : null);
            result.setRawResponse(objectMapper.writeValueAsString(response));
            return result;
        } catch (BizException ex) {
            throw ex;
        } catch (ServiceException ex) {
            log.error("Wx refund query service failed, outRefundNo={}", outRefundNo, ex);
            throw new BizException("微信退款查询失败：" + ex.getErrorMessage());
        } catch (Exception ex) {
            log.error("Wx refund query failed, outRefundNo={}", outRefundNo, ex);
            throw new BizException("微信退款查询失败：" + ex.getMessage());
        }
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleRefundNotify(HttpServletRequest request, String body) {
        if (!wxPayProperties.isConfigured()) {
            return failureBody("NOT_CONFIGURED");
        }
        try {
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(request.getHeader("Wechatpay-Serial"))
                    .nonce(request.getHeader("Wechatpay-Nonce"))
                    .signature(request.getHeader("Wechatpay-Signature"))
                    .timestamp(request.getHeader("Wechatpay-Timestamp"))
                    .signType(request.getHeader("Wechatpay-Signature-Type"))
                    .body(body)
                    .build();

            NotificationParser parser = new NotificationParser((NotificationConfig) buildConfig());
            RefundNotification notification = parser.parse(requestParam, RefundNotification.class);
            if (notification == null) {
                return successBody();
            }

            Status refundStatus = notification.getRefundStatus();
            String statusName = refundStatus != null ? refundStatus.name() : null;
            String callbackJson = objectMapper.writeValueAsString(notification);
            omsRefundNotifyService.handleWxRefundNotify(
                    notification.getOutRefundNo(), statusName, callbackJson);
            return successBody();
        } catch (Exception ex) {
            log.error("Wx refund notify handle failed", ex);
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
