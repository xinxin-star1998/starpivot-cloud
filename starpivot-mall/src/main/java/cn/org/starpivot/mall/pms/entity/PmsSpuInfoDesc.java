package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * SPU 介绍实体。
 * <p>
 * 对应数据库表 {@code pms_spu_info_desc}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("pms_spu_info_desc")
public class PmsSpuInfoDesc {

    /**
     * SPU ID
     */

    @TableId(value = "spu_id", type = IdType.INPUT)
    private Long spuId;

    /**
     * decript
     */
    private String decript;

}
