package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

/**
 * C 端砍价活动详情。
 */
@Data
public class PortalBargainDetailVo {

    private PortalBargainActivityVo activity;

    /** 当前会员在本活动下的砍价记录，未发起时为空 */
    private PortalBargainRecordVo record;
}
