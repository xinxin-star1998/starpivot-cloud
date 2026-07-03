package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * C 端秒杀页视图对象。
 */
@Data
public class PortalSeckillPageVo {

    private String title;

    private String subTitle;

    /** 当前有效秒杀活动 ID */
    private Long promotionId;

    private Long activeSessionId;

    private List<PortalSeckillSessionVo> sessions = new ArrayList<>();

    private List<PortalHomeProductVo> products = new ArrayList<>();
}
