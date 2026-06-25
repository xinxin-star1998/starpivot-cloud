package cn.org.starpivot.mall.wms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购单视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PurchaseVo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * Assignee ID
     */
    private Long assigneeId;

    /**
     * assignee名称
     */
    private String assigneeName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * priority
     */
    private Integer priority;

    /**
     * 状态
     */
    private Integer status;

    /**
     * Ware ID
     */
    private Long wareId;

    /**
     * 仓库名称（关联 wms_ware_info）
     */
    private String wareName;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * details
     */
    private List<PurchaseDetailVo> details;
}
