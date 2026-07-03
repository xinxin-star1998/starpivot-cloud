package cn.org.starpivot.api.mall.order.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderSubmitResultDto implements Serializable {

    private Long orderId;
    private String orderSn;
    private Integer status;
}
