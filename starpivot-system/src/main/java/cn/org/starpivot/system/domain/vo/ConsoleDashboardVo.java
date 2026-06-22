package cn.org.starpivot.system.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 控制台仪表盘 VO。
 * <p>
 * 聚合首页统计卡片、访问趋势、待办事项及动态信息等展示数据。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 *   <li>{@link Builder} — 支持建造者模式构建实例</li>
 *   <li>{@link NoArgsConstructor}、{@link AllArgsConstructor} — 配合 {@link Builder} 使用</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsoleDashboardVo {

    /**
     * 统计卡片列表
     */
    private List<CardItem> cardList;

    /**
     * 访问量趋势数据
     */
    private TrendData visitTrend;

    /**
     * 用户增长趋势数据
     */
    private TrendData userTrend;

    /**
     * 待办事项列表
     */
    private List<TodoItem> todoList;

    /**
     * 最新动态列表
     */
    private List<DynamicItem> dynamicList;

    /**
     * 新用户列表
     */
    private List<NewUserItem> newUserList;

    /**
     * 统计卡片项。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardItem {

        /**
         * 指标描述
         */
        private String des;

        /**
         * 图标标识
         */
        private String icon;

        /**
         * 指标数值
         */
        private Long num;

        /**
         * 环比变化（如 +12%）
         */
        private String change;
    }

    /**
     * 趋势图数据。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {

        /**
         * X 轴日期标签
         */
        private List<String> xAxisData;

        /**
         * Y 轴数值序列
         */
        private List<Long> data;
    }

    /**
     * 待办事项项。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodoItem {

        /**
         * 用户名
         */
        private String username;

        /**
         * 日期
         */
        private String date;

        /**
         * 是否已完成
         */
        private Boolean complete;
    }

    /**
     * 最新动态项。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicItem {

        /**
         * 用户名
         */
        private String username;

        /**
         * 操作类型
         */
        private String type;

        /**
         * 操作目标
         */
        private String target;
    }

    /**
     * 新用户项。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewUserItem {

        /**
         * 用户名
         */
        private String username;

        /**
         * 所在省份
         */
        private String province;

        /**
         * 性别（0男 1女）
         */
        private Integer sex;

        /**
         * 占比百分比
         */
        private Integer percentage;
    }
}
