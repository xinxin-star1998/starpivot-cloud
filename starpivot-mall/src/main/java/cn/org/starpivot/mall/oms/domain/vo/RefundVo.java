package cn.org.starpivot.mall.oms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款流水 VO
 */
@Data
public class RefundVo {

    private Long id;

    private Long orderReturnId;

    private BigDecimal refund;

    private String refundSn;

    private Integer refundStatus;

    private Integer refundChannel;

    private String refundContent;
}
