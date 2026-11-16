package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 砍价活动保存请求 BO。
 */
@Data
public class BargainActivitySaveBo {

    private Long id;

    @NotBlank(message = "活动标题不能为空")
    @Size(max = 255, message = "活动标题长度不能超过255")
    private String title;

    @NotNull(message = "SKU 不能为空")
    private Long skuId;

    @NotNull(message = "商品原价不能为空")
    @DecimalMin(value = "0.01", message = "商品原价必须大于0")
    private BigDecimal originalPrice;

    @NotNull(message = "砍价底价不能为空")
    @DecimalMin(value = "0.01", message = "砍价底价必须大于0")
    private BigDecimal floorPrice;

    @NotNull(message = "最多助力次数不能为空")
    @Min(value = 1, message = "最多助力次数至少为1")
    private Integer bargainTimes;

    @NotNull(message = "单次最低砍价金额不能为空")
    @DecimalMin(value = "0.01", message = "单次最低砍价金额必须大于0")
    private BigDecimal minAmount;

    @NotNull(message = "单次最高砍价金额不能为空")
    @DecimalMin(value = "0.01", message = "单次最高砍价金额必须大于0")
    private BigDecimal maxAmount;

    @NotNull(message = "活动库存不能为空")
    @Min(value = 0, message = "活动库存不能为负")
    private Integer stockCount;

    @Min(value = 1, message = "每人限购至少为1件")
    private Integer limitBuy;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Min(value = 1, message = "砍价有效小时数至少为1")
    private Integer validHours;

    private Integer status;

    private Integer sort;
}
