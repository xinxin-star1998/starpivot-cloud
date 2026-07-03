package cn.org.starpivot.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员价格实体。
 * <p>
 * 对应数据库表 {@code sms_member_price}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("sms_member_price")
public class SmsMemberPrice {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * MemberLevel ID
     */
    private Long memberLevelId;

    /**
     * memberLevel名称
     */
    private String memberLevelName;

    /**
     * member Price
     */
    private BigDecimal memberPrice;

    /**
     * add Other
     */
    private Integer addOther;

}
