package cn.org.starpivot.ai.domain.dto;

import cn.org.starpivot.common.domain.PageReqBo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiUsageLogQueryDto extends PageReqBo {

    private Long userId;

    private String model;

    private String requestType;

    private String beginTime;

    private String endTime;
}
