package cn.org.starpivot.api.mall.order.dto;

import cn.org.starpivot.api.mall.promotion.dto.CouponTrialItemDto;
import lombok.Data;

import java.io.Serializable;
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
}
