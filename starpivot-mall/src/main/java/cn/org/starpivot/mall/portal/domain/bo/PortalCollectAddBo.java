package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PortalCollectAddBo {

    @NotNull(message = "商品ID不能为空")
    private Long spuId;
}
