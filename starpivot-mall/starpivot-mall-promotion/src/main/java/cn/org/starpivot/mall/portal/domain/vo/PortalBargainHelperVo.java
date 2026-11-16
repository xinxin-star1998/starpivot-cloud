package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * C 端砍价助力记录。
 */
@Data
public class PortalBargainHelperVo {

    private Long helperMemberId;

    private BigDecimal cutAmount;
}
