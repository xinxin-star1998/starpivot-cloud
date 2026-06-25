package cn.org.starpivot.mall.wms.domain.vo;

import lombok.Data;

/**
 * 仓库工单明细视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class WareOrderTaskDetailVo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * sku名称
     */
    private String skuName;

    /**
     * sku Num
     */
    private Integer skuNum;

    /**
     * Task ID
     */
    private Long taskId;

    /**
     * Ware ID
     */
    private Long wareId;

    /**
     * 状态
     */
    private Integer lockStatus;
}
