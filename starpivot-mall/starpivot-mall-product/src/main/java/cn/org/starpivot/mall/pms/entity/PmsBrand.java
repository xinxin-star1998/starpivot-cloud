package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 品牌实体。
 * <p>
 * 对应数据库表 {@code pms_brand}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("pms_brand")
public class PmsBrand {

    /**
     * 品牌 ID
     */

    @TableId(value = "brand_id", type = IdType.AUTO)
    private Long brandId;

    /**
     * 名称
     */
    private String name;

    /**
     * logo
     */
    private String logo;

    /**
     * descript
     */
    private String descript;

    /**
     * 状态
     */
    private Integer showStatus;

    /**
     * first Letter
     */
    private String firstLetter;

    /**
     * 排序
     */
    private Integer sort;

}
