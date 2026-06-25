package cn.org.starpivot.mall.oms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;


/**
 * 退款视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class RefundVo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * OrderReturn ID
     */
    private Long orderReturnId;

    /**
     * refund
     */
    private BigDecimal refund;

    /**
     * refund Sn
     */
    private String refundSn;

    /**
     * 状态
     */
    private Integer refundStatus;

    /**
     * refund Channel
     */
    private Integer refundChannel;

    /**
     * refund Content
     */
    private String refundContent;
}
