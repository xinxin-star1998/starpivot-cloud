package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PortalCommentSummaryVo {

    private Long spuId;

    private Long total;

    private BigDecimal avgStar;
}
