package cn.org.starpivot.ai.domain.vo;

import lombok.Data;

@Data
public class AiUsageSummaryVo {

    private Long totalRequests;

    private Long totalTokens;

    private Long successRequests;

    private Long failedRequests;

    private Long avgLatencyMs;

    private Long promptTokens;

    private Long completionTokens;
}
