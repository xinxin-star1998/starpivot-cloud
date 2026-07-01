package cn.org.starpivot.mall.portal.domain.bo;

import cn.org.starpivot.common.domain.PageReqBo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PortalCommentQueryBo extends PageReqBo {

    @NotNull(message = "商品ID不能为空")
    private Long spuId;
}
