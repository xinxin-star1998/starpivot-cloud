package cn.org.starpivot.api.approval.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审批完结 MQ 消息体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalFinishedMessage {

    private Long instanceId;
    private String bizModule;
    private String bizType;
    private String bizKey;
    private String templateCode;
    /** APPROVED / REJECTED / WITHDRAWN */
    private String result;
    private Long starterId;
    private String comment;
    private LocalDateTime finishTime;
}
