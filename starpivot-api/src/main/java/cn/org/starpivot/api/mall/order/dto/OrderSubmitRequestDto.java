package cn.org.starpivot.api.mall.order.dto;

import cn.org.starpivot.api.mall.promotion.dto.CouponTrialItemDto;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitRequestDto implements Serializable {

    private Long memberId;
    private Long addressId;
    private Boolean useCart;
    private List<Long> cartSkuIds;
    private List<CouponTrialItemDto> items;
    private String note;
    private Integer payType;
    private Long couponHistoryId;
    private Integer useIntegration;
    private String orderToken;

    /** 活动类型：1拼团 2砍价，非活动订单为空 */
    private Integer activityType;

    /** 活动 ID */
    private Long activityId;

    /** 活动参与记录 ID */
    private Long activityRecordId;

    /** 活动成交单价，非空时覆盖普通计价（不参与优惠券/满减/阶梯价） */
    private BigDecimal activityPrice;
}
