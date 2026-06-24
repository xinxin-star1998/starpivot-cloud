package cn.org.starpivot.mall.oms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水 VO
 */
@Data
public class PaymentVo {

    private Long id;

    private String orderSn;

    private Long orderId;

    private String alipayTradeNo;

    private BigDecimal totalAmount;

    private String subject;

    private String paymentStatus;

    private LocalDateTime createTime;

    private LocalDateTime confirmTime;

    private String callbackContent;

    private LocalDateTime callbackTime;
}
