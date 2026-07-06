package cn.org.starpivot.ai.config;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiPromptTemplateSnapshot {

    private final String id;

    private final String label;

    private final String description;

    private final String prompt;

    /** 可选，覆盖该场景下的默认温度 */
    private final Double temperature;

    /** 可选，覆盖该场景下的默认模型 */
    private final String model;
}
