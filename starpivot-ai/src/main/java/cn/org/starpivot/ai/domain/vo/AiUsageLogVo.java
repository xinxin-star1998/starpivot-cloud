package cn.org.starpivot.ai.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiUsageLogVo {

    private Long logId;

    private Long userId;

    private String conversationId;

    private String model;

    private String requestType;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Long latencyMs;

    private String success;

    private String errorMessage;

    private LocalDateTime createTime;
}
