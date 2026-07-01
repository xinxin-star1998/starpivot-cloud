package cn.org.starpivot.approval.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApNotificationVo {

    private Long notifyId;
    private String notifyType;
    private String title;
    private String content;
    private Long instanceId;
    private Long taskId;
    private String readFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
