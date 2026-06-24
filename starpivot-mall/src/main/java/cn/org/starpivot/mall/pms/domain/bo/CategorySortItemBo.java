package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategorySortItemBo {

    @NotNull(message = "分类ID不能为空")
    private Long catId;

    @NotNull(message = "排序值不能为空")
    private Integer sort;
}
