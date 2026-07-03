package cn.org.starpivot.mall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SPU实体。
 * <p>
 * 对应数据库表 {@code pms_spu_info}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("pms_spu_info")
public class PmsSpuInfo {

    /**
     * 主键 ID
     */

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * spu名称
     */
    private String spuName;

    /**
     * spu Description
     */
    private String spuDescription;

    /**
     * Catalog ID
     */
    private Long catalogId;

    /**
     * 品牌 ID
     */
    private Long brandId;

    /**
     * weight
     */
    private BigDecimal weight;

    /**
     * 状态
     */
    private Integer publishStatus;

    /**
     * 审批实例 ID
     */
    private Long approvalInstanceId;

    /**
     * 审批状态
     */
    private String auditStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
