package cn.org.starpivot.api.tms.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FreightCalculateResult implements Serializable {

    private BigDecimal freightAmount;
    private Long ruleId;
    private String ruleName;
    private String ruleType;
    private BigDecimal totalWeightKg;
}
