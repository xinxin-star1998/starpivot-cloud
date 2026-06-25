package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * SPU 积分/成长值保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class SpuBoundsSaveBo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * SPU ID
     */
    /**
     * SPU ID
     */
    @NotNull(message = "SPU ID不能为空")
    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * grow Bounds
     */
    private BigDecimal growBounds;
    /**
     * buy Bounds
     */
    private BigDecimal buyBounds;
    /**
     * work
     */
    private Integer work;
}
