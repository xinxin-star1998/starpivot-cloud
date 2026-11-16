package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * C 端砍价活动视图对象。
 */
@Data
public class PortalBargainActivityVo {

    private Long id;

    private String title;

    private Long skuId;

    private String skuName;

    private String skuImg;

    private BigDecimal originalPrice;

    private BigDecimal floorPrice;

    private Integer bargainTimes;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    /** 剩余活动库存 */
    private Integer remainStock;

    private Integer limitBuy;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer validHours;

    /** ongoing / upcoming / ended */
    private String state;
}
