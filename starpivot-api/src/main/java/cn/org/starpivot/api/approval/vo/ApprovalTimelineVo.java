package cn.org.starpivot.api.approval.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批时间轴（供前端 Step 组件与 Feign 调用方使用）。
 */
@Data
public class ApprovalTimelineVo {

    private Long instanceId;
    private String title;
    private String status;
    private String bizModule;
    private String bizType;
    private String bizKey;
    private List<TimelineStepVo> steps;

    @Data
    public static class TimelineStepVo {
        private String stepCode;
        private String stepName;
        /** PENDING / DONE / SKIPPED / CANCELLED */
        private String status;
        private List<String> assignees;
        private List<TimelineRecordVo> records;
    }

    @Data
    public static class TimelineRecordVo {
        private String operatorName;
        private String action;
        private String comment;
        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime time;
    }
}
