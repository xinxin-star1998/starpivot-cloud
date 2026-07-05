package cn.org.starpivot.tms.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TmsTrackEventVo {
    private LocalDateTime eventTime;
    private String eventStatus;
    private String eventDesc;
    private String location;
    private String source;
}
