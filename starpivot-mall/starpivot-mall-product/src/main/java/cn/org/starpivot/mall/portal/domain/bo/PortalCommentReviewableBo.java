package cn.org.starpivot.mall.portal.domain.bo;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PortalCommentReviewableBo {

    @NotEmpty(message = "商品ID列表不能为空")
    private List<Long> spuIds;
}
