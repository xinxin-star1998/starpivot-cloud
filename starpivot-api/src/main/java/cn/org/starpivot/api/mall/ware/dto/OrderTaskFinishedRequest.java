package cn.org.starpivot.api.mall.ware.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrderTaskFinishedRequest implements Serializable {

    @NotNull
    private Long orderId;

    private List<StockDeductionLineDto> deductions;
}
