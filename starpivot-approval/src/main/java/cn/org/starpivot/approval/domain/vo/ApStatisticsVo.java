package cn.org.starpivot.approval.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class ApStatisticsVo {

    private Long totalInstances;
    private Long runningCount;
    private Long approvedCount;
    private Long rejectedCount;
    private Long withdrawnCount;
    private Long pendingTaskCount;
    private Long overdueTaskCount;
    private Double avgFinishHours;
    private List<BizTypeStatItem> bizTypeStats;
    private List<DailyFinishedItem> dailyFinished;

    @Data
    public static class BizTypeStatItem {
        private String bizType;
        private Long total;
        private Long approved;
        private Long rejected;
    }

    @Data
    public static class DailyFinishedItem {
        private String day;
        private Long count;
    }
}
