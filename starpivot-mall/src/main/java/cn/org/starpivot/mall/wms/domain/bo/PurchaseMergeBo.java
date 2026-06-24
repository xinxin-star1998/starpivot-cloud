package cn.org.starpivot.mall.wms.domain.bo;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class PurchaseMergeBo {

    /** 目标采购单 ID，为空则新建 */
    private Long purchaseId;

    @NotEmpty(message = "合并的采购需求不能为空")
    private List<Long> items;
}
