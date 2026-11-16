package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * C 端砍价记录（含助力明细）。
 */
@Data
public class PortalBargainRecordVo {

    private Long id;

    private Long activityId;

    private String title;

    private Long skuId;

    private String skuName;

    private String skuImg;

    private BigDecimal originalPrice;

    private BigDecimal currentPrice;

    private BigDecimal floorPrice;

    private Integer bargainTimes;

    /** 已助力次数 */
    private Integer helpCount;

    /** 剩余可助力次数 */
    private Integer remainHelps;

    /** 0砍价中 1已到底 2已下单 3已失效 */
    private Integer status;

    private String statusText;

    private LocalDateTime expireTime;

    private Long orderId;

    private String orderSn;

    private LocalDateTime createTime;

    private List<PortalBargainHelperVo> helpers;
}
