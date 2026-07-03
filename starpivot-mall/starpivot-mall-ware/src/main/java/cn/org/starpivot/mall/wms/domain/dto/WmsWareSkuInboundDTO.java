package cn.org.starpivot.mall.wms.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 快速入库请求 DTO。
 */
@Data
public class WmsWareSkuInboundDTO {

    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;

    @NotNull(message = "仓库不能为空")
    private Long wareId;

    @NotNull(message = "入库数量不能为空")
    @Min(value = 1, message = "入库数量至少为 1")
    private Integer skuNum;
}
