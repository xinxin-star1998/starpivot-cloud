package cn.org.starpivot.mall.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付信息实体。
 * <p>
 * 对应数据库表 {@code oms_payment_info}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("oms_payment_info")
public class OmsPaymentInfo {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
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
