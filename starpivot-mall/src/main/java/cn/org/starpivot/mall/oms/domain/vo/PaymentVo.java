package cn.org.starpivot.mall.oms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 支付视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PaymentVo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * Order ID
     */
    private Long orderId;

    /**
     * alipay Trade No
     */
    private String alipayTradeNo;

    /**
     * 订单总额
     */
    private BigDecimal totalAmount;

    /**
     * subject
     */
    private String subject;

    /**
     * 状态
     */
    private String paymentStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * confirm时间
     */
    private LocalDateTime confirmTime;

    /**
     * callback Content
     */
    private String callbackContent;

    /**
     * callback时间
     */
    private LocalDateTime callbackTime;
}
