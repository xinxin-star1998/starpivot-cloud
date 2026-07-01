package cn.org.starpivot.approval.domain.vo;

import lombok.Data;

@Data
public class ApTemplateRouteVo {

    private Long routeId;
    private String fromStepCode;
    private String fromStepName;
    private String toStepCode;
    private String toStepName;
    private Integer priority;
    private String conditionExpr;
}
