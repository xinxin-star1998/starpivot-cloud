package cn.org.starpivot.ai.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RagRetrievalResult {

    private String context;

    private List<RagSourceVo> sources;

    /** 检索异常降级为无知识上下文时为 true */
    private boolean degraded;
}
