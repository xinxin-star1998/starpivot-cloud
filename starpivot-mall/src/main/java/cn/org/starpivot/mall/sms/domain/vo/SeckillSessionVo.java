package cn.org.starpivot.mall.sms.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SeckillSessionVo {

    private Long id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime createTime;
}
