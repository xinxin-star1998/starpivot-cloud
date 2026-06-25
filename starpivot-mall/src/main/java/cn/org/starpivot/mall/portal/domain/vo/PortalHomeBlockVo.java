package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页区块视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalHomeBlockVo {

    /** new | seckill | budget | subject */
    /**
     * code
     */
    private String code;

    /**
     * title
     */
    private String title;

    /**
     * sub Title
     */
    private String subTitle;

    /**
     * url
     */
    private String url;

    /**
     * 图片
     */
    private String coverImg;

    private List<PortalHomeProductVo> products = new ArrayList<>();

    /** 秒杀场次（仅 code=seckill 时有值） */
    private List<PortalSeckillSessionVo> sessions = new ArrayList<>();

    /** 当前推荐场次 ID（仅 code=seckill 时有值） */
    /**
     * ActiveSession ID
     */
    private Long activeSessionId;
}
