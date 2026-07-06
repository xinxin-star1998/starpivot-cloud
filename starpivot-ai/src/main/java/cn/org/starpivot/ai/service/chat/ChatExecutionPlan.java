package cn.org.starpivot.ai.service.chat;

import lombok.Builder;

@Builder
public record ChatExecutionPlan(
        ChatIntent intent,
        String promptScene,
        String model,
        boolean useRag,
        boolean autoScene,
        boolean autoModel) {}
