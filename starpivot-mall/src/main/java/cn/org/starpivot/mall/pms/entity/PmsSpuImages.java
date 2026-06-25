package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * SPU 图片实体。
 * <p>
 * 对应数据库表 {@code pms_spu_images}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("pms_spu_images")
public class PmsSpuImages {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * img名称
     */
    private String imgName;

    /**
     * img Url
     */
    private String imgUrl;

    /**
     * img Sort
     */
    private Integer imgSort;

    /**
     * 图片
     */
    private Integer defaultImg;

}
