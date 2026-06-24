package cn.org.starpivot.mall.portal.domain.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class PortalHomeBlockVo {

    /** new | seckill | budget | subject */
    private String code;

    private String title;

    private String subTitle;

    private String url;

    private String coverImg;

    private List<PortalHomeProductVo> products = new ArrayList<>();

    /** 秒杀场次（仅 code=seckill 时有值） */
    private List<PortalSeckillSessionVo> sessions = new ArrayList<>();

    /** 当前推荐场次 ID（仅 code=seckill 时有值） */
    private Long activeSessionId;
}
