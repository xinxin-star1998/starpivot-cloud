package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀活动保存请求 BO。
 * <p>
 * 用于新增或修改接口的请求体。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class SeckillPromotionSaveBo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * title
     */
    /**
     * title
     */
    @NotBlank(message = "活动标题不能为空")
    @Size(max = 255, message = "活动标题长度不能超过255")
    /**
     * title
     */
    private String title;

    /**
     * start时间
     */
    private LocalDateTime startTime;
    /**
     * end时间
     */
    private LocalDateTime endTime;
    /**
     * 状态
     */
    private Integer status;
    /**
     * sku List
     */
    private List<SeckillSkuRelationBo> skuList;
}
