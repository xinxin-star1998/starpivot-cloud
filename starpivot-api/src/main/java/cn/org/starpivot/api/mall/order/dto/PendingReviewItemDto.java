package cn.org.starpivot.api.mall.order.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PendingReviewItemDto implements Serializable {

    private Long spuId;

    private Long skuId;

    private String spuName;

    private String coverImg;

    private String orderSn;
}
