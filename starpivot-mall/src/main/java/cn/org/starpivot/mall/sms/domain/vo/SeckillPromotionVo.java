package cn.org.starpivot.mall.sms.domain.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SeckillPromotionVo {

    private Long id;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime createTime;
    private Long userId;
    private List<SeckillSkuRelationVo> skuList;
}
