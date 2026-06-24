package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class CategorySortBatchBo {

    @NotEmpty(message = "排序项不能为空")
    @Valid
    private List<CategorySortItemBo> items;
}
