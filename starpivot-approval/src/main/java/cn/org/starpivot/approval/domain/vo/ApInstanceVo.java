package cn.org.starpivot.approval.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApInstanceVo {

    private Long instanceId;
    private String templateCode;
    private String bizModule;
    private String bizType;
    private String bizKey;
    private String title;
    private Long starterId;
    private String starterName;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
