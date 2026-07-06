package cn.org.starpivot.ai.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPromptTemplateVo {

    private String id;

    private String label;

    /** 场景说明，供前端展示 */
    private String description;
}
