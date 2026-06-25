package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 分类品牌关联实体。
 * <p>
 * 对应数据库表 {@code pms_category_brand_relation}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("pms_category_brand_relation")
public class PmsCategoryBrandRelation {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 品牌 ID
     */
    private Long brandId;

    /**
     * 分类 ID
     */
    private Long catelogId;

    /**
     * brand名称
     */
    private String brandName;

    /**
     * catelog名称
     */
    private String catelogName;

}
