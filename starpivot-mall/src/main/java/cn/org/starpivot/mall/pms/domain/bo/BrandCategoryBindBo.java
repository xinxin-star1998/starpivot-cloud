package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class BrandCategoryBindBo {

    @NotNull(message = "品牌ID不能为空")
    private Long brandId;

    private List<Long> catIds;
}
