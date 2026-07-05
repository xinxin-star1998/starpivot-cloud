package cn.org.starpivot.tms.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TmsFreightRuleSaveDto {

    private Long id;

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    private String defaultFlag = "0";
    private BigDecimal baseFee;
    private BigDecimal firstWeightKg;
    private BigDecimal firstFee;
    private BigDecimal continueFeePerKg;
    private String status = "0";
    private Integer sortOrder = 0;
    private String remark;
}
