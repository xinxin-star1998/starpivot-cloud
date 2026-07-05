package cn.org.starpivot.tms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TmsFreightRuleVo {
    private Long id;
    private String ruleName;
    private String ruleType;
    private String defaultFlag;
    private BigDecimal baseFee;
    private BigDecimal firstWeightKg;
    private BigDecimal firstFee;
    private BigDecimal continueFeePerKg;
    private String status;
    private Integer sortOrder;
    private String remark;
}
