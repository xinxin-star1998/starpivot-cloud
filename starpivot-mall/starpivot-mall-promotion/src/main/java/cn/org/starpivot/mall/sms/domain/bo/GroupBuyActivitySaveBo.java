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
 * 拼团活动保存请求 BO。
 */
@Data
public class GroupBuyActivitySaveBo {

    private Long id;

    @NotBlank(message = "活动标题不能为空")
    @Size(max = 255, message = "活动标题长度不能超过255")
    private String title;

    @NotNull(message = "SKU 不能为空")
    private Long skuId;

    @NotNull(message = "拼团价不能为空")
    @DecimalMin(value = "0.01", message = "拼团价必须大于0")
    private BigDecimal groupPrice;

    @NotNull(message = "成团人数不能为空")
    @Min(value = 2, message = "成团人数至少为2")
    private Integer groupSize;

    @NotNull(message = "活动库存不能为空")
    @Min(value = 0, message = "活动库存不能为负")
    private Integer stockCount;

    @Min(value = 1, message = "每人限购至少为1件")
    private Integer limitBuy;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Min(value = 1, message = "成团有效小时数至少为1")
    private Integer validHours;

    private Integer status;

    private Integer sort;
}
