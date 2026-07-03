package cn.org.starpivot.mall.pms.domain.bo;

import lombok.Data;

import java.util.List;

/**
 * 商品分类视图对象。
 * <p>
 * 用于接口响应的数据视图。
 * </p>
 * <ul>
 *   <li>{@link Data} — 自动生成 getter/setter 等方法</li>
 * </ul>
 */

@Data
public class PmsCategoryVO {

    /**
     * Cat ID
     */
    private Long catId;

    /**
     * 名称
     */
    private String name;

    /**
     * ParentCid
     */
    private Long parentCid;

    /**
     * cat Level
     */
    private Integer catLevel;

    /**
     * 状态
     */
    private Integer showStatus;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * icon
     */
    private String icon;

    /**
     * product Unit
     */
    private String productUnit;

    /**
     * product数量
     */
    private Integer productCount;

    /**
     * children
     */
    private List<PmsCategoryVO> children;
}
