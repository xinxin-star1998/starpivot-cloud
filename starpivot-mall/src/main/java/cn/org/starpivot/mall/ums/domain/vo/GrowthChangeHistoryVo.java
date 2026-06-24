package cn.org.starpivot.mall.ums.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class GrowthChangeHistoryVo {

    private Long id;
    private Long memberId;
    private LocalDateTime createTime;
    private Integer changeCount;
    private String note;
    private Integer sourceType;
}
