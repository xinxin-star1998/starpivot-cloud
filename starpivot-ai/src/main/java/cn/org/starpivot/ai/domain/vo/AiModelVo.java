package cn.org.starpivot.ai.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelVo {

    private String id;

    private String label;

    /** 所属供应商名称，对话窗口分组展示 */
    private String providerName;

    private String providerCode;
}
