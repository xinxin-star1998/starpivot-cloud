package cn.org.starpivot.api.mall.ware.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class InboundForReturnRequest implements Serializable {

    @NotNull
    private Long orderId;

    @NotNull
    private Long skuId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
