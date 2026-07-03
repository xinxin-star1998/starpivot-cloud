package cn.org.starpivot.mall.portal.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 秒杀场次视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PortalSeckillSessionVo {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /** HH:mm */
    /**
     * start Label
     */
    private String startLabel;

    /** HH:mm */
    /**
     * end Label
     */
    private String endLabel;

    /** ongoing | upcoming | ended */
    /**
     * state
     */
    private String state;

    private List<PortalHomeProductVo> products = new ArrayList<>();
}
