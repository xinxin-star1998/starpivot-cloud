package cn.org.starpivot.mall.ums.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会员等级实体。
 * <p>
 * 对应数据库表 {@code ums_member_level}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("ums_member_level")
public class UmsMemberLevel {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
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
