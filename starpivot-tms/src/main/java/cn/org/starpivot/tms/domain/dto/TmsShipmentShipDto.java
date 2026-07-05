package cn.org.starpivot.tms.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TmsShipmentShipDto {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "承运商不能为空")
    private Long carrierId;

    @NotBlank(message = "物流单号不能为空")
    private String trackingNo;
}
