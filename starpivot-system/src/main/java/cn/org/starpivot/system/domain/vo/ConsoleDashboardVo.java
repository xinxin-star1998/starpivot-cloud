package cn.org.starpivot.system.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsoleDashboardVo {

    private List<CardItem> cardList;
    private TrendData visitTrend;
    private TrendData userTrend;
    private List<TodoItem> todoList;
    private List<DynamicItem> dynamicList;
    private List<NewUserItem> newUserList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardItem {
        private String des;
        private String icon;
        private Long num;
        private String change;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private List<String> xAxisData;
        private List<Long> data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodoItem {
        private String username;
        private String date;
        private Boolean complete;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicItem {
        private String username;
        private String type;
        private String target;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewUserItem {
        private String username;
        private String province;
        private Integer sex;
        private Integer percentage;
    }
}
