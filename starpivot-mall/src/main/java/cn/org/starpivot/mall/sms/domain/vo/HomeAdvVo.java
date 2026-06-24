package cn.org.starpivot.mall.sms.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HomeAdvVo {

    private Long id;
    private String name;
    private String pic;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    private Integer status;
    private Integer clickCount;
    private String url;
    private String note;
    private Integer sort;
    private Long publisherId;
    private Long authId;
}
