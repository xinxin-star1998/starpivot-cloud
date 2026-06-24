package cn.org.starpivot.mall.oms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 退货审核
 */
@Data
public class ReturnAuditBo {

    @NotNull(message = "退货申请ID不能为空")
    private Long id;

    @NotNull(message = "审核状态不能为空")
    private Integer status;

    private String handleNote;

    private String handleMan;
}
