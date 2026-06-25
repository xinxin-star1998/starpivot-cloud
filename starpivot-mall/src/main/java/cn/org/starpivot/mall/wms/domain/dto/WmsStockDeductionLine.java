package cn.org.starpivot.mall.wms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付出库时的 SKU 仓库扣减明细（用于生成已完成工作单）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WmsStockDeductionLine {

    private Long skuId;

    private String skuName;

    private Long wareId;

    private Integer quantity;
}
