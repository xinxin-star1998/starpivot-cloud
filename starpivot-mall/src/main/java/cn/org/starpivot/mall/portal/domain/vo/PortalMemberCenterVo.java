package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

@Data
public class PortalMemberCenterVo {

    private PortalMemberVo member;

    private String levelName;

    private Integer collectCount;

    private Integer orderCount;

    private Integer couponCount;

    private Integer commentCount;

    private Integer pendingReviewCount;
}
