package cn.org.starpivot.tms.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TmsCarrierSaveDto {

    private Long id;

    @NotBlank(message = "承运商编码不能为空")
    private String carrierCode;

    @NotBlank(message = "承运商名称不能为空")
    private String carrierName;

    @NotBlank(message = "快递100编码不能为空")
    private String kuaidi100Com;

    private Integer sortOrder = 0;
    private String status = "0";
    private String remark;
}
