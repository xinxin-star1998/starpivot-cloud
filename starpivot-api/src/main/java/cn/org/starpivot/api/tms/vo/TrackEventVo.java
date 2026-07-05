package cn.org.starpivot.api.tms.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class TrackEventVo implements Serializable {

    private LocalDateTime eventTime;
    private String eventStatus;
    private String eventDesc;
    private String location;
    private String source;
}
