package cn.org.starpivot.api.mall.ware.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockDeductionLineDto implements Serializable {

    private Long skuId;

    private String skuName;

    private Long wareId;

    private Integer quantity;
}
