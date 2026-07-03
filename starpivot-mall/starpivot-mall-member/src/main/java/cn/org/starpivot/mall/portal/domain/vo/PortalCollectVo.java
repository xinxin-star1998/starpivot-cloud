package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PortalCollectVo {

    private Long id;

    private Long spuId;

    private String spuName;

    private String spuImg;

    private BigDecimal price;

    private Long defaultSkuId;

    /** 1=上架 */
    private Integer publishStatus;

    private LocalDateTime createTime;
}
