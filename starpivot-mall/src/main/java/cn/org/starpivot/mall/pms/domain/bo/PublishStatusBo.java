package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** SPU 上架/下架 */
@Data
public class PublishStatusBo {

    @NotNull(message = "SPU ID 不能为空")
    private Long id;

    /** 0-下架 1-上架 */
    @NotNull(message = "上架状态不能为空")
    private Integer publishStatus;
}
