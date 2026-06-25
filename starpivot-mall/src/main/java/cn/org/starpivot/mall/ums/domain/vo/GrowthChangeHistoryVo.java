package cn.org.starpivot.mall.ums.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 成长值变更历史视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class GrowthChangeHistoryVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 会员 ID
     */
    private Long memberId;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * change数量
     */
    private Integer changeCount;
    /**
     * note
     */
    private String note;
    /**
     * 类型
     */
    private Integer sourceType;
}
