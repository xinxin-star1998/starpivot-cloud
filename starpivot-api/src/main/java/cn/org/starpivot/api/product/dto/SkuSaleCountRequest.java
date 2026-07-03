package cn.org.starpivot.api.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class SkuSaleCountRequest implements Serializable {

    @NotNull
    private Long skuId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
