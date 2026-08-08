package cn.org.starpivot.api.mall.order.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单按月销售金额聚合（内部接口）。
 */
@Data
public class OrderSalesMonthAmountDto implements Serializable {

    /** 年月，格式 yyyy-MM */
    private String yearMonth;

    /** 当月已付款订单支付金额合计（元） */
    private BigDecimal totalAmount;
}
