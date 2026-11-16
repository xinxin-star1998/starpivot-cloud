package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * C 端拼团活动详情。
 */
@Data
public class PortalGroupBuyDetailVo {

    private PortalGroupBuyActivityVo activity;

    /** 可直接参与的进行中团 */
    private List<PortalGroupBuyGroupVo> groups;
}
