package cn.org.starpivot.mall.sms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SeckillPromotionSaveBo {

    private Long id;

    @NotBlank(message = "活动标题不能为空")
    @Size(max = 255, message = "活动标题长度不能超过255")
    private String title;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private List<SeckillSkuRelationBo> skuList;
}
