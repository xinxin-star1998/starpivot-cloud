package cn.org.starpivot.system.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConsoleDashboardVo {

    private List<CardItem> cardList;
    private TrendData visitTrend;
    private TrendData userTrend;
    private List<TodoItem> todoList;
    private List<DynamicItem> dynamicList;
    private List<NewUserItem> newUserList;

    @Data
    @Builder
    public static class CardItem {
        private String des;
        private String icon;
        private Long num;
        private String change;
    }

    @Data
    @Builder
    public static class TrendData {
        private List<String> xAxisData;
        private List<Long> data;
    }

    @Data
    @Builder
    public static class TodoItem {
        private String username;
        private String date;
        private Boolean complete;
    }

    @Data
    @Builder
    public static class DynamicItem {
        private String username;
        private String type;
        private String target;
    }

    @Data
    @Builder
    public static class NewUserItem {
        private String username;
        private String province;
        private Integer sex;
        private Integer percentage;
    }
}
