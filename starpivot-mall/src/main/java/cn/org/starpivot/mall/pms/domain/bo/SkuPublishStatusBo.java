package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** SKU 上下架（更新所属 SPU 的 publish_status） */

/**
 * SKU 发布状态请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class SkuPublishStatusBo {

    /**
     * SKU ID
     */
    /**
     * SKU ID
     */
    @NotNull(message = "SKU ID 不能为空")
    /**
     * SKU ID
     */
    private Long skuId;

    /** 0-下架 1-上架 */
    /**
     * 状态
     */
    /**
     * 状态
     */
    @NotNull(message = "上架状态不能为空")
    /**
     * 状态
     */
    private Integer publishStatus;
}
