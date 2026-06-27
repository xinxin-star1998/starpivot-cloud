package cn.org.starpivot.approval.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApTaskVo {

    private Long taskId;
    private Long instanceId;
    private String title;
    private String bizModule;
    private String bizType;
    private String bizKey;
    private String stepCode;
    private String stepName;
    private String status;
    private String action;
    private String comment;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
