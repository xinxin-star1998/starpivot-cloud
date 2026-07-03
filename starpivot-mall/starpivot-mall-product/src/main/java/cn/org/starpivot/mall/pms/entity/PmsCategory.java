package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品分类实体。
 * <p>
 * 对应数据库表 {@code pms_category}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("pms_category")
public class PmsCategory {

    /**
     * Cat ID
     */
    /**
     * cat ID
     */
    @TableId(value = "cat_id", type = IdType.AUTO)
    private Long catId;

    /**
     * 名称
     */
    private String name;

    /**
     * ParentCid
     */
    private Long parentCid;

    /**
     * cat Level
     */
    private Long catLevel;

    /**
     * 状态
     */
    private Long showStatus;

    /**
     * 排序
     */
    private Long sort;

    /**
     * icon
     */
    private String icon;

    /**
     * product Unit
     */
    private String productUnit;

    /**
     * product数量
     */
    private Long productCount;

}
