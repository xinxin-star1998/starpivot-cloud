package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * C 端拼团参与记录。
 */
@Data
public class PortalGroupBuyRecordVo {

    private Long id;

    private Long activityId;

    private String title;

    private Long skuId;

    private String skuName;

    private String skuImg;

    private String groupNo;

    private BigDecimal groupPrice;

    private Integer quantity;

    /** 0待支付 1待成团 2已成团 3成团失败 4已取消 */
    private Integer status;

    private String statusText;

    /** 当前会员是否为团长 */
    private Boolean leader;

    private Integer joinedCount;

    private Integer groupSize;

    private LocalDateTime expireTime;

    private Long orderId;

    private String orderSn;

    private LocalDateTime createTime;
}
