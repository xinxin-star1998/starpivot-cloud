package cn.org.starpivot.mall.pms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** SPU 上架/下架 */

/**
 * 发布状态请求 BO。
 * <p>
 * 用于接口请求或响应的数据传输。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PublishStatusBo {

    /**
     * 主键 ID
     */
    /**
     * 主键 ID
     */
    @NotNull(message = "SPU ID 不能为空")
    /**
     * 主键 ID
     */
    private Long id;

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
