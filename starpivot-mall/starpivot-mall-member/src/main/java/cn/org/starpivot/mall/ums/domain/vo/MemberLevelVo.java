package cn.org.starpivot.mall.ums.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * 会员等级视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class MemberLevelVo {

    /**
     * 主键 ID
     */
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * growth Point
     */
    private Integer growthPoint;
    /**
     * 状态
     */
    private Integer defaultStatus;
    /**
     * free Freight Point
     */
    private BigDecimal freeFreightPoint;
    /**
     * comment Growth Point
     */
    private Integer commentGrowthPoint;
    /**
     * privilege Free Freight
     */
    private Integer privilegeFreeFreight;
    /**
     * privilege Member Price
     */
    private Integer privilegeMemberPrice;
    /**
     * privilege Birthday
     */
    private Integer privilegeBirthday;
    /**
     * note
     */
    private String note;
}
