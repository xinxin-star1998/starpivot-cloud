package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** SKU 上下架（更新所属 SPU 的 publish_status） */
@Data
public class SkuPublishStatusBo {

    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;

    /** 0-下架 1-上架 */
    @NotNull(message = "上架状态不能为空")
    private Integer publishStatus;
}
