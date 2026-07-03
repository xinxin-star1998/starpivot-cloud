package cn.org.starpivot.mall.pay.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.pay.config.AlipayProperties;
import cn.org.starpivot.mall.pay.domain.vo.AlipayPagePayVo;
import cn.org.starpivot.mall.pay.service.AlipayPayService;
import cn.org.starpivot.mall.pay.service.PortalOrderPayService;
import cn.org.starpivot.mall.portal.PortalConstants;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayPayServiceImpl implements AlipayPayService {

    private static final String PRODUCT_CODE = "FAST_INSTANT_TRADE_PAY";
    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";
    private static final String TRADE_FINISHED = "TRADE_FINISHED";

    private final AlipayProperties alipayProperties;
    private final OmsOrderMapper omsOrderMapper;
    private final PortalOrderPayService portalOrderPayService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean isAvailable() {
        return alipayProperties.isConfigured();
    }

    @Override
    @Transactional(readOnly = true)
    public AlipayPagePayVo createPagePay(Long memberId, Long orderId) {
        if (!isAvailable()) {
            throw new BizException("支付宝支付未启用或未配置完整");
        }
        OmsOrder order = requireUnpaidOrder(memberId, orderId);

        try {
            AlipayClient client = buildClient();
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayProperties.getNotifyUrl());
            if (StringUtils.hasText(alipayProperties.getReturnUrl())) {
                request.setReturnUrl(alipayProperties.getReturnUrl());
            }

            Map<String, Object> biz = new HashMap<>();
            biz.put("out_trade_no", order.getOrderSn());
            biz.put("total_amount", order.getPayAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
            biz.put("subject", "StarPivot商城-" + order.getOrderSn());
            biz.put("product_code", PRODUCT_CODE);
            request.setBizContent(objectMapper.writeValueAsString(biz));

            AlipayTradePagePayResponse response = client.pageExecute(request);
            if (!response.isSuccess()) {
                throw new BizException("支付宝下单失败：" + response.getSubMsg());
            }

            AlipayPagePayVo vo = new AlipayPagePayVo();
            vo.setOrderSn(order.getOrderSn());
            vo.setPayForm(response.getBody());
            return vo;
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Alipay page pay failed, orderId={}", orderId, ex);
            throw new BizException("支付宝下单失败：" + ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String refund(String orderSn, String alipayTradeNo, BigDecimal refundAmount, String outRequestNo) {
        if (!isAvailable()) {
            throw new BizException("支付宝未启用，无法原路退款");
        }
        if (refundAmount == null || refundAmount.signum() <= 0) {
            throw new BizException("退款金额无效");
        }
        if (!StringUtils.hasText(outRequestNo)) {
            throw new BizException("退款请求号不能为空");
        }
        try {
            AlipayClient client = buildClient();
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            Map<String, Object> biz = new HashMap<>();
            if (StringUtils.hasText(alipayTradeNo)) {
                biz.put("trade_no", alipayTradeNo);
            } else {
                biz.put("out_trade_no", orderSn);
            }
            biz.put("refund_amount", refundAmount.setScale(2, RoundingMode.HALF_UP).toPlainString());
            biz.put("out_request_no", outRequestNo);
            biz.put("refund_reason", "商城退货退款");
            request.setBizContent(objectMapper.writeValueAsString(biz));

            AlipayTradeRefundResponse response = client.execute(request);
            if (!response.isSuccess()) {
                throw new BizException("支付宝退款失败：" + response.getSubMsg());
            }
            return response.getTradeNo();
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Alipay refund failed, orderSn={}, outRequestNo={}", orderSn, outRequestNo, ex);
            throw new BizException("支付宝退款失败：" + ex.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleNotify(Map<String, String> params) {
        if (!isAvailable() || params == null || params.isEmpty()) {
            return false;
        }
        try {
            boolean verified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.normalizedAlipayPublicKey(),
                    "UTF-8",
                    "RSA2");
            if (!verified) {
                log.warn("Alipay notify signature invalid, out_trade_no={}", params.get("out_trade_no"));
                return false;
            }

            String tradeStatus = params.get("trade_status");
            if (!TRADE_SUCCESS.equals(tradeStatus) && !TRADE_FINISHED.equals(tradeStatus)) {
                log.info("Alipay notify ignored trade_status={}, out_trade_no={}", tradeStatus, params.get("out_trade_no"));
                return true;
            }

            String orderSn = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            if (!StringUtils.hasText(orderSn)) {
                return false;
            }

            OmsOrder order = omsOrderMapper.selectOne(
                    Wrappers.<OmsOrder>lambdaQuery().eq(OmsOrder::getOrderSn, orderSn).last("LIMIT 1"));
            if (order == null) {
                log.warn("Alipay notify order not found: {}", orderSn);
                return false;
            }

            if (!verifyPayAmount(order, params.get("total_amount"))) {
                log.warn("Alipay notify amount mismatch, orderSn={}, expected={}, actual={}",
                        orderSn, order.getPayAmount(), params.get("total_amount"));
                return false;
            }

            String callbackJson = objectMapper.writeValueAsString(params);
            boolean confirmed = portalOrderPayService.confirmPaid(order, tradeNo, tradeStatus, callbackJson);
            if (!confirmed && Integer.valueOf(PortalConstants.ORDER_STATUS_CLOSED).equals(order.getStatus())) {
                log.warn("Alipay notify could not confirm closed order, orderSn={}", orderSn);
                return false;
            }
            return true;
        } catch (Exception ex) {
            log.error("Alipay notify handle failed", ex);
            return false;
        }
    }

    @Override
    public Map<String, String> extractNotifyParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            if (values == null || values.length == 0) {
                continue;
            }
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                valueStr.append(i == values.length - 1 ? values[i] : values[i] + ",");
            }
            params.put(name, valueStr.toString());
        }
        return params;
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

    private boolean verifyPayAmount(OmsOrder order, String totalAmountStr) {
        if (order.getPayAmount() == null || !StringUtils.hasText(totalAmountStr)) {
            return true;
        }
        try {
            BigDecimal paid = new BigDecimal(totalAmountStr.trim()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal expected = order.getPayAmount().setScale(2, RoundingMode.HALF_UP);
            return expected.compareTo(paid) == 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private AlipayClient buildClient() {
        return new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.normalizedPrivateKey(),
                "json",
                "UTF-8",
                alipayProperties.normalizedAlipayPublicKey(),
                "RSA2");
    }
}
